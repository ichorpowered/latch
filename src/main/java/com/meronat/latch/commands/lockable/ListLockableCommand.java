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

package com.meronat.latch.commands.lockable;

import com.meronat.latch.Latch;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ListLockableCommand implements CommandExecutor {

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) {
        Sponge.getScheduler().createAsyncExecutor(Latch.getPluginContainer()).execute(() -> {
            final Optional<PaginationService> optionalPaginationService = Sponge.getServiceManager().provide(PaginationService.class);

            final List<Text> contents = Latch.getLockManager().getLockableBlocks().stream().sorted().map(i ->
                    Text.of(TextColors.GREEN, i)).collect(Collectors.toList());

            if (optionalPaginationService.isPresent()) {
                optionalPaginationService.get().builder()
                        .title(Text.of(TextColors.DARK_GREEN, "Lockable Blocks"))
                        .linesPerPage(10)
                        .padding(Text.of(TextColors.GRAY, "="))
                        .contents(contents)
                        .sendTo(src);
            } else {
                src.sendMessage(Text.of(TextColors.RED, "Pagination service not found, printing out lockable blocks manually:"));
                for (Text t : contents) {
                    src.sendMessage(t);
                }
            }
        });

        return CommandResult.success();
    }

}
