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
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.NotifyNeighborBlockEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Optional;

public class NotifyNeighborListener {

    //Cover all the ways the block could be powered
    /*private boolean isPowered(Location<?> location) {
        return (location.get(Keys.POWER).isPresent() && location.get(Keys.POWER).get() > 0) ||
                (location.getProperty(PoweredProperty.class).isPresent() && location.getProperty(PoweredProperty.class).get().getValue()) ||
                (location.getProperty(IndirectlyPoweredProperty.class).isPresent() && location.getProperty(IndirectlyPoweredProperty.class).get().getValue());
    }*/

    /*This listener handles the below scenarios:
        -A piston moving the lock
        -Breaking the block below a lock dependent upon it
        -Redstone affecting the lock
    */
    //TODO Could this be improved? Is this querying too often? +Reevaluate once Sponge modifies redstone data
    @Listener
    public void notifyNeighbors(NotifyNeighborBlockEvent event, @First LocatableBlock cause) {
        LockManager lockManager = Latch.getLockManager();
        event.getNeighbors().entrySet().removeIf(neighbor -> {
            Optional<Lock> optionalLock = lockManager.getLock(cause.getLocation().getBlockRelative(neighbor.getKey()));

            if (optionalLock.isPresent()) {

                Lock lock = optionalLock.get();

                if (!lock.getProtectFromRedstone()) {
                    return false;
                }

                Optional<Player> optionalPlayer = event.getCause().first(Player.class);

                if (!optionalPlayer.isPresent()) {
                    return true;
                } else {
                    return !lock.canAccess(optionalPlayer.get().getUniqueId());
                }

            }
            return false;
        });
    }

}
