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
import java.util.Map;
import java.util.Optional;

public class LimitsCommand implements CommandExecutor {

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {

        Optional<User> optionalUser = args.getOne("user");

        User user;

        if (optionalUser.isPresent()) {
            if (src.hasPermission("latch.admin.limits")) {
                user = optionalUser.get();
            } else {
                throw new CommandException(Text.of(TextColors.RED, "You do not have permission to specify a user to see their limits."));
            }
        } else if (src instanceof User) {
            user = (User) src;
        } else {
            throw new CommandException(Text.of(TextColors.RED, "Only users can use this command without specifying a player."));
        }

        Sponge.getScheduler().createAsyncExecutor(Latch.getPluginContainer()).execute(() -> {
            List<Text> contents = new ArrayList<>();

            String displayName = src.getName().equalsIgnoreCase(user.getName()) ? "Your" : user.getName()+"'s";

            Map<String, Integer> limits = Latch.getLockManager().getLimits();

            int total = 0;

            Map<String, Integer> amounts = Latch.getStorageHandler().getLimits(user.getUniqueId());

            for (Map.Entry<String, Integer> e : limits.entrySet()) {
                if (!e.getKey().equalsIgnoreCase("total")) {
                    contents.add(Text.of(TextColors.GOLD, e.getKey().toLowerCase() + ": ", TextColors.GRAY, amounts.getOrDefault(e.getKey(), 0) + "/" + e.getValue()));

                    total += amounts.getOrDefault(e.getKey(), 0);
                }
            }

            contents.add(Text.of(TextColors.GOLD, "total: ", TextColors.GRAY, total + "/" + limits.get("total")));

            Sponge.getScheduler().createSyncExecutor(Latch.getPluginContainer()).execute(() -> {
                Optional<PaginationService> optionalPaginationService = Sponge.getServiceManager().provide(PaginationService.class);

                if (optionalPaginationService.isPresent()) {
                    optionalPaginationService.get().builder()
                            .title(Text.of(TextColors.DARK_GREEN, displayName + " Limits"))
                            .linesPerPage(10)
                            .padding(Text.of(TextColors.DARK_GREEN, "="))
                            .contents(contents)
                            .sendTo(src);
                } else {
                    src.sendMessage(Text.of(TextColors.RED, "Pagination service not found, printing out limits list:"));
                    for (Text t : contents) {
                        src.sendMessage(t);
                    }
                }
            });
        });

        return CommandResult.success();

    }

}
