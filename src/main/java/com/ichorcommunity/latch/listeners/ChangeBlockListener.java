package com.ichorcommunity.latch.listeners;

import com.ichorcommunity.latch.Latch;
import com.ichorcommunity.latch.entities.Lock;
import com.ichorcommunity.latch.interactions.AbstractLockInteraction;
import com.ichorcommunity.latch.utils.LatchUtils;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.explosive.Explosive;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Optional;

public class ChangeBlockListener {

    @Listener
    public void validateBlockPlacement(ChangeBlockEvent.Place event) {
        //Did a player cause this... if so use them for owner checks
        Optional<Player> player = event.getCause().last(Player.class);

        //For each of the lockable blocks... make sure we're not impacting another lock the player is not the owner of
        for( Transaction<BlockSnapshot> bs : event.getTransactions()) {
            if (bs.isValid() && bs.getFinal().getLocation().isPresent()) {

                //If the block is a restricted block, make sure it's not being placed near a lock
                if( Latch.lockManager.isRestrictedBlock(bs.getFinal().getState().getType())) {
                    for(Lock lock : LatchUtils.getAdjacentLocks(bs.getFinal().getLocation().get())) {
                        //If there is a player and they aren't the owner, OR there's no player, invalidate
                        if( (!player.isPresent() || (player.isPresent() && !lock.isOwner(player.get().getUniqueId()))) ) {
                            bs.setValid(false);
                            if(player.isPresent()) {
                                player.get().sendMessage(Text.of("You can't place that type of block near a lock you don't own."));
                            }
                            break;
                        }
                    }
                }

                //If the block is a lockable block, make sure it's not connecting with someone else's lock
                if( Latch.lockManager.isLockableBlock(bs.getFinal().getState().getType())) {
                    Optional<Location<World>> optionalOtherBlock = LatchUtils.getDoubleBlockLocation(bs.getFinal());
                    Optional<Lock> otherBlockLock = Optional.ofNullable(null);

                    //If the block has another block that needs to be unlocked
                    if(optionalOtherBlock.isPresent()) {
                        otherBlockLock = Latch.lockManager.getLock(optionalOtherBlock.get());
                    }
                    if(otherBlockLock.isPresent()) {
                        if( (!player.isPresent() || (player.isPresent() && !otherBlockLock.get().isOwner(player.get().getUniqueId()))) ) {
                            bs.setValid(false);
                            if(player.isPresent()) {
                                player.get().sendMessage(Text.of("You can't place that type of block near a lock you don't own."));
                            }
                            break;
                        }
                    }
                }

            }
        }

        if(!player.isPresent()) {
            return;
        }

        //If the player has interaction data
        if(Latch.lockManager.hasInteractionData(player.get().getUniqueId())) {

            AbstractLockInteraction lockInteraction = Latch.lockManager.getInteractionData(player.get().getUniqueId());

            //Check all of the blocks and apply the interaction
            //Could cancel the block placement here if it fails -- but Meronat decided no
            for( Transaction<BlockSnapshot> bs : event.getTransactions()) {
                if (bs.isValid() && bs.getFinal().getLocation().isPresent()) {

                    lockInteraction.handleInteraction(player.get(), bs.getFinal().getLocation().get(), bs.getFinal());

                }
            }

            if(!lockInteraction.shouldPersist()) {
                Latch.lockManager.removeInteractionData(player.get().getUniqueId());
            }
        }
    }

    @Listener
    public void onBreakBlockByPlayer(ChangeBlockEvent.Break event, @Root Player player) {
        //Only allow the owner to break a lock
        for( Transaction<BlockSnapshot> bs : event.getTransactions()) {
            if (bs.isValid() && bs.getOriginal().getLocation().isPresent()) {
                Optional<Lock> lock = Latch.lockManager.getLock(bs.getOriginal().getLocation().get());
                //If lock is present and the player is NOT the owner
                if(lock.isPresent() && !lock.get().isOwner(player.getUniqueId())) {
                    bs.setValid(false);
                }
            }
        }
    }

    @Listener
    public void onBlockBrokenTileEntity(ChangeBlockEvent.Post event) {
        //Player and explosion listeners handled elsewhere - this is for other events (like pistons)
        //Can't explicitly look for pistons in the cause because if blocks are chained the piston is not in the cause
        if(event.getCause().root() instanceof Player || event.getCause().first(Explosive.class).isPresent()) {
            return;
        }

        //If the post event is affecting a lock... invalidate the new blocksnapshot
        for( Transaction<BlockSnapshot> bs : event.getTransactions()) {
            if(bs.getOriginal().getLocation().isPresent() && Latch.lockManager.isLockableBlock(bs.getOriginal().getState().getType())) {
                if(Latch.lockManager.getLock(bs.getOriginal().getLocation().get()).isPresent()) {
                    bs.setValid(false);
                }
            }
        }
    }

    @Listener
    public void onBlockBrokenByExplosion(ChangeBlockEvent.Break event) {
        //If there's a lock broken by an explosion, need to evaluate it
        boolean protectFromExplosives = event.getCause().first(Explosive.class).isPresent() && Latch.getConfig().getNode("protect_from_explosives").getBoolean(true);

        if(!protectFromExplosives) {
            return;
        }

        for( Transaction<BlockSnapshot> bs : event.getTransactions()) {
            if (bs.isValid() && bs.getOriginal().getLocation().isPresent()) {

                if(Latch.lockManager.getLock(bs.getOriginal().getLocation().get()).isPresent()) {
                    bs.setValid(false);
                }
            }
        }

    }


}
