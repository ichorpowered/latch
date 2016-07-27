/*
 * This file is part of Latch, licensed under the MIT License (MIT).
 *
 * Copyright (c) Ichor Community <http://www.ichorcommunity.com>
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

package com.ichorcommunity.latch.listeners;

import com.ichorcommunity.latch.Latch;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.entity.spawn.SpawnCause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.filter.cause.Root;

public class SpawnEntityListener {

    @Listener
    public void onSpawnLockItem(SpawnEntityEvent event, @Root SpawnCause sc) {
        //This is to prevent item drops from being generated when blocks break from things like Pistons
        //Sponge still drops the item even if the blocksnapshot is invalidated/event is cancelled

        //Piston generates a CUSTOM spawn type
        if( sc.getType() == SpawnTypes.CUSTOM ) {
            //For each of the entities
            for(Entity e : event.getEntities()) {
                //If it's an item and there's a lock there (have to convert item location to block location)
                if(e.getType() == EntityTypes.ITEM && Latch.getLockManager().getLock(e.getLocation().getExtent().getLocation(e.getLocation().getBlockX(), e.getLocation().getBlockY(), e.getLocation().getBlockZ())).isPresent() ) {
                    event.setCancelled(true);
                }
            }
        }
    }
}

