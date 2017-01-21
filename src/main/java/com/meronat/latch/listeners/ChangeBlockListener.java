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
import com.meronat.latch.entities.LockManager;
import com.meronat.latch.enums.LockType;
import com.meronat.latch.interactions.AbstractLockInteraction;
import com.meronat.latch.interactions.CreateLockInteraction;
import com.meronat.latch.utils.LatchUtils;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.Piston;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.property.block.MatterProperty;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.world.ExplosionEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.chat.ChatTypes;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.LocatableBlock;
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
        for (Transaction<BlockSnapshot> bs : event.getTransactions()) {
            if (bs.isValid() && bs.getFinal().getLocation().isPresent()) {
                //If the block is a restricted block, make sure it's not being placed near a lock
                if( Latch.getLockManager().isRestrictedBlock(bs.getFinal().getState().getType())) {
                    for(Lock lock : LatchUtils.getAdjacentLocks(bs.getFinal().getLocation().get())) {
                        //If there is a player and they aren't the owner, OR there's no player, invalidate
                        if( (!player.isPresent() || (player.isPresent() && !lock.isOwnerOrBypassing(player.get().getUniqueId()))) ) {
                            bs.setValid(false);
                            event.setCancelled(true);
                            player.ifPresent(p -> p.sendMessage(Text.of(TextColors.RED, "You can't place that type of block near a lock you don't own.")));
                        }
                    }
                }

                //If the block is a lockable block, make sure it's not connecting with someone else's lock
                if (Latch.getLockManager().isLockableBlock(bs.getFinal().getState().getType())) {
                    Optional<Location<World>> optionalOtherBlock = LatchUtils.getDoubleBlockLocation(bs.getFinal());
                    Optional<Lock> otherBlockLock = Optional.empty();

                    //If the block has another block that needs to be unlocked
                    if(optionalOtherBlock.isPresent()) {
                        otherBlockLock = Latch.getLockManager().getLock(optionalOtherBlock.get());
                    }
                    if(otherBlockLock.isPresent()) {
                        if(player.isPresent() && otherBlockLock.get().isOwnerOrBypassing(player.get().getUniqueId())) {
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

                // Make sure it is a player trying to complete the lock interaction.
                if (!(event.getCause().root() instanceof Player)) {
                    return;
                }

                if (player.isPresent()) {
                    //If the player has interaction data
                    if (lockInteraction.isPresent()) {
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

                        if (!lockInteraction.get().shouldPersist()) {
                            Latch.getLockManager().removeInteractionData(player.get().getUniqueId());
                        }
                    } else if (Latch.getConfig().getNode("auto_lock_on_placement").getBoolean()) {

                        if (!Latch.getLockManager().isLockableBlock(bs.getFinal().getState().getType())) {
                            return;
                        }

                        if (bs.isValid() && bs.getFinal().getLocation().isPresent() && isSolidBlock(bs.getFinal().getState())) {

                            boolean result = new CreateLockInteraction(player.get().getUniqueId(), LockType.PRIVATE, "")
                                    .handleInteraction(player.get(), bs.getFinal().getLocation().get(), bs.getFinal());

                            bs.setValid(result);

                            //Set interactionSuccessful to true so we don't send expanded messages (for doors)
                            if (result) {
                                interactionSuccessful = true;
                            }

                        }

                    }

                }

            }

        }

    }

    @Listener
    public void onPreBlockChange(ChangeBlockEvent.Pre event, @First LocatableBlock block) {

        if (block.getBlockState().getType().equals(BlockTypes.PISTON)) {

            //noinspection OptionalGetWithoutIsPresent
            Location<World> location = block.getLocation().getBlockRelative(block.get(Keys.DIRECTION).get());

            LockManager lockManager = Latch.getLockManager();

            if (lockManager.getLock(location).isPresent() || (lockManager.getLock(location.getBlockRelative(Direction.UP)).isPresent() && lockManager.isProtectBelowBlocks(location.getBlockRelative(Direction.UP).getBlockType()))) {

                event.setCancelled(true);

            }

        }

    }

    @Listener
    public void onBreakBlockByPlayer(ChangeBlockEvent.Break event, @Root Player player) {
        //Only allow the owner to break a lock
        for (Transaction<BlockSnapshot> bs : event.getTransactions()) {
            if (bs.isValid() && bs.getOriginal().getLocation().isPresent()) {

                Location location = bs.getOriginal().getLocation().get();

                LockManager lockManager = Latch.getLockManager();

                Optional<Lock> optionalLock = lockManager.getLock(location);

                //If the block is below a block we need to protect the below blocks of...
                //Potentially Sponge issue - should be able to detect these blocks

                Optional<Lock> aboveLock = lockManager.getLock(bs.getOriginal().getLocation().get().getBlockRelative(Direction.UP));

                if (aboveLock.isPresent() && lockManager.isProtectBelowBlocks(location.getBlockRelative(Direction.UP).getBlockType()) && !aboveLock.get().isOwnerOrBypassing(player.getUniqueId())) {

                    player.sendMessage(Text.of(TextColors.RED, "You cannot destroy a block which is depended on by a lock that's not yours."));
                    bs.setValid(false);
                    continue;

                }

                if (optionalLock.isPresent()) {

                    Lock lock = optionalLock.get();

                    //Check to make sure they're the owner
                    if(!lock.isOwnerOrBypassing(player.getUniqueId())) {
                        bs.setValid(false);
                        player.sendMessage(Text.of(TextColors.RED, "You cannot destroy a lock you are not the owner of."));
                        return;
                    }

                    player.sendMessage(Text.of(TextColors.DARK_GREEN, "You have destroyed this ", TextColors.GRAY,
                            lock.getLockedObject(), TextColors.DARK_GREEN, " lock."));

                    lockManager.deleteLock(bs.getOriginal().getLocation().get(), false);

                }

            }

        }

    }

    @Listener
    public void onBlockBrokenByExplosion(ExplosionEvent.Post event) {
        if(Latch.getConfig().getNode("protect_from_explosives").getBoolean(true)) {
            //If we're supposed to protect from explosions, invalidate the transaction
            for (Transaction<BlockSnapshot> transaction : event.getTransactions()) {
                if (transaction.isValid() && transaction.getOriginal().getLocation().isPresent()) {

                    if (Latch.getLockManager().getLock(transaction.getOriginal().getLocation().get()).isPresent()) {
                        transaction.setValid(false);
                    }
                }
            }
        } else {
            //Otherwise we should delete the locks destroyed by the explosion
            for( Transaction<BlockSnapshot> bs : event.getTransactions()) {
                if (bs.isValid() && bs.getOriginal().getLocation().isPresent()) {
                    if(Latch.getLockManager().getLock(bs.getOriginal().getLocation().get()).isPresent()) {
                        Latch.getLockManager().deleteLock(bs.getOriginal().getLocation().get(), false);
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
