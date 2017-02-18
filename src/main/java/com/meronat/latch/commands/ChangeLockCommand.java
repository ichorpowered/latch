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
import com.meronat.latch.interactions.ChangeLockInteraction;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.GuavaCollectors;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ChangeLockCommand implements CommandExecutor {

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (!(src instanceof Player)) {
            throw new CommandException(Text.of(TextColors.RED, "You must be a player to use this command."));
        }

        Player player = (Player) src;

        if (!(args.hasAny("name") || args.hasAny("type") || args.hasAny("owner") || args.hasAny("password") || args.hasAny("add") || args
            .hasAny("remove") || args.hasAny("redstone"))) {
            throw new CommandException(Text.of(TextColors.RED, "You must specify at least one attribute to change."));
        }

        if (args.hasAny("redstone") && !Latch.getLockManager().getProtectFromRedstone()) {
            throw new CommandException(Text.of(TextColors.RED, "Protection from redstone is disabled for all locks."));
        }

        ChangeLockInteraction changeLock = new ChangeLockInteraction(player.getUniqueId());

        Optional<String> optionalString = args.getOne("name");
        if (optionalString.isPresent()) {
            if (optionalString.get().length() <= 25) {
                changeLock.setLockName(optionalString.get());
            } else {
                throw new CommandException(Text.of(TextColors.RED, "Lock names must be less than 25 characters long."));
            }
        }

        // If type argument is present, change the type of lock to the one specified.
        args.<LockType>getOne("type").ifPresent(changeLock::setType);

        // If owner argument is present, set the new owner of the lock.
        args.<User>getOne("owner").ifPresent(user -> changeLock.setNewOwner(user.getUniqueId()));

        // If password argument is present, set the new password of the lock.
        args.<String>getOne("password").ifPresent(changeLock::setPassword);

        List<UUID> members = args.<User>getAll("add").stream().map(User::getUniqueId).collect(GuavaCollectors.toImmutableList());

        if (members.size() > 0) {
            changeLock.setMembersToAdd(members);
        }

        members = args.<User>getAll("remove").stream().map(User::getUniqueId).collect(GuavaCollectors.toImmutableList());

        if (members.size() > 0) {
            changeLock.setMembersToRemove(members);
        }

        changeLock.setPersistence(args.hasAny("p"));

        args.<Boolean>getOne("redstone").ifPresent(changeLock::setProtectFromRedstone);

        Latch.getLockManager().setInteractionData(player.getUniqueId(), changeLock);

        if (args.hasAny("p")) {
            player.sendMessage(Text.of(TextColors.DARK_GREEN, "You will change all locks you click until you type \"/latch persist\"."));
        } else {
            player.sendMessage(Text.of(TextColors.DARK_GREEN, "You will change the next lock you click."));
        }

        return CommandResult.success();
    }

}
