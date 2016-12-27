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

package com.ichorcommunity.latch.interactions;

import com.ichorcommunity.latch.Latch;
import com.ichorcommunity.latch.entities.Lock;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Optional;
import java.util.UUID;

public class DeleteLockInteraction implements AbstractLockInteraction {

    private final UUID player;

    private boolean persisting = false;

    public DeleteLockInteraction(UUID player) {
        this.player = player;
    }

    @Override
    public boolean handleInteraction(Player player, Location<World> location, BlockSnapshot blockstate) {
        Optional<Lock> lock = Latch.getLockManager().getLock(location);
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

        player.sendMessage(Text.of("You have deleted this " + lock.get().getLockedObject() + " lock."));
        Latch.getLockManager().deleteLock(location, true);

        return true;
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
