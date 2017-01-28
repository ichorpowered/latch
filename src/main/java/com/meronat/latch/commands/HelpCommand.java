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
import com.meronat.latch.utils.LatchUtils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class HelpCommand implements CommandExecutor {

    public CommandCallable getCommand() {
        return CommandSpec.builder()
                .description(Text.of("Latch help command"))
                .permission("latch.normal.help")
                .executor(this)
                .arguments(GenericArguments.none())
                .build();
    }

    @Override
    public CommandResult execute(@Nonnull CommandSource src, CommandContext args) throws CommandException {

        List<Text> contents = new ArrayList<>();

        contents.add(LatchUtils.formatHelpText("/latch private","Create a private lock",
                Text.of("Add -p to persist")));

        contents.add(LatchUtils.formatHelpText("/latch password [password]", "Create a password lock with the specified password",
                Text.of("Add -p to persist", Text.NEW_LINE,"Add -o to only require a password the first time")));

        contents.add(LatchUtils.formatHelpText("/latch donation", "Create a donation chest which everyone can add to",
                Text.of("Add -p to persist")));

        Text changeHelp = Text.of("--name=[name] to rename the lock", Text.NEW_LINE,
                "--type=[PRIVATE, PASSWORD_ALWAYS, PASSWORD_ONCE] to change the lock type", Text.NEW_LINE,
                "--password=[password] change the password of the lock (resets access list)", Text.NEW_LINE,
                "--add=[player] add the player to the lock access list", Text.NEW_LINE,
                "--remove=[player] remove the player from the lock access list", Text.NEW_LINE,
                "--owner=[player] give the lock to another player");

        if (Latch.getConfig().getNode("protect_from_redstone").getBoolean(false)) {

            changeHelp = Text.of("--name=[name] to rename the lock", Text.NEW_LINE,
                    "--type=[PRIVATE, PASSWORD_ALWAYS, PASSWORD_ONCE] to change the lock type", Text.NEW_LINE,
                    "--password=[password] change the password of the lock (resets access list)", Text.NEW_LINE,
                    "--add=[player] add the player to the lock access list", Text.NEW_LINE,
                    "--remove=[player] remove the player from the lock access list", Text.NEW_LINE,
                    "--owner=[player] give the lock to another player", Text.NEW_LINE,
                    "--redstone=[true/false] enable or disable redstone protection");

        }

        contents.add(LatchUtils.formatHelpText("/latch change", "Change the attributes of one of your locks (hover for flags)",
                changeHelp));

        contents.add(LatchUtils.formatHelpText("/latch delete", "Remove a lock you're the owner of",
                Text.of("Add -p to persist")));

        contents.add(LatchUtils.formatHelpText("/latch persist", "Continue applying the last Latch command run on block click/place",
                Text.of("Run again or /latch stop to stop applying the last Latch command")));

        contents.add(LatchUtils.formatHelpText("/latch info", "Display information about the next lock clicked",
                Text.of("Add -p to persist")));

        contents.add(LatchUtils.formatHelpText("/latch list", "List all of your locks",
                Text.of("/latch list [player] to list another player's (if you have permission)")));

        contents.add(LatchUtils.formatHelpText("/unlock [password]", "Attempt to open a lock with this password",
                Text.of("Or use /latch open [password]")));

        contents.add(LatchUtils.formatHelpText("/latch add [user]", "Adds the specified user to one of your locks",
                Text.of("Add -p to persist")));

        contents.add(LatchUtils.formatHelpText("/latch remove [user]", "Removes the specified user from one of your locks",
                Text.of("Add -p to persist")));

        contents.add(LatchUtils.formatHelpText("/latch purge", "Purge all of your or all of a player's locks",
                Text.of("People with the proper permission can specify a user")));

        contents.add(LatchUtils.formatHelpText("/latch bypass", "Allows admins to enter bypass mode and access other's locks.",
                Text.of("Run again or log off to leave bypass mode.")));

        Sponge.getServiceManager().provide(PaginationService.class).get().builder()
                .title(Text.of(TextColors.DARK_GREEN, "Latch Help"))
                .linesPerPage(15)
                .padding(Text.of(TextColors.GRAY, "="))
                .contents(contents)
                .sendTo(src);

        return CommandResult.success();

    }

}
