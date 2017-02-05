/*
 * This file is part of Latch, licensed under the MIT License.
 *
 * Copyright (c) 2016-2017 IchorPowered <https://github.com/IchorPowered>
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
import com.meronat.latch.commands.AddAccessorCommand;
import com.meronat.latch.commands.AdminBypassCommand;
import com.meronat.latch.commands.ChangeLockCommand;
import com.meronat.latch.commands.CreateDonationLockCommand;
import com.meronat.latch.commands.CreatePasswordLockCommand;
import com.meronat.latch.commands.CreatePrivateLockCommand;
import com.meronat.latch.commands.CreatePublicLockCommand;
import com.meronat.latch.commands.DisplayLockCommand;
import com.meronat.latch.commands.HelpCommand;
import com.meronat.latch.commands.InfoCommand;
import com.meronat.latch.commands.LimitsCommand;
import com.meronat.latch.commands.ListCommand;
import com.meronat.latch.commands.PersistCommand;
import com.meronat.latch.commands.PurgeCommand;
import com.meronat.latch.commands.RemoveAccessorCommand;
import com.meronat.latch.commands.RemoveLockCommand;
import com.meronat.latch.commands.UnlockCommand;
import com.meronat.latch.entities.LockManager;
import com.meronat.latch.listeners.ChangeBlockListener;
import com.meronat.latch.listeners.InteractBlockListener;
import com.meronat.latch.listeners.NotifyNeighborListener;
import com.meronat.latch.listeners.PlayerDisconnectListener;
import com.meronat.latch.storage.SqlHandler;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Tristate;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Plugin(
        id = Info.ID,
        name = Info.NAME,
        version = Info.VERSION,
        description = Info.DESCRIPTION,
        url = Info.URL,
        authors = {
                "Nighteyes604",
                "Meronat"
        }
)
public class Latch {

    private static Logger logger;
    private static Path configPath;

    private static final LockManager lockManager = new LockManager();

    private static PluginContainer plugin;

    private static SqlHandler storageHandler;

    @Inject
    public Latch(Logger logger, @DefaultConfig(sharedRoot = false) Path configPath, PluginContainer pluginContainer) {
        Latch.logger = logger;
        Latch.configPath = configPath;
        Latch.plugin = pluginContainer;
    }

    @Inject
    @DefaultConfig(sharedRoot = false)
    private ConfigurationLoader<CommentedConfigurationNode> configManager;

    @Inject
    private Metrics metrics;

    private static Configuration config;

    @Listener
    public void onGameInit(GameInitializationEvent event) {
        config = new Configuration(configManager);
        storageHandler = new SqlHandler();

        loadConfigurationData();

        registerListeners();

        Sponge.getCommandManager().register(this, CommandSpec.builder()
                .description(Text.of("The base Latch command."))
                .permission("latch.normal")
                .child(new CreatePrivateLockCommand().getCommand(), "private")
                .child(new CreateDonationLockCommand().getCommand(), "donation")
                .child(new CreatePublicLockCommand().getCommand(), "public")
                .child(new CreatePasswordLockCommand().getCommand(), "password")
                .child(new PersistCommand().getCommand(), "persist", "clear", "unpersist", "stop", "cancel")
                .child(new RemoveLockCommand().getCommand(), "delete", "removelock")
                .child(new ChangeLockCommand().getCommand(), "change")
                .child(new DisplayLockCommand().getCommand(), "info", "display")
                .child(new UnlockCommand().getCommand(), "open", "unlock")
                .child(new ListCommand().getCommand(), "list", "displayall")
                .child(new HelpCommand().getCommand(), "help")
                .child(new AddAccessorCommand().getCommand(), "add", "plus")
                .child(new RemoveAccessorCommand().getCommand(), "remove", "minus", "rem", "removeplayer")
                .child(new AdminBypassCommand().getCommand(), "bypass", "adminbypass", "admin")
                .child(new PurgeCommand().getCommand(), "purge", "destroyall")
                .child(new InfoCommand().getCommand(), "version", "authors")
                .child(new LimitsCommand().getCommand(), "limits", "max")
                .executor(new HelpCommand())
                .build(), "latch", "lock");

        Sponge.getCommandManager().register(this, new UnlockCommand().getCommand(), "unlock", "unlatch");

        // Register base permission node.
        if(getConfig().getNode("add_default_permissions").getBoolean()) {
            Sponge.getServiceManager().provide(PermissionService.class).ifPresent(
                    p -> p.getUserSubjects().getDefaults().getSubjectData()
                            .setPermission(p.getDefaults().getActiveContexts(), "latch.normal", Tristate.TRUE));
        }
    }

    private void registerTasks() {

        if (getConfig().getNode("clean_old_locks").getBoolean(false)) {
            Task.builder()
                    .name("clean-old-locks")
                    .interval(getConfig().getNode("clean_old_locks_interval").getInt(2), TimeUnit.HOURS)
                    .execute(() -> {

                        // TODO SQL Statement which deletes these old locks

                    })
                    .submit(getPluginContainer());
        }

    }

    private void registerListeners() {
        EventManager eventManager = Sponge.getEventManager();

        eventManager.registerListeners(this, new ChangeBlockListener());
        eventManager.registerListeners(this, new InteractBlockListener());
        if (getConfig().getNode("protect_from_redstone").getBoolean(false)); {
            eventManager.registerListeners(this, new NotifyNeighborListener());
        }
        if (getConfig().getNode("remove_bypass_on_logout").getBoolean()) {
            eventManager.registerListeners(this, new PlayerDisconnectListener());
        }
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

    public static Logger getLogger() {
        return logger;
    }

    public static SqlHandler getStorageHandler() {
        return storageHandler;
    }

    public static LockManager getLockManager() {
        return lockManager;
    }

    public static CommentedConfigurationNode getConfig() {
        return config.getRootNode();
    }

    public static Path getConfigPatch() {
        return configPath;
    }

    public static PluginContainer getPluginContainer() {
        return plugin;
    }

}