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

import java.util.ArrayList;
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
        Optional<Lock> lock = Latch.lockManager.getLock(location);
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

        ArrayList<Lock> locks = new ArrayList<Lock>();
        locks.add(lock.get());

        Optional<Location<World>> optionalOtherBlock = LatchUtils.getDoubleBlockLocation(blockstate);
        Optional<Lock> otherBlockLock = Optional.ofNullable(null);

        //If the block has another block that needs to be unlocked
        if(optionalOtherBlock.isPresent()) {
            otherBlockLock = Latch.lockManager.getLock(optionalOtherBlock.get());
        }
        if(otherBlockLock.isPresent()) {
            if(!otherBlockLock.get().isOwner(player.getUniqueId())) {
                player.sendMessage(Text.of("You're not the owner of the adjacent lock, not modifying it."));
            } else {
                locks.add(otherBlockLock.get());
            }
        }

        //Modify the attributes of the lock
        for(Lock thisLock : locks) {
            if(type != null) {
                thisLock.setType(type);
            }
            if(password != null) {
                thisLock.changePassword(LatchUtils.hashPassword(password, thisLock.getLocation()));
            }
            if(lockName != null) {
                thisLock.setName(lockName);
            }
            if(newOwner != null) {
                thisLock.setOwner(newOwner);
            }
            if(membersToAdd != null) {
                for(User user : membersToAdd) {
                    thisLock.addAccess(user.getUniqueId());
                }
            }
            if(membersToRemove != null) {
                for(User user : membersToRemove) {
                    thisLock.removeAccess(user.getUniqueId());
                }
            }
        }

        player.sendMessage(Text.of("Lock data has been updated."));
        return true;
    }

    @Override
    public boolean shouldPersist() {
        return persisting;
    }

    @Override
    public void setPersistance(boolean persist) {
        this.persisting = persist;
    }
}
