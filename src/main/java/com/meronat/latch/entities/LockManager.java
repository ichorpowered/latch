/*
 * This file is part of Latch, licensed under the MIT License (MIT).
 *
 * Copyright (c) IchorPowered <https://github.com/IchorPowered>
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
import com.meronat.latch.interactions.AbstractLockInteraction;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class LockManager {

    private Set<UUID> bypassing = new HashSet<>();

    private List<String> lockableBlocks = new ArrayList<>();
    private List<String> restrictedBlocks = new ArrayList<>();
    private List<String> protectBelowBlocks = new ArrayList<>();
    private HashMap<String, Integer> lockLimits = new HashMap<>();

    private boolean protectFromRedstone = true;

    private HashMap<UUID, AbstractLockInteraction> interactionData = new HashMap<>();

    public Optional<Lock> getLock(Location location) {
        return Latch.getStorageHandler().getLockByLocation(location);
    }

    public void createLock(Lock lock) {
        Latch.getStorageHandler().createLock(lock, lock.getLocations(), lock.getAbleToAccess());
    }

    public void deleteLock(Location<World> location, boolean deleteEntireLock) {
        Latch.getStorageHandler().deleteLock(location, deleteEntireLock);
    }

    /*
     * Locks that someone should be able to enter a password and access (or gain perm access to)
     */
    public boolean isPasswordCompatibleLock(Lock lock) {
        //If the lock is one of the two password locks, or a donation lock with a password
        return lock.getLockType() == LockType.PASSWORD_ALWAYS ||
                lock.getLockType() == LockType.PASSWORD_ONCE /*||
                (lock.getLockType() == LockType.DONATION && lock.getPassword().length() > 0)*/;
    }

    public boolean hasInteractionData(UUID uniqueId) {
        return interactionData.containsKey(uniqueId);
    }

    public AbstractLockInteraction getInteractionData(UUID uniqueId) {
        return interactionData.get(uniqueId);
    }

    public void setInteractionData(UUID uniqueId, AbstractLockInteraction lockInteraction) {
        interactionData.put(uniqueId, lockInteraction);
    }

    public void setLockableBlocks(List<String> lockableBlocks) {
        this.lockableBlocks = lockableBlocks;
    }

    public void setRestrictedBlocks(List<String> preventAdjacentToLocks) {
        this.restrictedBlocks = preventAdjacentToLocks;
    }

    public void setProtectBelowBlocks(List<String> protectBelowBlocks) {
        this.protectBelowBlocks = protectBelowBlocks;
    }

    public boolean isRestrictedBlock(BlockType type) {
        return restrictedBlocks.contains(type.getId());
    }

    public boolean isLockableBlock(BlockType block) {
        return lockableBlocks.contains(block.getId());
    }

    public boolean isProtectBelowBlocks(BlockType block) { return protectBelowBlocks.contains(block.getId()); }

    public void removeInteractionData(UUID uniqueId) {
        interactionData.remove(uniqueId);
    }

    public void addLockAccess(Lock thisLock, UUID uniqueId) {
        if(!thisLock.canAccess(uniqueId)) {
            thisLock.addAccess(uniqueId);
            Latch.getStorageHandler().addLockAccess(thisLock, uniqueId);
        }
    }

    public boolean isUniqueName(UUID playerUUID, String lockName) {
        return Latch.getStorageHandler().isUniqueName(playerUUID, lockName);
    }

    public void addLockLocation(Lock lock, Location<World> location) {
        if(!lock.getLocations().contains(location)) {
            Latch.getStorageHandler().addLockLocation(lock, location);
        }
    }

    public void removeAllLockAccess(Lock thisLock) {
        thisLock.getAbleToAccess().clear();
        Latch.getStorageHandler().removeAllLockAccess(thisLock);
    }

    public void removeLockAccess(Lock thisLock, UUID uniqueId) {
        if(thisLock.canAccess(uniqueId)) {
            thisLock.removeAccess(uniqueId);
            Latch.getStorageHandler().removeLockAccess(thisLock, uniqueId);

        }
    }

    public void updateLockAttributes(UUID originalOwner, String originalName, Lock lock) {
        Latch.getStorageHandler().updateLockAttributes(originalOwner, originalName, lock);
    }

    public List<Lock> getPlayersLocks(UUID uniqueId) {
        return Latch.getStorageHandler().getLocksByOwner(uniqueId);
    }

    public void setLockLimits(HashMap<String,Integer> lockLimits) {
        this.lockLimits.clear();
        for(Map.Entry<String, Integer> limit : lockLimits.entrySet()) {
            //Only add if limit >=0, otherwise no limit
            if(limit.getValue() >= 0) {
                this.lockLimits.put(limit.getKey().toLowerCase(), limit.getValue());
            }
        }
    }

    public boolean isPlayerAtLockLimit(UUID player, LockType type) {
        return Latch.getStorageHandler().isPlayerAtLockLimit(player, type, lockLimits);
    }

    public boolean getProtectFromRedstone() {
        return protectFromRedstone;
    }

    public void setProtectFromRedstone(boolean protectFromRedstone) {
        this.protectFromRedstone = protectFromRedstone;
    }

    public boolean isBypassing(UUID uuid) {

        return this.bypassing.contains(uuid);

    }

    public void setBypassing(UUID uuid) {

        this.bypassing.add(uuid);

    }

    public void removeBypassing(UUID uuid) {

        this.bypassing.remove(uuid);

    }

}
