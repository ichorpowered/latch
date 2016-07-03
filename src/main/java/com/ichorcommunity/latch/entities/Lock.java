package com.ichorcommunity.latch.entities;


import com.ichorcommunity.latch.enums.LockType;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.UUID;

public class Lock {

    private UUID owner;

    private String name;

    private LockType type;

    private String world;
    private Location location;

    private String password = "";

    public Lock(UUID owner, LockType type, Location<World> location) {
        this.owner = owner;
        this.type = type;

        this.location = location;
        this.world = location.getExtent().getName();

        this.name = location.getBlockType().toString() + "-" + world + "," + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ();
    }





}
