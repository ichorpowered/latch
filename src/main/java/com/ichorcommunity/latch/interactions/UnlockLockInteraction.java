package com.ichorcommunity.latch.interactions;

import com.ichorcommunity.latch.Latch;
import com.ichorcommunity.latch.entities.Lock;
import com.ichorcommunity.latch.enums.LockType;
import com.ichorcommunity.latch.utils.LatchUtils;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

public class UnlockLockInteraction implements AbstractLockInteraction {

    private final UUID player;

    private boolean persisting = false;
    private final String password;

    public UnlockLockInteraction(UUID player, String password) {
        this.player = player;
        this.password = password;
    }

    @Override
    public boolean handleInteraction(Player player, Location<World> location, BlockSnapshot blockstate) {
        Optional<Lock> lock = Latch.getLockManager().getLock(location);
        //Check to see if another lock is present
        if(!lock.isPresent()) {
            player.sendMessage(Text.of("There is no lock there."));
            return false;
        }

        //If they're the owner or on the list of players within the lock, allow
        if(lock.get().canAccess(player.getUniqueId())) {
            return true;
        }

        //Check the password
        if(Latch.getLockManager().isPasswordCompatibleLock(lock.get())) {
            if( !LatchUtils.hashPassword(password, location).equals(lock.get().getPassword())) {
                player.sendMessage(Text.of("The password you tried is incorrect."));
                return false;
            }

            //If the password is correct we're returning true - but if it's a PASSWORD_ONCE need to add them to allowed members
            if(lock.get().getLockType() == LockType.PASSWORD_ONCE) {
                //Check for other locks
                ArrayList<Lock> locks = new ArrayList<Lock>();
                locks.add(lock.get());

                Optional<Location<World>> optionalOtherBlock = LatchUtils.getDoubleBlockLocation(blockstate);
                Optional<Lock> otherBlockLock = Optional.ofNullable(null);

                //If the block has another block that needs to be unlocked
                if(optionalOtherBlock.isPresent()) {
                    otherBlockLock = Optional.ofNullable(Latch.getLockManager().getLock(optionalOtherBlock.get()).get());
                }
                if(otherBlockLock.isPresent()) {
                    if(!otherBlockLock.get().getPassword().equalsIgnoreCase(password)) {
                        player.sendMessage(Text.of("The adjacent lock does not have the same password."));
                    } else {
                        locks.add(otherBlockLock.get());
                    }
                }

                //Modify the attributes of the lock
                for(Lock thisLock : locks) {
                    Latch.getLockManager().addLockAccess(thisLock, player.getUniqueId());
                }
                player.sendMessage(Text.of("Unlocking the password lock for future access."));
            }
            return true;
        }
        //Default state
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
