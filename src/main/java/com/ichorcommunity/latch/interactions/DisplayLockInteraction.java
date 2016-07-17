package com.ichorcommunity.latch.interactions;

import com.ichorcommunity.latch.Latch;
import com.ichorcommunity.latch.entities.Lock;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;
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
    public boolean handleInteraction(Player player, Location<World> location, BlockSnapshot blockstate) {
        Optional<Lock> lock = Latch.getLockManager().getLock(location);
        //Check to see if another lock is present
        if(!lock.isPresent()) {
            player.sendMessage(Text.of("There is no lock there."));
            return false;
        }

        Optional<UserStorageService> userStorageService = Sponge.getGame().getServiceManager().provide(UserStorageService.class);

        player.sendMessage(Text.of("Lock name: " + lock.get().getName() + ", Type: " + lock.get().getLockType()));
        player.sendMessage(Text.of("Owner: " + lock.get().getOwnerName()));
        player.sendMessage(Text.of("Locked object: " + lock.get().getLockedObject()));
        player.sendMessage(Text.of("Players: " + String.join(", ", lock.get().getAbleToAccessNames())));

        //Return false to cancel interactions when using this command
        return false;
    }

    @Override
    public boolean shouldPersist() {
        return persisting;
    }

    @Override
    public void setPersistance(boolean persist) {
        this.persisting = persist;
    }

}
