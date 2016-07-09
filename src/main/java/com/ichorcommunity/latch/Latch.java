package com.ichorcommunity.latch;

import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import com.ichorcommunity.latch.commands.BaseCommand;
import com.ichorcommunity.latch.entities.LockManager;
import com.ichorcommunity.latch.listeners.ChangeBlockListener;
import com.ichorcommunity.latch.listeners.CreateLockListener;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

@Plugin(
        id = "latch",
        name = "Latch",
        version = "0.0.1",
        description = "A locking plugin which optionally allows you to lockpick those locks.",
        url = "http://ichorcommunity.com/",
        authors = {
                "Nighteyes604",
                "Meronat"
        }
)
public class Latch {

    @Inject
    private static Logger logger;

    public static LockManager lockManager = new LockManager();

    @Inject
    public Latch(Logger logger) {
        this.logger = logger;
    }

    @Inject
    @DefaultConfig(sharedRoot = false)
    private ConfigurationLoader<CommentedConfigurationNode> configManager;

    private static Configuration config;


    @Listener
    public void onGameInit(GameInitializationEvent event) {
        config = new Configuration(configManager);
        loadConfigurationData();

        registerListeners();

        Sponge.getCommandManager().register(this, new BaseCommand().baseCommand, "latch","lock");

        TypeToken<List<String>> stringList = new TypeToken<List<String>>() {};

        try {
            getLogger().info("Size: " + getConfig().getNode("lockable_blocks").getList(stringList).size());
        } catch (ObjectMappingException e) {
            e.printStackTrace();
        }

    }

    public static Logger getLogger() {
        return logger;
    }

    private void registerListeners() {
        Sponge.getEventManager().registerListeners(this, new ChangeBlockListener());
        Sponge.getEventManager().registerListeners(this, new CreateLockListener());
        Sponge.getEventManager().registerListeners(this, new ChangeBlockListener());
    }

    private void loadConfigurationData() {
        List<String> configBlockNames = new ArrayList<String>();
        List<String> restrictedBlockNames = new ArrayList<String>();

        try {
            configBlockNames = getConfig().getNode("lockable_blocks").getList(TypeToken.of(String.class));
        } catch (ObjectMappingException e) {
            getLogger().error("Error loading list of lockable_blocks.");
            e.printStackTrace();
        }

        try {
            configBlockNames = getConfig().getNode("prevent_adjacent_to_locks").getList(TypeToken.of(String.class));
        } catch (ObjectMappingException e) {
            getLogger().error("Error loading list of prevent_adjacent_to_locks.");
            e.printStackTrace();
        }

        lockManager.setLockableBlocks(configBlockNames);
        lockManager.setRestrictedBlocks(restrictedBlockNames);
    }

    public static CommentedConfigurationNode getConfig() {
        return config.getConfigurationNode();
    }
}