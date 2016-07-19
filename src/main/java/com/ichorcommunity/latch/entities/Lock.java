package com.ichorcommunity.latch.entities;


import com.ichorcommunity.latch.enums.LockType;
import com.ichorcommunity.latch.utils.LatchUtils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class Lock {

    private UUID owner;

    private String name;

    private LockType type;

    private HashSet<Location<World>> location = new HashSet<Location<World>>();

    private String lockedObjectName;

    private byte[] salt = new byte[8];
    private String password = "";
    private HashSet<UUID> ableToAccess;

    public Lock(UUID owner, LockType type, HashSet<Location<World>> location, String lockedObjectName, byte[] salt, String password) {
        this(owner, LatchUtils.getRandomLockName(owner, lockedObjectName), type, location, lockedObjectName, salt, password, new HashSet<>());
    }

    public Lock(UUID owner, String lockName, LockType type, HashSet<Location<World>> location, String lockedObjectName, byte[] salt, String password, HashSet<UUID> players) {

        this.owner = owner;
        this.type = type;
        this.lockedObjectName = lockedObjectName;

        this.salt = salt;
        this.password = password;

        this.location = location;

        this.name = lockName;

        ableToAccess = players;
    }

    protected HashSet<Location<World>> getLocations() {
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

    public void changePassword(String password) {
        this.password = password;
    }

    protected void addAccess(UUID uuid) {
        ableToAccess.add(uuid);
    }

    protected void removeAccess(UUID uuid) {
        ableToAccess.remove(uuid);
    }

    protected HashSet<UUID> getAbleToAccess() {
        return ableToAccess;
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

    public Optional<Location<World>> getFirstLocation() {
        Optional<Location<World>> location = Optional.empty();
        Iterator<Location<World>> itr = this.location.iterator();

        if(itr.hasNext()) {
            return Optional.of(itr.next());
        }
        return location;
    }

    public void setSalt(byte[] salt) {
        this.salt = salt;
    }

    public byte[] getSalt() {
        return this.salt;
    }
}
