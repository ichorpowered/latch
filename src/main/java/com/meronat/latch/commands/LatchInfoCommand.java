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

import com.meronat.latch.Info;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

import java.net.MalformedURLException;
import java.net.URL;

public class LatchInfoCommand implements CommandExecutor {

    private static final URL ICHOR_URL;

    static {
        try {
            ICHOR_URL = new URL("http://ichorpowered.com");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private static final Text INFO_TEXT =
            Text.of(TextColors.DARK_GREEN, "Latch", " v", Info.VERSION, Text.NEW_LINE)
                    .concat(Text.of(TextColors.GRAY, "Created by "))
                    .concat(Text.builder("IchorPowered")
                            .color(TextColors.GOLD)
                            .onHover(TextActions.showText(Text.of(TextColors.GRAY, "Visit the IchorPowered website.")))
                            .onClick(ICHOR_URL != null ? TextActions.openUrl(ICHOR_URL) : TextActions.suggestCommand("Error getting url."))
                            .append(Text.NEW_LINE)
                            .build())
                    .concat(Text.builder("Click here for ")
                            .color(TextColors.GRAY)
                            .onClick(TextActions.runCommand("/latch help"))
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

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        src.sendMessage(INFO_TEXT);

        return CommandResult.success();
    }

}
