package com.ichorcommunity.latch.entities;


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

    private HashMap<Location, Lock> locksByLocation = new HashMap<Location, Lock>();

    private HashMap<UUID, AbstractLockInteraction> interactionData = new HashMap<UUID, AbstractLockInteraction>();

    public Optional<Lock> getLock(Location location) {
        return Optional.ofNullable(locksByLocation.get(location));
    }

    public void createLock(Lock lock) {
        locksByLocation.put(lock.getLocation(), lock);
    }

    public void deleteLock(Location<World> location) {
        locksByLocation.remove(location);
    }

    /*
     * Locks that someone should be able to enter a password and access (or gain perm access to)
     */
    public boolean isPasswordCompatibleLock(Lock lock) {
        //If the lock is one of the two password locks, or a donation lock with a password
        return lock.getLockType() == LockType.PASSWORD_ALWAYS ||
                lock.getLockType() == LockType.PASSWORD_ONCE ||
                (lock.getLockType() == LockType.DONATION && lock.getPassword().length() > 0);
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
}
