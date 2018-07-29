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
import com.meronat.latch.entities.LockManager;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Optional;

public class AddLockableCommand implements CommandExecutor {

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        final Optional<BlockType> command = args.getOne("entered");

        final BlockType blockType;

        if (command.isPresent()) {
            blockType = command.get();
        } else {
            if (src instanceof Player) {
                final ItemType type = ((Player) src).getItemInHand(HandTypes.MAIN_HAND).orElseThrow(() -> new CommandException(
                        Text.of(TextColors.RED, "You must either specify a block type or have an item in your hand!"))).getType();

                if (type.equals(ItemTypes.AIR) || type.equals(ItemTypes.NONE)) {
                    throw new CommandException(Text.of(TextColors.RED, "You must either specify a block type or have an item in your hand!"));
                }

                blockType = type.getBlock().orElseThrow(() ->
                                new CommandException(Text.of(TextColors.RED, "That item you are holding does not have a block representation!")));
            } else {
                throw new CommandException(Text.of(TextColors.RED, "You must specify a block type if you are not a player!"));
            }
        }

        final LockManager lockManager = Latch.getLockManager();

        if (lockManager.isLockableBlock(blockType)) {
            throw new CommandException(Text.of(TextColors.RED, blockType.getId() + " is already a lockable block!"));
        }

        if (!lockManager.addLockableBlock(blockType)) {
            throw new CommandException(Text.of(TextColors.RED, "There was a problem adding " + blockType.getId() + " as a lockable block!"));
        }

        src.sendMessage(Text.of(TextColors.DARK_GREEN, blockType.getId() + " is now a lockable block!"));

        return CommandResult.success();
    }

}
