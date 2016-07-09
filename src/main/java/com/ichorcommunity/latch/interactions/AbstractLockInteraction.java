package com.ichorcommunity.latch.interactions;

import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public interface AbstractLockInteraction {

    //Must take in BlockSnapshot for when this fires in events - need the "original" or "final" depending on event
    abstract boolean handleInteraction(Player player, Location<World> location, BlockSnapshot blockstate);

    abstract boolean shouldPersist();

    abstract void setPersistance(boolean persist);


}
