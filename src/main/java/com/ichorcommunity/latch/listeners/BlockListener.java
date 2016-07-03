package com.ichorcommunity.latch.listeners;

import com.ichorcommunity.latch.Latch;
import com.ichorcommunity.latch.entities.Lock;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Optional;

import static com.ichorcommunity.latch.Latch.getLogger;

public class BlockListener {

    @Listener
    public void onBreakBlock(ChangeBlockEvent.Break event) {
        getLogger().info("BlockListener - onBreakBlock, cancelled? " + event.isCancelled());

        for( Transaction<BlockSnapshot> bs : event.getTransactions()) {
            if(bs.getOriginal().getLocation().isPresent()) {

                Optional<Lock> lock = Latch.lockManager.getLock(bs.getOriginal().getLocation().get());

                if( lock.isPresent() ) {
                    event.setCancelled(true);
                    return;
                }

            }
            Location<World> location = bs.getOriginal().getLocation().get();

            Latch.lockManager.getLock(location);



        }
    }



}
