/*
 * This file is part of Latch, licensed under the MIT License.
 *
 * Copyright (c) 2016-2018 IchorPowered <https://github.com/IchorPowered>
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
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

import java.util.Optional;

public class PurgeCommand implements CommandExecutor {

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        final Optional<User> optionalUser = args.getOne("target");
        final User user;
        final boolean self;

        if (src instanceof User) {
            if (optionalUser.isPresent()) {
                if (src.hasPermission("latch.admin.purge")) {
                    self = false;
                    user = optionalUser.get();
                } else {
                    throw new CommandException(Text.of(TextColors.RED, "You do not have permission to specify a player to purge."));
                }
            } else {
                user = (User) src;
                self = true;
            }
        } else if (src instanceof ConsoleSource) {
            if (optionalUser.isPresent()) {
                self = false;
                user = optionalUser.get();
            } else {
                throw new CommandException(Text.of(TextColors.RED, "You must specify a user."));
            }
        } else {
            throw new CommandException(Text.of(TextColors.RED, "You source type is not able to execute this command."));
        }

        final Text yes = Text.of(TextColors.GREEN, TextActions.executeCallback(x -> yes(user, src, self)), "YES   ");
        final Text no = Text.of(TextColors.RED, TextActions.executeCallback(x -> no(user, src, self)), " NO");

        if (self) {
            src.sendMessage(Text.of(TextColors.DARK_RED, "Are you sure you want to delete all of your locks? ").concat(yes).concat(no));
        } else {
            src.sendMessage(
                Text.of(TextColors.DARK_RED, "Are you sure you want to delete all of ", TextColors.GRAY, user.getName() + "'s", TextColors.DARK_RED,
                    " locks?").concat(yes).concat(no));
        }

        return CommandResult.success();
    }

    private void yes(User user, CommandSource src, boolean self) {
        Latch.getStorageHandler().deleteLocksForPlayer(user.getUniqueId());

        if (self) {
            src.sendMessage(Text.of(TextColors.DARK_GREEN, "All of your locks have been deleted."));
        } else {
            src.sendMessage(
                Text.of(TextColors.DARK_GREEN, "All of ", TextColors.GRAY, user.getName() + "'s", TextColors.DARK_GREEN, " locks have been purged."));
            user.getPlayer().ifPresent(p -> p.sendMessage(Text.of(TextColors.RED, "All of your locks have been deleted by a staff member.")));
        }
    }

    private void no(User user, CommandSource src, boolean self) {
        if (self) {
            src.sendMessage(Text.of(TextColors.DARK_GREEN, "You have cancelled the deletion of all of your locks."));
        } else {
            src.sendMessage(
                Text.of(TextColors.DARK_GREEN, "You have cancelled the deletion of ", TextColors.GRAY, user.getName() + "'s", TextColors.DARK_GREEN,
                    " locks."));
        }
    }

}
