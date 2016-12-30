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

import org.apache.commons.lang3.StringUtils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

import javax.annotation.Nonnull;

public class BaseCommand implements CommandExecutor {

    private final CommandSpec baseCommand = CommandSpec.builder()
            .description(Text.of("The base Latch command."))
            .permission("latch.normal")
            .child(new CreatePrivateLockCommand().getCommand(), "private")
            .child(new CreateDonationLockCommand().getCommand(), "donation")
            .child(new CreatePublicLockCommand().getCommand(), "public")
            .child(new CreatePasswordLockCommand().getCommand(), "password")
            .child(new PersistCommand().getCommand(), "persist", "clear", "unpersist", "stop", "cancel")
            .child(new RemoveLockCommand().getCommand(), "remove", "rem")
            .child(new ChangeLockCommand().getCommand(), "change")
            .child(new DisplayLockCommand().getCommand(), "info", "display")
            .child(new UnlockCommand().getCommand(), "open", "unlock")
            .child(new ListCommand().getCommand(), "list", "displayall")
            .child(new HelpCommand().getCommand(), "help")
            .executor(this)
            .build();

    @Override
    public CommandResult execute(@Nonnull CommandSource source, CommandContext args) throws CommandException {

        source.sendMessage(Text.of(TextColors.DARK_GREEN, "Latch", " v", Sponge.getPluginManager().getPlugin("latch").get().getVersion().get()));
        source.sendMessage(Text.of(TextColors.GRAY, "Created by ", StringUtils.join(Sponge.getPluginManager().getPlugin("latch").get().getAuthors(), ", ")));
        source.sendMessage(Text.builder("Click here for ")
                .color(TextColors.GRAY)
                .onClick(TextActions.runCommand("latch help"))
                .onHover(TextActions.showText(Text.of("/latch help")))
                .append(Text.builder("Latch")
                        .color(TextColors.DARK_GREEN)
                        .onClick(TextActions.runCommand("/latch help"))
                        .onHover(TextActions.showText(Text.of("/latch help")))
                        .append(Text.builder(" help.")
                                .color(TextColors.GRAY)
                                .onClick(TextActions.runCommand("/latch help"))
                                .onHover(TextActions.showText(Text.of("/latch help")))
                                .build()).build()).build());

        return CommandResult.success();
    }

    public CommandSpec getCommand() {

        return this.baseCommand;

    }
}
