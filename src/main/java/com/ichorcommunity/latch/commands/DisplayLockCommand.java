package com.ichorcommunity.latch.commands;

import com.ichorcommunity.latch.Latch;
import com.ichorcommunity.latch.interactions.DisplayLockInteraction;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandFlags;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

public class DisplayLockCommand implements CommandExecutor {

    private CommandFlags.Builder flagBuilder = GenericArguments.flags();

    public CommandCallable getCommand() {
        return CommandSpec.builder()
                .description(Text.of("Display information of a lock"))
                .permission("latch.normal.display")
                .executor(this)
                .arguments(GenericArguments.optionalWeak(
                        flagBuilder
                                .permissionFlag("latch.normal.persist", "persist", "p")
                                .buildWith(GenericArguments.none())))
                .build();
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {

        if(src instanceof Player) {

            DisplayLockInteraction displayLock = new DisplayLockInteraction(((Player) src).getUniqueId());
            displayLock.setPersistance(args.hasAny("p"));

            Latch.getLockManager().setInteractionData(((Player) src).getUniqueId(), displayLock);

            ((Player) src).sendMessage(Text.of("You will display information of the next lock you click."));

            return CommandResult.success();
        }

        return CommandResult.empty();
    }

}
