/*
 * This file is part of Latch, licensed under the MIT License.
 *
 * Copyright (c) 2016-2018 IchorPowered <https://github.com/IchorPowered>
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

package com.meronat.latch.interactions;

import com.meronat.latch.Latch;
import com.meronat.latch.entities.Lock;
import com.meronat.latch.enums.LockType;
import com.meronat.latch.utils.LatchUtils;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

public class ChangeLockInteraction implements LockInteraction {

    private final UUID player;

    @Nullable private LockType type;
    @Nullable private String password;
    @Nullable private String lockName;
    @Nullable private UUID newOwner;
    @Nullable private Collection<UUID> membersToAdd;
    @Nullable private Collection<UUID> membersToRemove;
    @Nullable private Boolean protectFromRedstone;

    private boolean persisting = false;

    public ChangeLockInteraction(UUID player) {
        this.player = player;
    }

    public void setType(LockType type) {
        this.type = type;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setLockName(String lockName) {
        this.lockName = lockName;
    }

    public void setNewOwner(UUID newOwner) {
        this.newOwner = newOwner;
    }

    public void setMembersToAdd(Collection<UUID> members) {
        this.membersToAdd = members;
    }

    public void setMembersToRemove(Collection<UUID> members) {
        this.membersToRemove = members;
    }

    public void setProtectFromRedstone(Boolean protectFromRedstone) {
        this.protectFromRedstone = protectFromRedstone;
    }

    @Override
    public boolean handleInteraction(Player player, Location<World> location, BlockSnapshot blockState) {
        final Optional<Lock> optionalLock = Latch.getLockManager().getLock(location);
        //Check to see if another lock is present
        if (!optionalLock.isPresent()) {
            player.sendMessage(Text.of(TextColors.RED, "There is no lock there."));
            return false;
        }

        final Lock lock = optionalLock.get();

        //Check to make sure they're the owner
        if (!lock.isOwnerOrBypassing(player.getUniqueId())) {
            player.sendMessage(Text.of(TextColors.RED, "You are not the owner of this lock."));
            return false;
        }

        //Check to make sure, if they're assigning a new owner, the new owner is not at their limit
        if (newOwner != null || type != null) {
            if (Latch.getLockManager().isPlayerAtLockLimit(newOwner == null ? lock.getOwner() : newOwner, type == null ? lock.getLockType() : type)) {
                player.sendMessage(Text.of(TextColors.RED, "You cannot change this lock due to lock limits."));
                return false;
            }
        }

        final UUID originalOwner = lock.getOwner();
        final String originalName = lock.getName();

        if (this.lockName != null) {
            if (this.lockName.equalsIgnoreCase(originalName)) {
                player.sendMessage(Text.of(TextColors.RED, "This lock already has this name."));
                return false;
            }
            if (!Latch.getLockManager().isUniqueName(this.player, this.lockName)) {
                player.sendMessage(Text.of(TextColors.RED, "This lock name is already taken."));
                return false;
            }
            lock.setName(this.lockName);
        }

        if (this.type != null) {
            lock.setType(type);
        }
        if (this.password != null) {
            lock.setSalt(LatchUtils.generateSalt());
            lock.changePassword(LatchUtils.hashPassword(this.password, lock.getSalt()));

            //If changing password, need to clear out ability to access
            Latch.getLockManager().removeAllLockAccess(lock);
        }
        if (this.newOwner != null) {
            //If assigning to a new owner - need to validate the name
            if (!Latch.getLockManager().isUniqueName(this.newOwner, lock.getName())) {
                lock.setName(LatchUtils.getRandomLockName(this.newOwner, lock.getLockedObject()));
            }
            lock.setOwner(this.newOwner);
        }
        if (this.membersToAdd != null) {
            for (UUID u : this.membersToAdd) {
                Latch.getLockManager().addLockAccess(lock, u);
            }
        }
        if (this.membersToRemove != null) {
            for (UUID u : this.membersToRemove) {
                Latch.getLockManager().removeLockAccess(lock, u);
            }
        }
        if (this.protectFromRedstone != null) {
            lock.setProtectFromRedstone(this.protectFromRedstone);
        }

        //Update the base lock elements
        if (this.lockName != null || this.type != null || this.password != null || this.newOwner != null || this.protectFromRedstone != null) {
            Latch.getLockManager().updateLockAttributes(originalOwner, originalName, lock);
        }

        player.sendMessage(Text.of(TextColors.DARK_GREEN, "Lock data has been successfully updated."));

        return true;
    }

    @Override
    public boolean shouldPersist() {
        return this.persisting;
    }

    @Override
    public void setPersistence(boolean persist) {
        this.persisting = persist;
    }

}
