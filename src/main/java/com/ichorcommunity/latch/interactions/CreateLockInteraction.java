package com.ichorcommunity.latch.interactions;

import com.ichorcommunity.latch.Latch;
import com.ichorcommunity.latch.entities.Lock;
import com.ichorcommunity.latch.enums.LockType;
import com.ichorcommunity.latch.events.LockCreateEvent;
import com.ichorcommunity.latch.utils.LatchUtils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

public class CreateLockInteraction implements AbstractLockInteraction {

    private final UUID player;
    private final LockType type;
    private final String password;

    private boolean persisting = false;

    public CreateLockInteraction(UUID player, LockType type, String password) {
        this.player = player;
        this.type = type;
        this.password = password;
    }

    @Override
    public boolean handleInteraction(Player player, Location<World> location, BlockSnapshot blockstate) {
        //Check to see if another lock is present
        if(Latch.getLockManager().getLock(location).isPresent()) {
            player.sendMessage(Text.of("There is already a lock here."));
            return false;
        }

        //Make sure it's a lockable block
        if(!Latch.getLockManager().isLockableBlock(blockstate.getState().getType())) {
            player.sendMessage(Text.of("That is not a lockable block: " + blockstate.getState().getType()));
            return false;
        }

        Optional<Location<World>> optionalOtherBlock = LatchUtils.getDoubleBlockLocation(blockstate);

        HashSet<Location<World>> lockLocations = new HashSet<Location<World>>();
        lockLocations.add(location);

        //If the block has another block that needs to be locked
        if(optionalOtherBlock.isPresent()) {
            //Check to see if another lock is present
            Optional<Lock> otherLock = Latch.getLockManager().getLock(optionalOtherBlock.get());
            if( otherLock.isPresent() && !otherLock.get().isOwner(player.getUniqueId()) ) {
                //Shouldn't happen if we've configured this correctly - but just in case...
                player.sendMessage(Text.of("Another lock already present on the double block - delete locks and try again."));
                return false;
            }
            lockLocations.add(optionalOtherBlock.get());
        }

        if(Latch.getLockManager().isPlayerAtLockLimit(player.getUniqueId(), type)) {
            player.sendMessage(Text.of("You have reached the limit for locks."));
            return false;
        }

        //Fire the lock create event and create the lock if it's not cancelled (by other plugins)
        byte[] salt = LatchUtils.generateSalt();

        LockCreateEvent lockCreateEvent = new LockCreateEvent(player,
                new Lock(player.getUniqueId(), type, lockLocations, LatchUtils.getBlockNameFromType(blockstate.getState().getType()), salt, LatchUtils.hashPassword(password, salt)),
                Cause.source(player).build());

        Sponge.getEventManager().post(lockCreateEvent);

        //Stop if original locking event or other block locking event is cancelled
        if (lockCreateEvent.isCancelled() ) {
            return false;
        }

        //Notify the player
        player.sendMessage(Text.of("You have created a " + lockCreateEvent.getLock().getLockType() + " lock, " + lockCreateEvent.getLock().getName()));
        Latch.getLockManager().createLock(lockCreateEvent.getLock());

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
