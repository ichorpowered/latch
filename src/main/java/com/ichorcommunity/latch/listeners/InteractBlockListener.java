package com.ichorcommunity.latch.listeners;

import com.ichorcommunity.latch.Latch;
import com.ichorcommunity.latch.entities.Lock;
import com.ichorcommunity.latch.interactions.AbstractLockInteraction;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.filter.type.Include;
import org.spongepowered.api.text.Text;

import java.util.Optional;

public class InteractBlockListener {

    @Listener
    @Include( {InteractBlockEvent.Primary.class, InteractBlockEvent.Secondary.class})
    public void onPlayerClick(InteractBlockEvent event, @Root Player player) {
        //Special code to handle shift secondary clicking (placing a block)
        if(event instanceof InteractBlockEvent.Secondary && player.get(Keys.IS_SNEAKING).isPresent() && player.get(Keys.IS_SNEAKING).get()) {
            if(player.getItemInHand(HandTypes.MAIN_HAND).isPresent() && player.getItemInHand(HandTypes.MAIN_HAND).get().getItem().getBlock().isPresent()) {
                if(event.getTargetBlock().getLocation().isPresent() && event.getTargetBlock().getLocation().get().getBlockRelative(event.getTargetSide()).getBlockType() == BlockTypes.AIR) {
                    //If they're sneaking and have an item(block) in their hand, and are clicking to replace air... let the blockplace handle it
                    return;
                }
            }
        }

        //Ignore air and invalid locations, and non-lockable blocks
        if(event.getTargetBlock().equals(BlockSnapshot.NONE) || !(event.getTargetBlock().getLocation().isPresent()) || !Latch.getLockManager().isLockableBlock(event.getTargetBlock().getState().getType())) {
            return;
        }

        //If they have an interaction, handle the interaction
        if(Latch.getLockManager().hasInteractionData(player.getUniqueId())) {
            AbstractLockInteraction lockInteraction = Latch.getLockManager().getInteractionData(player.getUniqueId());

            boolean result = lockInteraction.handleInteraction(player, event.getTargetBlock().getLocation().get(), event.getTargetBlock());

            event.setCancelled(!result);

            if(!lockInteraction.shouldPersist()) {
                Latch.getLockManager().removeInteractionData(player.getUniqueId());
            }
        } else {
            //Otherwise we only care if it's a lock
            if(Latch.getLockManager().isLockableBlock(event.getTargetBlock().getState().getType())) {
                Optional<Lock> optionalLock = Latch.getLockManager().getLock(event.getTargetBlock().getLocation().get());
                if(optionalLock.isPresent() && !optionalLock.get().canAccess(player.getUniqueId())) {
                    player.sendMessage(Text.of("You can't access this lock."));
                    event.setCancelled(true);
                }
            }

        }
    }

}



