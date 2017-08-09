/*
 * This file is part of Latch, licensed under the MIT License.
 *
 * Copyright (c) 2016-2017 IchorPowered <https://github.com/IchorPowered>
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

package com.meronat.latch.commands;

import com.meronat.latch.Latch;
import com.meronat.latch.enums.LockType;
import com.meronat.latch.interactions.CreateLockInteraction;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Optional;

public class CreatePrivateLockCommand implements CommandExecutor {

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (!(src instanceof Player)) {
            throw new CommandException(Text.of(TextColors.RED, "You must be a player to use this command."));
        }

        final Player player = (Player) src;
        final Optional<String> optionalName = args.getOne("name");

        final CreateLockInteraction privateLock;

        //noinspection OptionalIsPresent
        if (optionalName.isPresent()) {
            if (!Latch.getLockManager().isUniqueName(player.getUniqueId(), optionalName.get())) {
                throw new CommandException(Text.of(TextColors.RED, "You already have a lock with this name."));
            }
            if (optionalName.get().length() <= 25) {
                privateLock = new CreateLockInteraction(player.getUniqueId(), LockType.PRIVATE, "", optionalName.get());
            } else {
                throw new CommandException(Text.of(TextColors.RED, "Lock names must be less than 25 characters long."));
            }
        } else {
            privateLock = new CreateLockInteraction(player.getUniqueId(), LockType.PRIVATE, "");
        }

        privateLock.setPersistence(args.hasAny("p"));

        Latch.getLockManager().setInteractionData(player.getUniqueId(), privateLock);

        if (args.hasAny("p")) {
            player.sendMessage(
                Text.of(TextColors.DARK_GREEN, "You will lock all latchable blocks you click or place until you type \"latch persist\"."));
        } else {
            player.sendMessage(Text.of(TextColors.DARK_GREEN, "You will lock the next latchable block you click or place."));
        }

        return CommandResult.success();
    }

}
