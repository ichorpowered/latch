package com.ichorcommunity.latch.commands;

import com.ichorcommunity.latch.Latch;
import com.ichorcommunity.latch.interactions.DeleteLockInteraction;
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

public class RemoveLockCommand implements CommandExecutor {

    private CommandFlags.Builder flagBuilder = GenericArguments.flags();

    public CommandCallable getCommand() {
        return CommandSpec.builder()
                .description(Text.of("Remove the next lock clicked"))
                .permission("latch.normal.remove")
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

            DeleteLockInteraction deleteLock = new DeleteLockInteraction(((Player) src).getUniqueId());
            deleteLock.setPersistance(args.hasAny("p"));

            Latch.getLockManager().setInteractionData(((Player) src).getUniqueId(), deleteLock);

            ((Player) src).sendMessage(Text.of("You will remove the next lock you click."));

            return CommandResult.success();
        }

        return CommandResult.empty();
    }
}
