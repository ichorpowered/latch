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

import java.time.LocalDateTime;
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

    private LocalDateTime lastAccessed;

    public Lock(UUID owner, LockType type, HashSet<Location<World>> location, String lockedObjectName, byte[] salt, String password, boolean protectFromRedstone, LocalDateTime lastAccessed) {
        this(owner, LatchUtils.getRandomLockName(owner, lockedObjectName), type, location, lockedObjectName, salt, password, new HashSet<>(), protectFromRedstone, lastAccessed);
    }

    public Lock(UUID owner, String lockName, LockType type, HashSet<Location<World>> location, String lockedObjectName, byte[] salt, String password, HashSet<UUID> players, boolean protectFromRedstone, LocalDateTime lastAccessed) {

        this.owner = owner;
        this.type = type;
        this.lockedObjectName = lockedObjectName;

        this.salt = salt;
        this.password = password;

        this.location = location;

        this.name = lockName;

        ableToAccess = players;

        this.protectFromRedstone = protectFromRedstone;

        this.lastAccessed = lastAccessed;
    }

    public Lock(UUID owner, LockType type, HashSet<Location<World>> location, String lockedObjectName, boolean protectFromRedstone, LocalDateTime lastAccessed) {

        this.owner = owner;
        this.name = LatchUtils.getRandomLockName(owner, lockedObjectName);
        this.type = type;
        this.lockedObjectName = lockedObjectName;
        this.location = location;
        this.protectFromRedstone = protectFromRedstone;
        this.ableToAccess = new HashSet<>();
        this.lastAccessed = lastAccessed;

    }

    // TODO This is super messy. Let's switch to a builder pattern once we add the flag table.

    public Lock(UUID owner, LockType type, HashSet<Location<World>> location, String lockedObjectName, boolean protectFromRedstone, LocalDateTime lastAccessed, String name) {
        this.owner = owner;
        this.name = name;
        this.type = type;
        this.lockedObjectName = lockedObjectName;
        this.location = location;
        this.protectFromRedstone = protectFromRedstone;
        this.ableToAccess = new HashSet<>();
        this.lastAccessed = lastAccessed;
    }

    protected HashSet<Location<World>> getLocations() {
        return this.location;
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
        return this.lockedObjectName;
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
        this.ableToAccess.add(uuid);
    }

    protected void removeAccess(UUID uuid) {
        this.ableToAccess.remove(uuid);
    }

    protected HashSet<UUID> getAbleToAccess() {
        return this.ableToAccess;
    }

    public List<String> getAbleToAccessNames() {
        List<String> names = new ArrayList<>();

        Optional<UserStorageService> userStorageService = Sponge.getGame().getServiceManager().provide(UserStorageService.class);

        if(userStorageService.isPresent()) {
            for(UUID uuid : this.ableToAccess) {
                userStorageService.get().get(uuid).ifPresent(u -> names.add(u.getName()));
            }
        }

        return names;
    }

    public boolean canAccess(UUID uniqueId) {
        return this.ableToAccess.contains(uniqueId) || this.owner.equals(uniqueId) || this.type == LockType.PUBLIC || Latch.getLockManager().isBypassing(uniqueId);
    }

    public String getPassword() {
        return this.password;
    }

    public String getOwnerName() {
        Optional<UserStorageService> userStorageService = Sponge.getGame().getServiceManager().provide(UserStorageService.class);

        if(userStorageService.isPresent()) {
            Optional<User> user = userStorageService.get().get(this.owner);
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
        return this.protectFromRedstone;
    }

    public LocalDateTime getLastAccessed() {
        return this.lastAccessed;
    }

    public void updateLastAccessed() {
        this.lastAccessed = LocalDateTime.now();
        Sponge.getScheduler().createAsyncExecutor(Latch.getPluginContainer()).execute(() -> Latch.getLockManager().updateLockAttributes(this.owner, this.name, this));
    }
}
