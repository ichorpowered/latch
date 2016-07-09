package com.ichorcommunity.latch.events;

import com.ichorcommunity.latch.entities.Lock;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.cause.Cause;

public class LockCreateEvent implements Event, Cancellable {

    private final Cause cause;
    private final Player creator;
    private final Lock lock;
    private boolean cancelled = false;

    public LockCreateEvent(Player creator, Lock lock, Cause cause) {
        this.creator = creator;
        this.lock = lock;
        this.cause = cause;
    }

    public Player getPlayer() {
        return creator;
    }

    public Lock getLock() {
        return lock;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public Cause getCause() {
        return cause;
    }
}
