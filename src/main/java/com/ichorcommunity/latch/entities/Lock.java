package com.ichorcommunity.latch.entities;


import com.ichorcommunity.latch.enums.LockType;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
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
        this(owner, type, location, lockedObjectName, "", new HashSet<>());
    }

    public Lock(UUID owner, LockType type, Location<World> location, String lockedObjectName, String password) {
        this(owner, type, location, lockedObjectName, password, new HashSet<>());
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

    public List<String> getAbleToAccessNames() {
        List<String> names = new ArrayList<String>();

        Optional<UserStorageService> userStorageService = Sponge.getGame().getServiceManager().provide(UserStorageService.class);

        if(userStorageService.isPresent()) {
            for(UUID uuid : ableToAccess) {
                Optional<User> user = userStorageService.get().get(uuid);
                if(user.isPresent()) {
                    names.add(user.get().getName());
                }
            }

        }

        return names;
    }

    public boolean canAccess(UUID uniqueId) {
        return ableToAccess.contains(uniqueId) || owner.equals(uniqueId);
    }

    public String getPassword() {
        return password;
    }

    public String getOwnerName() {
        Optional<UserStorageService> userStorageService = Sponge.getGame().getServiceManager().provide(UserStorageService.class);

        if(userStorageService.isPresent()) {
            Optional<User> user = userStorageService.get().get(owner);
            return user.get().getName();
        }
        return "(owner name not found)";
    }
}
