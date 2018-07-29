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

package com.meronat.latch.entities;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.meronat.latch.Latch;
import com.meronat.latch.enums.LockType;
import com.meronat.latch.interactions.LockInteraction;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class LockManager {

    private final HashMap<String, Integer> lockLimits = new HashMap<>();
    private final HashMap<UUID, LockInteraction> interactionData = new HashMap<>();

    private final Set<UUID> bypassing = new HashSet<>();

    private Set<String> lockableBlocks = new HashSet<>();
    private Set<String> restrictedBlocks = new HashSet<>();
    private Set<String> protectBelowBlocks = new HashSet<>();

    private boolean protectFromRedstone = false;

    public Optional<Lock> getLock(Location location) {
        return Latch.getStorageHandler().getLockByLocation(location);
    }

    public void createLock(Lock lock) {
        Latch.getStorageHandler().createLock(lock, lock.getLocations(), lock.getAccessors());
    }

    public void deleteLock(Location<World> location, boolean deleteEntireLock) {
        Latch.getStorageHandler().deleteLock(location, deleteEntireLock);
    }

    /*
     * Locks that someone should be able to enter a password and access (or gain perm access to)
     */
    public boolean isPasswordCompatibleLock(Lock lock) {
        //If the lock is one of the two password locks
        return lock.getLockType() == LockType.PASSWORD_ALWAYS || lock.getLockType() == LockType.PASSWORD_ONCE;
    }

    public boolean hasInteractionData(UUID uniqueId) {
        return this.interactionData.containsKey(uniqueId);
    }

    public LockInteraction getInteractionData(UUID uniqueId) {
        return this.interactionData.get(uniqueId);
    }

    public void setInteractionData(UUID uniqueId, LockInteraction lockInteraction) {
        this.interactionData.put(uniqueId, lockInteraction);
    }

    public void setLockableBlocks(List<String> lockableBlocks) {
        this.lockableBlocks = new HashSet<>(lockableBlocks);
    }

    public boolean addLockableBlock(BlockType blockType) {
        this.lockableBlocks.add(blockType.getId());
        return Latch.getConfiguration().setLockableBlocks(ImmutableSet.copyOf(this.lockableBlocks));
    }

    public boolean removeLockableBlock(BlockType blockType) {
        this.lockableBlocks.remove(blockType.getId());
        return Latch.getConfiguration().setLockableBlocks(ImmutableSet.copyOf(this.lockableBlocks));
    }

    public Set<String> getLockableBlocks() {
        return ImmutableSet.copyOf(this.lockableBlocks);
    }

    public void setRestrictedBlocks(List<String> preventAdjacentToLocks) {
        this.restrictedBlocks = new HashSet<>(preventAdjacentToLocks);
    }

    public void setProtectBelowBlocks(List<String> protectBelowBlocks) {
        this.protectBelowBlocks = new HashSet<>(protectBelowBlocks);
    }

    public boolean isRestrictedBlock(BlockType type) {
        return this.restrictedBlocks.contains(type.getId());
    }

    public boolean isLockableBlock(BlockType block) {
        return this.lockableBlocks.contains(block.getId());
    }

    public boolean isProtectBelowBlocks(BlockType block) {
        return this.protectBelowBlocks.contains(block.getId());
    }

    public void removeInteractionData(UUID uniqueId) {
        this.interactionData.remove(uniqueId);
    }

    public void addLockAccess(Lock lock, UUID uniqueId) {
        if (!lock.canAccess(uniqueId)) {
            lock.addAccess(uniqueId);
            Latch.getStorageHandler().addLockAccess(lock, uniqueId);
        }
    }

    public boolean isUniqueName(UUID playerUUID, String lockName) {
        return Latch.getStorageHandler().isUniqueName(playerUUID, lockName);
    }

    public void addLockLocation(Lock lock, Location<World> location) {
        if (!lock.getLocations().contains(location)) {
            Latch.getStorageHandler().addLockLocation(lock, location);
        }
    }

    public void removeAllLockAccess(Lock lock) {
        lock.getAccessors().clear();
        Latch.getStorageHandler().removeAllLockAccess(lock);
    }

    public void removeLockAccess(Lock lock, UUID uniqueId) {
        if (lock.canAccess(uniqueId)) {
            lock.removeAccess(uniqueId);
            Latch.getStorageHandler().removeLockAccess(lock, uniqueId);
        }
    }

    public void updateLockAttributes(UUID originalOwner, String originalName, Lock lock) {
        Latch.getStorageHandler().updateLockAttributes(originalOwner, originalName, lock);
    }

    public List<Lock> getPlayersLocks(UUID uniqueId) {
        return Latch.getStorageHandler().getLocksByOwner(uniqueId);
    }

    public void setLockLimits(HashMap<String, Integer> lockLimits) {
        this.lockLimits.clear();
        for (Map.Entry<String, Integer> limit : lockLimits.entrySet()) {
            //Only add if limit >=0, otherwise no limit
            if (limit.getValue() >= 0) {
                this.lockLimits.put(limit.getKey().toLowerCase(), limit.getValue());
            }
        }
    }

    public boolean isPlayerAtLockLimit(UUID player, LockType type) {
        return Latch.getStorageHandler().isPlayerAtLockLimit(player, type, this.lockLimits);
    }

    public ImmutableMap<String, Integer> getLimits() {
        return ImmutableMap.copyOf(this.lockLimits);
    }

    public boolean getProtectFromRedstone() {
        return this.protectFromRedstone;
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
