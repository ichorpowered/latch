package com.ichorcommunity.latch.entities;


import com.ichorcommunity.latch.enums.LockType;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.HashSet;
import java.util.UUID;

public class Lock {

    private UUID owner;

    private String name;

    private LockType type;

    private UUID world;
    private Location<World> location;

    private String lockedObjectName;

    private String password = "";
    private HashSet<UUID> ableToAccess;

    public Lock(UUID owner, LockType type, Location<World> location, String lockedObjectName) {
        this(owner, type, location, lockedObjectName, "", new HashSet<UUID>());
    }

    public Lock(UUID owner, LockType type, Location<World> location, String lockedObjectName, String password) {
        this(owner, type, location, lockedObjectName, password, new HashSet<UUID>());
    }

    public Lock(UUID owner, LockType type, Location<World> location, String lockedObjectName, String password, HashSet<UUID> players) {

        this.owner = owner;
        this.type = type;
        this.lockedObjectName = lockedObjectName;

        this.password = password;

        this.location = location;
        this.world = location.getExtent().getUniqueId();

        this.name = location.getBlockType().getName().substring( location.getBlockType().getName().lastIndexOf(":")+1) +
                "-" + location.getExtent().getName() +
                "," + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ();

        ableToAccess = players;
    }

    public Location<World> getLocation() {
        return location;
    }

    public void setOwner(UUID uuid) {
        this.owner = uuid;
    }

    public UUID getOwner() {
        return this.owner;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public LockType getLockType() {
        return this.type;
    }

    public String getLockedObject() {
        return lockedObjectName;
    }

    public boolean isOwner(UUID uniqueId) {
        return owner.equals(uniqueId);
    }

    public void setType(LockType type) {
        this.type = type;
    }

    /*
     * When changing the password, clear allowed members
     */
    public void changePassword(String password) {
        this.password = password;
        ableToAccess.clear();
    }

    public void addAccess(UUID uuid) {
        ableToAccess.add(uuid);
    }

    public void removeAccess(UUID uuid) {
        ableToAccess.remove(uuid);
    }

    public boolean canAccess(UUID uniqueId) {
        return ableToAccess.contains(uniqueId) || owner.equals(uniqueId);
    }

    public String getPassword() {
        return password;
    }
}
