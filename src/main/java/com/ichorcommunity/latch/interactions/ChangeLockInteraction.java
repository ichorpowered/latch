/*
 * This file is part of Latch, licensed under the MIT License (MIT).
 *
 * Copyright (c) Ichor Community <http://www.ichorcommunity.com>
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

package com.ichorcommunity.latch.interactions;

import com.ichorcommunity.latch.Latch;
import com.ichorcommunity.latch.entities.Lock;
import com.ichorcommunity.latch.enums.LockType;
import com.ichorcommunity.latch.utils.LatchUtils;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public class ChangeLockInteraction implements AbstractLockInteraction {

    private final UUID player;

    private LockType type;
    private String password;
    private String lockName;
    private UUID newOwner;
    private Collection<User> membersToAdd;
    private Collection<User> membersToRemove;

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

    public void setMembersToAdd(Collection<User> members) {
        this.membersToAdd = members;
    }

    public void setMembersToRemove(Collection<User> members) {
        this.membersToRemove = members;
    }


    @Override
    public boolean handleInteraction(Player player, Location<World> location, BlockSnapshot blockstate) {
        Optional<Lock> lock = Latch.getLockManager().getLock(location);
        //Check to see if another lock is present
        if(!lock.isPresent()) {
            player.sendMessage(Text.of("There is no lock there."));
            return false;
        }

        //Check to make sure they're the owner
        if(!lock.get().isOwner(player.getUniqueId())) {
            player.sendMessage(Text.of("You're not the owner of this lock."));
            return false;
        }

        //Check to make sure, if they're assigning a new owner, the new owner is not at their limit
        if(Latch.getLockManager().isPlayerAtLockLimit(newOwner == null ? lock.get().getOwner() : newOwner, type == null ? lock.get().getLockType() : type)) {
            player.sendMessage(Text.of("You can't give a player a lock that would put them over the lock limit."));
            return false;
        }

        UUID originalOwner = lock.get().getOwner();
        String originalName = lock.get().getName();

        if(type != null) {
            lock.get().setType(type);
        }
        if(password != null) {
            lock.get().setSalt(LatchUtils.generateSalt());
            lock.get().changePassword(LatchUtils.hashPassword(password, lock.get().getSalt()));

            //If changing password, need to clear out ability to access
            Latch.getLockManager().removeAllLockAccess(lock.get());
        }
        if(lockName != null) {
            lock.get().setName(lockName);
        }
        if(newOwner != null) {
            //If assigning to a new owner - need to validate the name
            if(!Latch.getLockManager().isUniqueName(newOwner, lock.get().getName())) {
                lock.get().setName(LatchUtils.getRandomLockName(newOwner, lock.get().getLockedObject()));
            }
            lock.get().setOwner(newOwner);
        }
        if(membersToAdd != null) {
            for(User user : membersToAdd) {
                Latch.getLockManager().addLockAccess(lock.get(), user.getUniqueId());
            }
        }
        if(membersToRemove != null) {
            for(User user : membersToRemove) {
                Latch.getLockManager().removeLockAccess(lock.get(), user.getUniqueId());
            }
        }

        //Update the base lock elements
        if(lockName != null || type != null || password != null || newOwner != null) {
            Latch.getLockManager().updateLockAttributes(originalOwner, originalName, lock.get());
        }

        player.sendMessage(Text.of("Lock data has been updated."));
        return true;
    }

    @Override
    public boolean shouldPersist() {
        return persisting;
    }

    @Override
    public void setPersistence(boolean persist) {
        this.persisting = persist;
    }
}
