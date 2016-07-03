package com.ichorcommunity.latch.entities;


import org.spongepowered.api.world.Location;

import java.util.HashMap;
import java.util.Optional;

public class LockManager {

    private HashMap<Location, Lock> locks = new HashMap<Location, Lock>();

    public Optional<Lock> getLock(Location location) {
        return Optional.ofNullable(locks.get(location));
    }

}
