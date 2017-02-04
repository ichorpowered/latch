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
import com.meronat.latch.interactions.LockInteraction;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class PersistCommand implements CommandExecutor {

    public CommandCallable getCommand() {
        return CommandSpec.builder()
                .description(Text.of("Persist or clear a persisted latch command"))
                .permission("latch.normal.persist")
                .executor(this)
                .build();
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {

        if(src instanceof Player) {

            Player player = (Player) src;

            if(!Latch.getLockManager().hasInteractionData(player.getUniqueId())) {
                throw new CommandException(Text.of(TextColors.RED, "You must have run a latch command to persist/clear it."));
            }

            LockInteraction interaction = Latch.getLockManager().getInteractionData(player.getUniqueId());
            interaction.setPersistence(!interaction.shouldPersist());

            if(interaction.shouldPersist()) {
                player.sendMessage(Text.of(TextColors.DARK_GREEN, "Your latch command will now persist."));
            } else {
                Latch.getLockManager().removeInteractionData(((Player) src).getUniqueId());
                player.sendMessage(Text.of(TextColors.DARK_GREEN, "Your latch command has been cleared."));
            }

            return CommandResult.success();

        }

        throw new CommandException(Text.of(TextColors.RED, "You must be a player to use this command."));

    }

}
