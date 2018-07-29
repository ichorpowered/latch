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

package com.meronat.latch.listeners;

import com.meronat.latch.Latch;
import com.meronat.latch.entities.Lock;
import com.meronat.latch.enums.LockType;
import com.meronat.latch.interactions.LockInteraction;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.data.type.PortionTypes;
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
import org.spongepowered.api.item.inventory.BlockCarrier;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.api.item.inventory.type.CarriedInventory;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class InteractBlockListener {

    //To work around Sponge issue - if the player is opening a donation chest they don't own... don't let them spawn xp (i.e. furnaces)
    private HashSet<UUID> stopThem = new HashSet<>();
    private Set<Location<World>> noChange = new HashSet<>();

    @Listener
    public void onClickInventory(final ClickInventoryEvent event, @First Player player) {
        //Make sure we have a transaction to validate
        if (event.getTransactions().size() <= 0) {
            return;
        }

        //Get the first transaction of this event
        final SlotTransaction slotTransaction = event.getTransactions().get(0);

        if (event.getTargetInventory() instanceof CarriedInventory<?>) {
            final CarriedInventory<?> carriedInventory = (CarriedInventory<?>) event.getTargetInventory();
            final Optional<?> carrier = carriedInventory.getCarrier();

            if (carrier.isPresent() && carrier.get() instanceof BlockCarrier) {
                final BlockCarrier blockCarrier = (BlockCarrier) carrier.get();
                //If the final item is NONE (or amount is less) person is trying to withdraw (so we care about it)
                if (slotTransaction.getFinal().getType() == ItemTypes.NONE || slotTransaction.getFinal().getQuantity() < slotTransaction.getOriginal().getQuantity()) {
                    //Then check to see if there's a lock
                    final Optional<Lock> lock = Latch.getLockManager().getLock(blockCarrier.getLocation());

                    //If there's a donation lock the player CANNOT access
                    if (lock.isPresent() && lock.get().getLockType() == LockType.DONATION && !lock.get().canAccess(player.getUniqueId())) {
                        event.setCancelled(true);
                    }
                }
            }
        }

        /*
        //If the player is interacting with a TileEntityCarrier
        if (event.getTargetInventory() instanceof TileEntityCarrier) {
            //If the final item is NONE (or amount is less) person is trying to withdraw (so we care about it)
            if (slotTransaction.getFinal().getType() == ItemTypes.NONE || slotTransaction.getFinal().getQuantity() < slotTransaction.getOriginal().getQuantity()) {
                //Then check to see if there's a lock
                final Optional<Lock> lock = Latch.getLockManager().getLock(((TileEntityCarrier) event.getTargetInventory()).getLocation());

                //If there's a donation lock the player CANNOT access
                if (lock.isPresent() && lock.get().getLockType() == LockType.DONATION && !lock.get().canAccess(player.getUniqueId())) {
                    event.setCancelled(true);
                }
            }
        }
        */
    }

    @Listener
    public void onCloseInventory(final InteractInventoryEvent.Close event, @Root Player player) {
        this.stopThem.remove(player.getUniqueId());
    }

    @Listener
    public void onSpawnExp(final SpawnEntityEvent event, @First Player player) {
        if (this.stopThem.contains(player.getUniqueId())) {
            for (Entity e : event.getEntities()) {
                if (e.getType() == EntityTypes.EXPERIENCE_ORB) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @Listener
    public void onLeave(final ClientConnectionEvent.Disconnect event, @Root Player player) {
        this.stopThem.remove(player.getUniqueId());
    }

    @Listener
    @Include({InteractBlockEvent.Primary.class, InteractBlockEvent.Secondary.class})
    public void onPlayerClick(final InteractBlockEvent event, @Root Player player) {
        final BlockSnapshot block = event.getTargetBlock();
        if (!block.getLocation().isPresent()) {
            return;
        }

        final Location<World> location = block.getLocation().get();
        //Special code to handle shift secondary clicking (placing a block)
        if (event instanceof InteractBlockEvent.Secondary && player.get(Keys.IS_SNEAKING).orElse(false)) {
            if (player.getItemInHand(HandTypes.MAIN_HAND).isPresent() && player.getItemInHand(HandTypes.MAIN_HAND).get().getType().getBlock().isPresent()) {
                if (location.getBlockRelative(event.getTargetSide()).getBlockType() == BlockTypes.AIR) {
                    //If they're sneaking and have an item(block) in their hand, and are clicking to replace air... let the block place handle it
                    return;
                }
            }
        }

        final BlockType blockType = block.getState().getType();

        //Ignore air and invalid locations, and non-lockable blocks
        if (block.equals(BlockSnapshot.NONE) || !Latch.getLockManager().isLockableBlock(blockType)) {
            return;
        }

        //If they have an interaction, handle the interaction
        if (Latch.getLockManager().hasInteractionData(player.getUniqueId())) {
            final LockInteraction lockInteraction = Latch.getLockManager().getInteractionData(player.getUniqueId());

            lockInteraction.handleInteraction(player, location, block);

            event.setCancelled(true);

            if (!lockInteraction.shouldPersist()) {
                Latch.getLockManager().removeInteractionData(player.getUniqueId());
            }
        } else {
            //Otherwise we only care if it's a lock
            Latch.getLockManager().getLock(location).ifPresent(lock -> {
                if (lock.getLockType() != LockType.DONATION && !lock.canAccess(player.getUniqueId())) {
                    player.sendMessage(Text.of(TextColors.RED, "You cannot access this lock."));
                    event.setCancelled(true);
                } else {
                    // Work around code for donation furnaces allowing infinite experience
                    if ((blockType.equals(BlockTypes.FURNACE) || blockType.equals(BlockTypes.LIT_FURNACE))
                            && lock.getLockType() == LockType.DONATION && !lock.canAccess(player.getUniqueId())) {
                        this.stopThem.add(player.getUniqueId());
                    } else if (blockType.equals(BlockTypes.IRON_DOOR) || blockType.equals(BlockTypes.IRON_TRAPDOOR)) {
                        if (!this.noChange.remove(location)) { // Hack to get iron door opening working for now
                            this.noChange.add(location);
                            final Location<World> newLocation;
                            if (blockType.equals(BlockTypes.IRON_DOOR) &&
                                    location.get(Keys.PORTION_TYPE).orElse(PortionTypes.BOTTOM).equals(PortionTypes.TOP)) {
                                newLocation = location.getBlockRelative(Direction.DOWN);
                            } else {
                                newLocation = location;
                            }
                            if (newLocation.get(Keys.OPEN).orElse(false)) {
                                newLocation.offer(Keys.OPEN, false);
                                newLocation.offer(Keys.POWERED, false);
                            } else {
                                newLocation.offer(Keys.OPEN, true);
                                newLocation.offer(Keys.POWERED, true);
                            }
                        }
                    }
                    lock.updateLastAccessed();
                }
            });
        }
    }

}
