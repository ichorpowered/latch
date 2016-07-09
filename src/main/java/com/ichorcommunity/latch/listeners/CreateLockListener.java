package com.ichorcommunity.latch.listeners;

public class CreateLockListener {

    /*@Listener
    public void onPlaceLock(ChangeBlockEvent.Place event, @Root Player player) {
        if(Latch.lockManager.hasCreationData(player.getUniqueId())) {

            LockCreationData type = Latch.lockManager.getCreationData(player.getUniqueId());

            for( Transaction<BlockSnapshot> bs : event.getTransactions()) {
                if(bs.getFinal().getLocation().isPresent() && bs.getFinal().getState().getType() == BlockTypes.CHEST) {

                    LockCreateEvent lockCreateEvent = new LockCreateEvent(player,
                            new Lock(player.getUniqueId(), type.getType(), bs.getFinal().getLocation().get(), type.getPassword()),
                            Cause.source(player).build());

                    Sponge.getEventManager().post(lockCreateEvent);

                    if(!event.isCancelled()) {
                        player.sendMessage(Text.of("You have created a " + lockCreateEvent.getLock().getLockType() + " lock, " + lockCreateEvent.getLock().getName()));
                        Latch.lockManager.createLock(lockCreateEvent.getLock());
                    }

                }
            }
        }
    }*/

}
