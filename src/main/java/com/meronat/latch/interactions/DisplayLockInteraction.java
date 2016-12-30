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

package com.meronat.latch.interactions;

import com.meronat.latch.Latch;
import com.meronat.latch.entities.Lock;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Optional;
import java.util.UUID;

public class DisplayLockInteraction implements AbstractLockInteraction {

    private final UUID player;

    private boolean persisting = false;

    public DisplayLockInteraction(UUID player) {
        this.player = player;
    }

    @Override
    public boolean handleInteraction(Player player, Location<World> location, BlockSnapshot blockState) {

        Optional<Lock> optionalLock = Latch.getLockManager().getLock(location);
        //Check to see if another lock is present
        if(!optionalLock.isPresent()) {
            player.sendMessage(Text.of(TextColors.RED, "There is no lock there."));
            return false;
        }

        Lock lock = optionalLock.get();

        player.sendMessage(Text.of(TextColors.GOLD, "Latch Info - " + "X: " + location.getBlockX() + " Y: " + location.getBlockY() + " Z: " + location.getBlockZ()));
        player.sendMessage(Text.of(TextColors.DARK_GREEN, lock.getLockType().getHumanReadable() + " " +
                lock.getLockedObject().substring(0, 1).toUpperCase() + lock.getLockedObject().substring(1) + ": ", TextColors.GRAY, lock.getName()));
        player.sendMessage(Text.of(TextColors.DARK_GREEN, "Owner: ", TextColors.GRAY, lock.getOwnerName()));
        player.sendMessage(Text.of(TextColors.DARK_GREEN, "Accessors: ", TextColors.GRAY, String.join(", ", lock.getAbleToAccessNames())));

        if (Latch.getConfig().getNode("protect_from_redstone").getBoolean(false)) {
            player.sendMessage(Text.of(TextColors.DARK_GREEN, "Protect from Redstone: ", TextColors.GRAY, lock.getProtectFromRedstone()));
        }

        //Return false to cancel interactions when using this command
        return false;

    }

    @Override
    public boolean shouldPersist() {
        return persisting;
    }

    @Override
    public void setPersistence(boolean persist) {
        this.persisting = persist;
    }

}
