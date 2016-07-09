package com.ichorcommunity.latch.listeners;

import com.ichorcommunity.latch.Latch;
import com.ichorcommunity.latch.interactions.AbstractLockInteraction;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.filter.cause.Root;

public class InteractBlockListener {

    @Listener
    public void onSecondaryClick(InteractBlockEvent.Secondary event, @Root Player player) {
        //Ignore air and invalid locations
        if(event.getTargetBlock().equals(BlockSnapshot.NONE) || !(event.getTargetBlock().getLocation().isPresent())) {
            return;
        }


        if(Latch.lockManager.hasInteractionData(player.getUniqueId())) {
            AbstractLockInteraction lockInteraction = Latch.lockManager.getInteractionData(player.getUniqueId());

            boolean result = lockInteraction.handleInteraction(player, event.getTargetBlock().getLocation().get(), event.getTargetBlock());

            event.setCancelled(!result);
        }
    }

}



