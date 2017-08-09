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
import com.meronat.latch.entities.Lock;
import com.meronat.latch.utils.LatchUtils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ListCommand implements CommandExecutor {

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        final Optional<User> optionalUser = args.getOne("owner");
        final User user;

        //If the user doesn't have the permission to check someone else's lock, default to them
        if (optionalUser.isPresent()) {
            if (src.hasPermission("latch.admin.list")) {
                user = optionalUser.get();
            } else {
                throw new CommandException(Text.of(TextColors.RED, "You do not have permission to specify a user to list."));
            }
        } else if (src instanceof User) {
            user = (User) src;
        } else {
            throw new CommandException(Text.of(TextColors.RED, "Only users can use this command without specifying a player."));
        }

        Sponge.getScheduler().createAsyncExecutor(Latch.getPluginContainer()).execute(() -> {
            final List<Text> contents = new ArrayList<>();
            final String displayName = src.getName().equalsIgnoreCase(user.getName()) ? "Your" : user.getName() + "'s";
            final List<Lock> locks = Latch.getLockManager().getPlayersLocks(user.getUniqueId());

            for (Lock lock : locks) {
                String location = lock.getFirstLocation().isPresent() ? LatchUtils.getLocationString(lock.getFirstLocation().get()) : "N/A";
                contents.add(
                    Text.of("  ", lock.getName(), TextColors.GRAY, ", type: ", TextColors.WHITE, lock.getLockType(), TextColors.GRAY, ", location: ",
                        TextColors.WHITE, location));
            }

            Sponge.getScheduler().createSyncExecutor(Latch.getPluginContainer()).execute(() -> {
                Optional<PaginationService> optionalPaginationService = Sponge.getServiceManager().provide(PaginationService.class);

                if (optionalPaginationService.isPresent()) {
                    optionalPaginationService.get().builder()
                        .title(Text.of(TextColors.DARK_GREEN, displayName + " Locks"))
                        .header(Text.of(TextColors.GRAY, "There are ", TextColors.WHITE, locks.size(), TextColors.GRAY, " lock(s):"))
                        .linesPerPage(10)
                        .padding(Text.of(TextColors.GRAY, "="))
                        .contents(contents)
                        .sendTo(src);
                } else {
                    src.sendMessage(Text.of(TextColors.RED, "Pagination service not found, printing out list:"));
                    for (Text t : contents) {
                        src.sendMessage(t);
                    }
                }
            });
        });

        return CommandResult.success();
    }

}
