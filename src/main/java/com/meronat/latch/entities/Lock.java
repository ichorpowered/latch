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

import static com.google.common.base.Preconditions.checkNotNull;

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
import java.util.Set;
import java.util.UUID;

public class Lock {

    private Set<Location<World>> locations;
    private Set<UUID> accessors;

    private byte[] salt;

    private UUID owner;
    private LockType type;
    private String name;
    private String objectName;
    private String password;
    private LocalDateTime lastAccessed;

    private boolean protectFromRedstone;

    private Lock(UUID owner, LockType type, String name, String objectName, String password, byte[] salt, Set<Location<World>> locations,
        Set<UUID> accessors, LocalDateTime lastAccessed, boolean protectFromRedstone) {
        this.owner = owner;
        this.type = type;
        this.name = name;
        this.objectName = objectName;
        this.password = password;
        this.salt = salt;
        this.locations = locations;
        this.accessors = accessors;
        this.lastAccessed = lastAccessed;
        this.protectFromRedstone = protectFromRedstone;
    }

    public Set<Location<World>> getLocations() {
        return this.locations;
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
        return this.objectName;
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

    public void addAccess(UUID uuid) {
        this.accessors.add(uuid);
    }

    public void removeAccess(UUID uuid) {
        this.accessors.remove(uuid);
    }

    public Set<UUID> getAccessors() {
        return this.accessors;
    }

    public List<String> getAbleToAccessNames() {
        List<String> names = new ArrayList<>();

        Optional<UserStorageService> userStorageService = Sponge.getGame().getServiceManager().provide(UserStorageService.class);

        if (userStorageService.isPresent()) {
            for (UUID uuid : this.accessors) {
                userStorageService.get().get(uuid).ifPresent(u -> names.add(u.getName()));
            }
        }

        return names;
    }

    public boolean canAccess(UUID uniqueId) {
        return this.accessors.contains(uniqueId) || this.owner.equals(uniqueId) || this.type == LockType.PUBLIC || Latch.getLockManager()
            .isBypassing(uniqueId);
    }

    public String getPassword() {
        return this.password;
    }

    public String getOwnerName() {
        Optional<UserStorageService> userStorageService = Sponge.getGame().getServiceManager().provide(UserStorageService.class);

        if (userStorageService.isPresent()) {
            Optional<User> user = userStorageService.get().get(this.owner);
            if (user.isPresent()) {
                return user.get().getName();
            }
        }
        return "(owner name not found)";
    }

    public Optional<Location<World>> getFirstLocation() {
        Optional<Location<World>> location = Optional.empty();
        Iterator<Location<World>> itr = this.locations.iterator();

        if (itr.hasNext()) {
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
        Sponge.getScheduler().createAsyncExecutor(Latch.getPluginContainer())
            .execute(() -> Latch.getLockManager().updateLockAttributes(this.owner, this.name, this));
    }

    public static class Builder {

        private Set<Location<World>> locations;
        private Set<UUID> accessors;

        private byte[] salt;

        private UUID owner;
        private LockType type;
        private String name;
        private String objectName;
        private String password;
        private LocalDateTime lastAccessed;

        private boolean protectedFromRedstone;

        private Builder() {}

        public Lock.Builder owner(UUID owner) {
            this.owner = owner;

            return this;
        }

        public Lock.Builder type(LockType type) {
            this.type = type;

            return this;
        }

        public Lock.Builder name(String name) {
            this.name = name;

            return this;
        }

        public Lock.Builder objectName(String name) {
            this.objectName = name;

            return this;
        }

        public Lock.Builder password(String password) {
            this.password = password;

            return this;
        }

        public Lock.Builder lastAccessed(LocalDateTime lastAccessed) {
            this.lastAccessed = lastAccessed;

            return this;
        }

        public Lock.Builder protectFromRedstone(boolean protect) {
            this.protectedFromRedstone = protect;

            return this;
        }

        public Lock.Builder salt(byte[] salt) {
            this.salt = salt;

            return this;
        }

        public Lock.Builder accessors(Set<UUID> accessors) {
            this.accessors = accessors;

            return this;
        }

        public Lock.Builder locations(Set<Location<World>> locations) {
            this.locations = locations;

            return this;
        }

        public Lock build() {
            checkNotNull(this.owner, "You must specify an owner for the lock.");
            checkNotNull(this.type, "You must specify the type of lock.");
            if (this.locations == null) {
                this.locations = new HashSet<>();
            }
            if (this.accessors == null) {
                this.accessors = new HashSet<>();
            }
            if (this.salt == null) {
                this.salt = new byte[8];
            }
            if (this.objectName == null) {
                this.objectName = "unknown";
            }
            if (this.name == null) {
                this.name = LatchUtils.getRandomLockName(this.owner, this.objectName);
            }
            if (this.password == null) {
                this.password = "";
            }
            if (this.lastAccessed == null) {
                this.lastAccessed = LocalDateTime.now();
            }

            return new Lock(
                this.owner,
                this.type,
                this.name,
                this.objectName,
                this.password,
                this.salt,
                this.locations,
                this.accessors,
                this.lastAccessed,
                this.protectedFromRedstone);
        }

    }

    public static Lock.Builder builder() {
        return new Lock.Builder();
    }

}
