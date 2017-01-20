/*
 * This file is part of Latch, licensed under the MIT License (MIT).
 *
 * Copyright (c) IchorPowered <https://github.com/IchorPowered>
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
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Optional;

public class PurgeCommand implements CommandExecutor {

    public CommandCallable getCommand() {
        return CommandSpec.builder()
                .description(Text.of("Set yourself to be bypassing."))
                .permission("latch.normal.purge")
                .arguments(GenericArguments.optionalWeak(GenericArguments.onlyOne(GenericArguments.user(Text.of("target")))))
                .executor(this)
                .build();
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {

        Optional<User> optionalUser = args.getOne("target");

        User user;

        if (src instanceof User) {

            if (optionalUser.isPresent()) {

                if (src.hasPermission("latch.admin.purge")) {

                    user = optionalUser.get();

                } else {

                    throw new CommandException(Text.of(TextColors.RED, "You do not have permission to specify a player to purge."));

                }

            } else {

                user = (User) src;

            }

        } else if (src instanceof ConsoleSource) {

            if (optionalUser.isPresent()) {

                user = optionalUser.get();

            } else {

                throw new CommandException(Text.of(TextColors.RED, "You must specify a user."));

            }

        } else {

            throw new CommandException(Text.of(TextColors.RED, "You source type is not able to execute this command."));

        }

        Latch.getStorageHandler().deleteLocksForPlayer(user.getUniqueId());

        user.getPlayer().ifPresent(p -> p.sendMessage(Text.of(TextColors.DARK_GREEN, "All of ", TextColors.GRAY,
                user.getName() + "'s", TextColors.DARK_GREEN, " locks have been purged.")));

        return CommandResult.success();

    }

}
