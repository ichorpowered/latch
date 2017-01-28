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
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.NotifyNeighborBlockEvent;
import org.spongepowered.api.event.filter.cause.First;
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

    private boolean protectLockFromRedstone(Location<World> location) {
        Optional<Lock> lock = Latch.getLockManager().getLock(location);
        return lock.isPresent() && lock.get().getProtectFromRedstone();
    }

    /*This listener handles the below scenarios:
        -A piston moving the lock
        -Breaking the block below a lock dependent upon it
        -Redstone affecting the lock
    */
    //TODO Could this be improved? Is this querying too often? +Reevaluate once Sponge modifies redstone data
    @Listener
    public void notifyNeighbors(NotifyNeighborBlockEvent event, @First BlockSnapshot cause) {
        cause.getLocation().ifPresent(worldLocation -> {
            //OR the cause is powered and our config blocks redstone we want to stop the notification
                event.getNeighbors().entrySet().removeIf(neighbor ->
                    protectLockFromRedstone(worldLocation.getBlockRelative(neighbor.getKey())));
        });
    }

}
