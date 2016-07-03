package com.ichorcommunity.latch;

import com.google.inject.Inject;
import com.ichorcommunity.latch.entities.LockManager;
import com.ichorcommunity.latch.listeners.BlockListener;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.Plugin;

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

    @Listener
    public void onGamePreInit(GamePreInitializationEvent event) {

    }

    @Listener
    public void onGameInit(GameInitializationEvent event) {
        registerListeners();
    }

    public static Logger getLogger() {
        return logger;
    }

    private void registerListeners() {
        Sponge.getEventManager().registerListeners(this, new BlockListener());
    }

}