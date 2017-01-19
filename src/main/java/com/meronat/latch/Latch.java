/*
 * This file is part of Latch, licensed under the MIT License (MIT).
 *
 * Copyright (c) IchorPowered <https://github.com/IchorPowered>
 * Copyright (c) Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.meronat.latch;

import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import com.meronat.latch.bstats.Metrics;
import com.meronat.latch.commands.BaseCommand;
import com.meronat.latch.commands.UnlockCommand;
import com.meronat.latch.entities.LockManager;
import com.meronat.latch.listeners.ChangeBlockListener;
import com.meronat.latch.listeners.InteractBlockListener;
import com.meronat.latch.listeners.NotifyNeighborListener;
import com.meronat.latch.listeners.SpawnEntityListener;
import com.meronat.latch.storage.SqlHandler;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.util.Tristate;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Plugin(
        id = "latch",
        name = "Latch",
        version = "0.2.0",
        description = "A locking plugin which optionally allows you to lockpick those locks.",
        url = "http://ichorcommunity.com/",
        authors = {
                "Nighteyes604",
                "Meronat"
        }
)
public class Latch {

    private static Logger logger;
    private static Path configPath;

    private static final LockManager lockManager = new LockManager();

    private static SqlHandler storageHandler;

    @Inject
    public Latch(Logger logger, @DefaultConfig(sharedRoot = false) Path configPath) {
        Latch.logger = logger;
        Latch.configPath = configPath;
    }

    @Inject
    @DefaultConfig(sharedRoot = false)
    private ConfigurationLoader<CommentedConfigurationNode> configManager;

    @Inject
    private Metrics metrics;

    private static Configuration config;

    @Listener
    public void onGameInit(GameInitializationEvent event) {

        // TODO Configure if redstone can interact with lock
        // TODO Add admin bypass, PurgeALL command
        // TODO Lock visualize command

        config = new Configuration(configManager);
        storageHandler = new SqlHandler();

        loadConfigurationData();

        registerListeners();

        Sponge.getCommandManager().register(this, new BaseCommand().getCommand(), "latch", "lock");
        Sponge.getCommandManager().register(this, new UnlockCommand().getCommand(), "unlock", "unlatch");

        // Register base permission node.
        if(getConfig().getNode("add_default_permissions").getBoolean()) {
            Sponge.getServiceManager().provide(PermissionService.class).ifPresent(
                    p -> p.getUserSubjects().getDefaults().getSubjectData()
                            .setPermission(p.getDefaults().getActiveContexts(), "latch.normal", Tristate.TRUE));
        }

    }

    public static Logger getLogger() {
        return logger;
    }

    private void registerListeners() {

        EventManager eventManager = Sponge.getEventManager();

        eventManager.registerListeners(this, new ChangeBlockListener());
        eventManager.registerListeners(this, new InteractBlockListener());
        eventManager.registerListeners(this, new SpawnEntityListener());
        eventManager.registerListeners(this, new NotifyNeighborListener());

    }

    private void loadConfigurationData() {
        List<String> configBlockNames = new ArrayList<>();
        List<String> restrictedBlockNames = new ArrayList<>();
        List<String> protectBelowBlocks = new ArrayList<>();
        HashMap<String, Integer> lockLimits = new HashMap<>();

        try {
            configBlockNames = getConfig().getNode("lockable_blocks").getList(TypeToken.of(String.class));
        } catch (ObjectMappingException e) {
            getLogger().error("Error loading list of lockable_blocks.");
            e.printStackTrace();
        }

        try {
            restrictedBlockNames = getConfig().getNode("prevent_adjacent_to_locks").getList(TypeToken.of(String.class));
        } catch (ObjectMappingException e) {
            getLogger().error("Error loading list of prevent_adjacent_to_locks.");
            e.printStackTrace();
        }

        try {
            protectBelowBlocks = getConfig().getNode("protect_below_block").getList(TypeToken.of(String.class));
        } catch (ObjectMappingException e) {
            getLogger().error("Error loading list of protect_below_block.");
            e.printStackTrace();
        }

        try {
            lockLimits = (HashMap<String, Integer>) getConfig().getNode("lock_limit").getValue(new TypeToken<Map<String, Integer>>() {});
        } catch (ObjectMappingException e) {
            getLogger().error("Error loading lock limits");
            e.printStackTrace();
        }

        lockManager.setLockableBlocks(configBlockNames);
        lockManager.setRestrictedBlocks(restrictedBlockNames);
        lockManager.setProtectBelowBlocks(protectBelowBlocks);
        lockManager.setLockLimits(lockLimits);
        lockManager.setProtectFromRedstone(getConfig().getNode("protect_from_redstone").getBoolean(false));
    }

    public static SqlHandler getStorageHandler() {
        return storageHandler;
    }

    public static LockManager getLockManager() {
        return lockManager;
    }

    public static CommentedConfigurationNode getConfig() {
        return config.getConfigurationNode();
    }

    public static Path getConfigPatch() {
        return configPath;
    }

}