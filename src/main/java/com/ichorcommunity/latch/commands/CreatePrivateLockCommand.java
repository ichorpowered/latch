package com.ichorcommunity.latch.commands;

import com.ichorcommunity.latch.Latch;
import com.ichorcommunity.latch.enums.LockType;
import com.ichorcommunity.latch.interactions.CreateLockInteraction;
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

public class CreatePrivateLockCommand implements CommandExecutor {

    private CommandFlags.Builder flagBuilder = GenericArguments.flags();

    public CommandCallable getCommand() {
        return CommandSpec.builder()
                .description(Text.of("Create a password lock"))
                .extendedDescription(Text.of(" on the next block placed/clicked."))
                .permission("latch.normal.create.private")
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

            CreateLockInteraction privateLock = new CreateLockInteraction(((Player) src).getUniqueId(), LockType.PRIVATE, "");
            privateLock.setPersistance(args.hasAny("p"));

            Latch.getLockManager().setInteractionData(((Player) src).getUniqueId(), privateLock);

            ((Player) src).sendMessage(Text.of("You will lock the next latchable block you click or place."));

            return CommandResult.success();
        }

        return CommandResult.empty();
    }

}
