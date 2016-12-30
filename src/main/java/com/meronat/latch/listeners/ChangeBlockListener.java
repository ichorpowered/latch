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

package com.meronat.latch.listeners;

import com.meronat.latch.Latch;
import com.meronat.latch.entities.Lock;
import com.meronat.latch.interactions.AbstractLockInteraction;
import com.meronat.latch.utils.LatchUtils;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.data.property.block.MatterProperty;
import org.spongepowered.api.entity.explosive.Explosive;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.chat.ChatTypes;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Optional;

public class ChangeBlockListener {

    @Listener
    public void validateBlockPlacement(ChangeBlockEvent.Place event) {
        //Did a player cause this... if so use them for owner checks
        Optional<Player> player = event.getCause().last(Player.class);

        //Get a player's lock interaction (if present)
        Optional<AbstractLockInteraction> lockInteraction = player.map(p -> Latch.getLockManager().getInteractionData(p.getUniqueId()));

        //Variable to see if a lock is created -- this way we don't duplicate messages if a lock gets expanded (like placing a door)
        boolean interactionSuccessful = false;

        //For each of the lockable blocks...
        for( Transaction<BlockSnapshot> bs : event.getTransactions()) {
            if (bs.isValid() && bs.getFinal().getLocation().isPresent()) {
                //If the block is a restricted block, make sure it's not being placed near a lock
                if( Latch.getLockManager().isRestrictedBlock(bs.getFinal().getState().getType())) {
                    for(Lock lock : LatchUtils.getAdjacentLocks(bs.getFinal().getLocation().get())) {
                        //If there is a player and they aren't the owner, OR there's no player, invalidate
                        if( (!player.isPresent() || (player.isPresent() && !lock.isOwner(player.get().getUniqueId()))) ) {
                            bs.setValid(false);
                            event.setCancelled(true);
                            player.ifPresent(p -> p.sendMessage(Text.of(TextColors.RED, "You can't place that type of block near a lock you don't own.")));
                        }
                    }
                }

                //If the block is a lockable block, make sure it's not connecting with someone else's lock
                if( Latch.getLockManager().isLockableBlock(bs.getFinal().getState().getType())) {
                    Optional<Location<World>> optionalOtherBlock = LatchUtils.getDoubleBlockLocation(bs.getFinal());
                    Optional<Lock> otherBlockLock = Optional.empty();

                    //If the block has another block that needs to be unlocked
                    if(optionalOtherBlock.isPresent()) {
                        otherBlockLock = Latch.getLockManager().getLock(optionalOtherBlock.get());
                    }
                    if(otherBlockLock.isPresent()) {
                        if(player.isPresent() && otherBlockLock.get().isOwner(player.get().getUniqueId())) {
                            Latch.getLockManager().addLockLocation(otherBlockLock.get(), bs.getFinal().getLocation().get());
                            if (!interactionSuccessful) {
                                player.get().sendMessage(ChatTypes.ACTION_BAR, Text.of(TextColors.DARK_GREEN,
                                        "You have expanded your " + otherBlockLock.get().getName() + " lock."));
                            }
                            continue; //break to allow expanding locks while having a command persisted
                        } else {
                            bs.setValid(false);
                            event.setCancelled(true);
                            player.ifPresent(p -> p.sendMessage(Text.of(TextColors.RED, "You can't place that type of block near a lock you don't own.")));
                            continue;
                        }
                    }
                }

                //If the player has interaction data
                if(lockInteraction.isPresent()) {
                    //Check all of the blocks and apply the interaction
                    //Could cancel the block placement here if it fails -- but Meronat decided no
                    if (bs.isValid() && bs.getFinal().getLocation().isPresent() && isSolidBlock(bs.getFinal().getState())) {

                        boolean result = lockInteraction.get().handleInteraction(player.get(), bs.getFinal().getLocation().get(), bs.getFinal());
                        bs.setValid(result);

                        //Set interactionSuccessful to true so we don't send expanded messages (for doors)
                        if (result) {
                            interactionSuccessful = true;
                        }

                    }

                    if(!lockInteraction.get().shouldPersist()) {
                        Latch.getLockManager().removeInteractionData(player.get().getUniqueId());
                    }
                }

            }
        }
    }

    @Listener
    public void onBreakBlockByPlayer(ChangeBlockEvent.Break event, @Root Player player) {
        //Only allow the owner to break a lock
        for( Transaction<BlockSnapshot> bs : event.getTransactions()) {
            if (bs.isValid() && bs.getOriginal().getLocation().isPresent()) {
                Optional<Lock> lock = Latch.getLockManager().getLock(bs.getOriginal().getLocation().get());

                //If the block is below a block we need to protect the below blocks of...
                //Potentially Sponge issue - should be able to detect these blocks
                if(Latch.getLockManager().isProtectBelowBlocks(bs.getOriginal().getLocation().get().getBlockRelative(Direction.UP).getBlockType()) &&
                        Latch.getLockManager().getLock(bs.getOriginal().getLocation().get().getBlockRelative(Direction.UP)).isPresent()) {
                    bs.setValid(false);
                    continue;
                }

                //If lock is present and the player is NOT the owner
                if(lock.isPresent() && !lock.get().isOwner(player.getUniqueId())) {
                    bs.setValid(false);
                } else if(lock.isPresent()) {
                    Latch.getLockManager().deleteLock(bs.getOriginal().getLocation().get(), false);
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
            if(bs.isValid() && bs.getOriginal().getLocation().isPresent()) {
                //If the block is below a block we need to protect the below blocks of...
                //Potentially Sponge issue - should be able to detect these blocks
                if(Latch.getLockManager().isProtectBelowBlocks(bs.getOriginal().getLocation().get().getBlockRelative(Direction.UP).getBlockType()) &&
                        Latch.getLockManager().getLock(bs.getOriginal().getLocation().get().getBlockRelative(Direction.UP)).isPresent()) {
                    bs.setValid(false);
                    event.setCancelled(true);
                    break;
                }

                if(Latch.getLockManager().isLockableBlock(bs.getOriginal().getState().getType()) && Latch.getLockManager().getLock(bs.getOriginal().getLocation().get()).isPresent()) {
                        bs.setValid(false);
                        event.setCancelled(true);
                }
            }
        }
    }

    @Listener
    public void onBlockBrokenByExplosion(ChangeBlockEvent.Break event) {
        //If there's a lock broken by an explosion, need to evaluate it
        boolean protectFromExplosives = event.getCause().first(Explosive.class).isPresent() && Latch.getConfig().getNode("protect_from_explosives").getBoolean(true);

        if(!protectFromExplosives) {
            //Delete the locks destroyed by the explosion
            for( Transaction<BlockSnapshot> bs : event.getTransactions()) {
                if (bs.isValid() && bs.getOriginal().getLocation().isPresent()) {
                    if(Latch.getLockManager().getLock(bs.getOriginal().getLocation().get()).isPresent()) {
                        Latch.getLockManager().deleteLock(bs.getOriginal().getLocation().get(), false);
                    }
                }
            }
        } else {
            for (Transaction<BlockSnapshot> bs : event.getTransactions()) {
                if (bs.isValid() && bs.getOriginal().getLocation().isPresent()) {

                    if (Latch.getLockManager().getLock(bs.getOriginal().getLocation().get()).isPresent()) {
                        bs.setValid(false);
                    }
                }
            }
        }
    }

    //Sponge issue? - Interacting with chests near water triggers the block place event for the surrounding water
    //So let's limit our block placing to just solid blocks
    private boolean isSolidBlock(BlockState bs) {
        Optional<MatterProperty> mp = bs.getProperty(MatterProperty.class);
        return mp.isPresent() && mp.get().getValue() == MatterProperty.Matter.SOLID;
    }


}
