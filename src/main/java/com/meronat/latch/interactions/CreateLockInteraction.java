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

package com.meronat.latch.interactions;

import com.meronat.latch.Latch;
import com.meronat.latch.entities.Lock;
import com.meronat.latch.enums.LockType;
import com.meronat.latch.events.LockCreateEvent;
import com.meronat.latch.utils.LatchUtils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

public class CreateLockInteraction implements LockInteraction {

    private final UUID player;
    private final LockType type;
    private String password;
    private String name;

    private boolean persisting = false;

    public CreateLockInteraction(UUID player, LockType type, String password) {
        this.player = player;
        this.type = type;
        this.password = password;
    }

    public CreateLockInteraction(UUID player, LockType type, String password, String name) {
        this.player = player;
        this.type = type;
        this.password = password;
        this.name = name;
    }

    @Override
    public boolean handleInteraction(Player player, Location<World> location, BlockSnapshot blockState) {
        //Check to see if another lock is present
        if (Latch.getLockManager().getLock(location).isPresent()) {
            player.sendMessage(Text.of(TextColors.RED, "There is already a lock here."));
            return false;
        }

        //Make sure it's a lockable block
        if (!Latch.getLockManager().isLockableBlock(blockState.getState().getType())) {
            player.sendMessage(Text.of(TextColors.RED, "That is not a lockable block: ", TextColors.GRAY, blockState.getState().getType()));
            return false;
        }

        Optional<Location<World>> optionalOtherBlock = LatchUtils.getDoubleBlockLocation(blockState);

        HashSet<Location<World>> lockLocations = new HashSet<>();
        lockLocations.add(location);

        //If the block has another block that needs to be locked
        if (optionalOtherBlock.isPresent()) {
            //Check to see if another lock is present
            Optional<Lock> otherLock = Latch.getLockManager().getLock(optionalOtherBlock.get());
            if (otherLock.isPresent() && !otherLock.get().isOwnerOrBypassing(player.getUniqueId())) {
                //Shouldn't happen if we've configured this correctly - but just in case...
                player.sendMessage(Text.of(TextColors.RED, "Another lock already present on the double block - delete locks and try again."));
                return false;
            }
            lockLocations.add(optionalOtherBlock.get());
        }

        if (Latch.getLockManager().isPlayerAtLockLimit(player.getUniqueId(), this.type)) {
            player.sendMessage(Text.of(TextColors.RED, "You have reached the limit for locks."));
            return false;
        }

        byte[] salt = null;

        if (this.type.equals(LockType.PASSWORD_ALWAYS) || this.type.equals(LockType.PASSWORD_ONCE)) {
            salt = LatchUtils.generateSalt();

            this.password = LatchUtils.hashPassword(this.password, salt);
        }

        LockCreateEvent lockCreateEvent = new LockCreateEvent(
            player,
            Lock.builder()
                .owner(player.getUniqueId())
                .type(this.type)
                .name(this.name)
                .objectName(LatchUtils.getBlockNameFromType(blockState.getState().getType()))
                .password(this.password)
                .salt(salt)
                .locations(lockLocations)
                .lastAccessed(LocalDateTime.now())
                .protectFromRedstone(Latch.getLockManager().getProtectFromRedstone())
                .build(),
            Cause.source(player).build());

        Sponge.getEventManager().post(lockCreateEvent);

        //Stop if original locking event or other block locking event is cancelled
        if (lockCreateEvent.isCancelled()) {
            return false;
        }

        //Notify the player
        player.sendMessage(Text.of(TextColors.DARK_GREEN, "You have created a ", TextColors.GRAY,
            lockCreateEvent.getLock().getLockType().getHumanReadable().toLowerCase(), TextColors.DARK_GREEN, " lock called: ", TextColors.GRAY,
            lockCreateEvent.getLock().getName()));
        Latch.getLockManager().createLock(lockCreateEvent.getLock());

        return true;
    }

    @Override
    public boolean shouldPersist() {
        return this.persisting;
    }

    @Override
    public void setPersistence(boolean persist) {
        this.persisting = persist;
    }

}
