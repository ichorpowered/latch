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

package com.meronat.latch.entities;

import com.meronat.latch.Latch;
import com.meronat.latch.enums.LockType;
import com.meronat.latch.utils.LatchUtils;
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

    private HashSet<Location<World>> location = new HashSet<>();

    private String lockedObjectName;

    private byte[] salt = new byte[8];
    private String password = "";
    private HashSet<UUID> ableToAccess;
    private boolean protectFromRedstone;

    public Lock(UUID owner, LockType type, HashSet<Location<World>> location, String lockedObjectName, byte[] salt, String password, boolean protectFromRedstone) {
        this(owner, LatchUtils.getRandomLockName(owner, lockedObjectName), type, location, lockedObjectName, salt, password, new HashSet<>(), protectFromRedstone);
    }

    public Lock(UUID owner, String lockName, LockType type, HashSet<Location<World>> location, String lockedObjectName, byte[] salt, String password, HashSet<UUID> players, boolean protectFromRedstone) {

        this.owner = owner;
        this.type = type;
        this.lockedObjectName = lockedObjectName;

        this.salt = salt;
        this.password = password;

        this.location = location;

        this.name = lockName;

        ableToAccess = players;

        this.protectFromRedstone = protectFromRedstone;
    }

    public Lock(UUID owner, LockType type, HashSet<Location<World>> location, String lockedObjectName, boolean protectFromRedstone) {

        this.owner = owner;
        this.name = LatchUtils.getRandomLockName(owner, lockedObjectName);
        this.type = type;
        this.lockedObjectName = lockedObjectName;
        this.location = location;
        this.protectFromRedstone = protectFromRedstone;
        this.ableToAccess = new HashSet<>();

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

    public boolean isOwnerOrBypassing(UUID uniqueId) {
        return this.owner.equals(uniqueId) || Latch.getLockManager().isBypassing(uniqueId);
    }

    public boolean isOwner(UUID uuid) {
        return this.owner.equals(uuid);
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
        List<String> names = new ArrayList<>();

        Optional<UserStorageService> userStorageService = Sponge.getGame().getServiceManager().provide(UserStorageService.class);

        if(userStorageService.isPresent()) {
            for(UUID uuid : ableToAccess) {
                userStorageService.get().get(uuid).ifPresent(u -> names.add(u.getName()));
            }
        }

        return names;
    }

    public boolean canAccess(UUID uniqueId) {
        return ableToAccess.contains(uniqueId) || owner.equals(uniqueId) || type == LockType.PUBLIC || Latch.getLockManager().isBypassing(uniqueId);
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

    public void setProtectFromRedstone(boolean protectFromRedstone) {
        this.protectFromRedstone = protectFromRedstone;
    }

    public boolean getProtectFromRedstone() {
        return protectFromRedstone;
    }
}
