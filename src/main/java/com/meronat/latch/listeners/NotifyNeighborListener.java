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

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.noise.module.combiner.Power;
import com.meronat.latch.Latch;
import com.meronat.latch.entities.Lock;
import com.meronat.latch.entities.LockManager;
import com.meronat.latch.interactions.AbstractLockInteraction;
import com.meronat.latch.utils.LatchUtils;

import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.block.tileentity.carrier.TileEntityCarrier;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.property.block.IndirectlyPoweredProperty;
import org.spongepowered.api.data.property.block.MatterProperty;
import org.spongepowered.api.data.property.block.PoweredProperty;
import org.spongepowered.api.entity.explosive.Explosive;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.NotifyNeighborBlockEvent;
import org.spongepowered.api.event.data.ChangeDataHolderEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class NotifyNeighborListener {

    //Cover all the ways the block could be powered
    private boolean isPowered(Location<?> location) {
        return (location.get(Keys.POWER).isPresent() && location.get(Keys.POWER).get() > 0) ||
                (location.getProperty(PoweredProperty.class).isPresent() && location.getProperty(PoweredProperty.class).get().getValue()) ||
                (location.getProperty(IndirectlyPoweredProperty.class).isPresent() && location.getProperty(IndirectlyPoweredProperty.class).get().getValue());
    }

    @Listener
    public void notifyNeighbors(NotifyNeighborBlockEvent event, @First BlockSnapshot cause) {
        cause.getLocation().ifPresent(worldLocation -> {
            if(cause.getState().getType() == BlockTypes.REDSTONE_TORCH || cause.getState().getType() == BlockTypes.REDSTONE_WIRE ) {
                Latch.getLogger().info(cause.getState().getType().toString() + isPowered(worldLocation) + "," + cause.getApplicableProperties().toString());
                Latch.getLogger().info("Is data present: " + worldLocation.get(Keys.POWER) + " or present on bs " + cause.get(Keys.POWER));
                Latch.getLogger().info("Is powered data present: " + worldLocation.get(Keys.POWERED) + " or present on bs " + cause.get(Keys.POWERED));
            }
            if(isPowered(worldLocation)) {
                for (Direction d : event.getOriginalNeighbors().keySet()) {
                    Latch.getLockManager().getLock(worldLocation.getBlockRelative(d)).ifPresent(lock -> {
                        if (lock.getProtectFromRedstone()) {
                            event.getNeighbors().remove(d);
                        }
                    });
                }
            }
        });
    }

}
