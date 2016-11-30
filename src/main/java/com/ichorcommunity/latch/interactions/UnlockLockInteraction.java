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

package com.ichorcommunity.latch.interactions;

import com.ichorcommunity.latch.Latch;
import com.ichorcommunity.latch.entities.Lock;
import com.ichorcommunity.latch.enums.LockType;
import com.ichorcommunity.latch.utils.LatchUtils;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

public class UnlockLockInteraction implements AbstractLockInteraction {

    private final UUID player;

    private boolean persisting = false;
    private final String password;

    public UnlockLockInteraction(UUID player, String password) {
        this.player = player;
        this.password = password;
    }

    @Override
    public boolean handleInteraction(Player player, Location<World> location, BlockSnapshot blockstate) {
        Optional<Lock> lock = Latch.getLockManager().getLock(location);
        //Check to see if another lock is present
        if(!lock.isPresent()) {
            player.sendMessage(Text.of("There is no lock there."));
            return false;
        }

        //If they're the owner or on the list of players within the lock, allow
        if(lock.get().canAccess(player.getUniqueId())) {
            return true;
        }

        //Check the password
        if(Latch.getLockManager().isPasswordCompatibleLock(lock.get())) {
            if( !LatchUtils.hashPassword(password, lock.get().getSalt()).equals(lock.get().getPassword())) {
                player.sendMessage(Text.of("The password you tried is incorrect."));
                return false;
            }

            //If the password is correct we're returning true - but if it's a PASSWORD_ONCE need to add them to allowed members
            if(lock.get().getLockType() == LockType.PASSWORD_ONCE) {
                //Check for other locks
                ArrayList<Lock> locks = new ArrayList<>();
                locks.add(lock.get());

                Optional<Location<World>> optionalOtherBlock = LatchUtils.getDoubleBlockLocation(blockstate);
                Optional<Lock> otherBlockLock = Optional.empty();

                //If the block has another block that needs to be unlocked
                if(optionalOtherBlock.isPresent()) {
                    otherBlockLock = Optional.of(Latch.getLockManager().getLock(optionalOtherBlock.get()).get());
                }
                if(otherBlockLock.isPresent()) {
                    if(!otherBlockLock.get().getPassword().equalsIgnoreCase(password)) {
                        player.sendMessage(Text.of("The adjacent lock does not have the same password."));
                    } else {
                        locks.add(otherBlockLock.get());
                    }
                }

                //Modify the attributes of the lock
                for(Lock thisLock : locks) {
                    Latch.getLockManager().addLockAccess(thisLock, player.getUniqueId());
                }
                player.sendMessage(Text.of("Unlocking the password lock for future access."));
            }
            return true;
        } else {
            player.sendMessage(Text.of("That is not a password lock."));
        }
        //Default state
        return false;
    }

    @Override
    public boolean shouldPersist() {
        return persisting;
    }

    @Override
    public void setPersistence(boolean persist) {
        this.persisting = persist;
    }
}
