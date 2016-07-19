package com.ichorcommunity.latch.entities;


import com.ichorcommunity.latch.Latch;
import com.ichorcommunity.latch.enums.LockType;
import com.ichorcommunity.latch.interactions.AbstractLockInteraction;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class LockManager {

    private List<String> lockableBlocks = new ArrayList<String>();
    private List<String> restrictedBlocks = new ArrayList<String>();
    private List<String> protectBelowBlocks = new ArrayList<String>();

    private HashMap<UUID, AbstractLockInteraction> interactionData = new HashMap<UUID, AbstractLockInteraction>();

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
}
