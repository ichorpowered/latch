/*
 * This file is part of Latch, licensed under the MIT License.
 *
 * Copyright (c) 2016-2017 IchorPowered <https://github.com/IchorPowered>
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
import com.meronat.latch.enums.LockType;
import com.meronat.latch.interactions.LockInteraction;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.carrier.TileEntityCarrier;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.filter.type.Include;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

public class InteractBlockListener {

    //To work around Sponge issue - if the player is opening a donation chest they don't own... don't let them spawn xp (i.e. furnaces)
    private HashSet<UUID> stopThem = new HashSet<>();

    @Listener
    public void onClickInventory(ClickInventoryEvent event, @First Player player) {
        //Make sure we have a transaction to validate
        if (event.getTransactions().size() <= 0) {
            return;
        }
        //Get the first transaction of this event
        SlotTransaction slotTransaction = event.getTransactions().get(0);

        Slot slot = slotTransaction.getSlot();

        //If the player is interacting with a TileEntityCarrier
        if (slot.parent() instanceof TileEntityCarrier ) {
            //If the final item is NONE (or amount is less) person is trying to withdraw (so we care about it)
            if (slotTransaction.getFinal().getType() == ItemTypes.NONE || slotTransaction.getFinal().getCount() < slotTransaction.getOriginal().getCount()) {
                //Then check to see if there's a lock
                Optional<Lock> lock = Latch.getLockManager().getLock(((TileEntityCarrier) slot.parent()).getLocation());

                //If there's a donation lock the player CANNOT access
                if(lock.isPresent() && lock.get().getLockType() == LockType.DONATION && !lock.get().canAccess(player.getUniqueId())) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @Listener
    public void onCloseInventory(InteractInventoryEvent.Close event, @Root Player player) {
        this.stopThem.remove(player.getUniqueId());
    }


    @Listener
    public void onSpawnExp(SpawnEntityEvent event, @First Player player) {
        if(this.stopThem.contains(player.getUniqueId())) {
            for(Entity e : event.getEntities()) {
                if(e.getType() == EntityTypes.EXPERIENCE_ORB) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @Listener
    public void onLeave(ClientConnectionEvent.Disconnect event, @Root Player player) {
        this.stopThem.remove(player.getUniqueId());
    }

    @Listener
    @Include( {InteractBlockEvent.Primary.class, InteractBlockEvent.Secondary.class})
    public void onPlayerClick(InteractBlockEvent event, @Root Player player) {
        //Special code to handle shift secondary clicking (placing a block)
        if(event instanceof InteractBlockEvent.Secondary && player.get(Keys.IS_SNEAKING).orElse(false)) {
            if(player.getItemInHand(HandTypes.MAIN_HAND).isPresent() && player.getItemInHand(HandTypes.MAIN_HAND).get().getItem().getBlock().isPresent()) {
                if(event.getTargetBlock().getLocation().isPresent() && event.getTargetBlock().getLocation().get().getBlockRelative(event.getTargetSide()).getBlockType() == BlockTypes.AIR) {
                    //If they're sneaking and have an item(block) in their hand, and are clicking to replace air... let the block place handle it
                    return;
                }
            }
        }

        //Ignore air and invalid locations, and non-lockable blocks
        if(event.getTargetBlock().equals(BlockSnapshot.NONE) || !(event.getTargetBlock().getLocation().isPresent()) || !Latch.getLockManager().isLockableBlock(event.getTargetBlock().getState().getType())) {
            return;
        }

        //If they have an interaction, handle the interaction
        if(Latch.getLockManager().hasInteractionData(player.getUniqueId())) {
            LockInteraction lockInteraction = Latch.getLockManager().getInteractionData(player.getUniqueId());

            lockInteraction.handleInteraction(player, event.getTargetBlock().getLocation().get(), event.getTargetBlock());

            event.setCancelled(true);

            if(!lockInteraction.shouldPersist()) {
                Latch.getLockManager().removeInteractionData(player.getUniqueId());
            }
        } else {
            //Otherwise we only care if it's a lock
            if(Latch.getLockManager().isLockableBlock(event.getTargetBlock().getState().getType())) {
                Latch.getLockManager().getLock(event.getTargetBlock().getLocation().get()).ifPresent(lock -> {
                    if (lock.getLockType() != LockType.DONATION && !lock.canAccess(player.getUniqueId())) {
                        player.sendMessage(Text.of(TextColors.RED, "You cannot access this lock."));
                        event.setCancelled(true);
                    } else {
                        if ((event.getTargetBlock().getState().getType().equals(BlockTypes.FURNACE)
                                || event.getTargetBlock().getState().getType().equals(BlockTypes.LIT_FURNACE))
                                && lock.getLockType() == LockType.DONATION
                                && !lock.canAccess(player.getUniqueId())) {
                            this.stopThem.add(player.getUniqueId());
                        }
                        lock.updateLastAccessed();
                    }
                });
            }

        }
    }

}