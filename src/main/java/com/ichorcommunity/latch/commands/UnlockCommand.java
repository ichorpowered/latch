package com.ichorcommunity.latch.commands;

import com.ichorcommunity.latch.Latch;
import com.ichorcommunity.latch.interactions.UnlockLockInteraction;
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

import java.util.Optional;

public class UnlockCommand implements CommandExecutor {

    private CommandFlags.Builder flagBuilder = GenericArguments.flags();

    public CommandCallable getCommand() {
        return CommandSpec.builder()
                .description(Text.of("Unlock a password lock"))
                .permission("latch.normal.unlock")
                .executor(this)
                .arguments(GenericArguments.optionalWeak(
                        flagBuilder
                                .permissionFlag("latch.normal.persist", "persist", "p")
                                .buildWith(GenericArguments.none())),
                        GenericArguments.optionalWeak(GenericArguments.string(Text.of("password"))))
                .build();
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {

        if(src instanceof Player) {

            Optional<String> password = args.<String>getOne("password");

            if(!password.isPresent())  {
                ((Player) src).sendMessage(Text.of("You must specify a password to unlock locks."));
                return CommandResult.empty();
            }

            UnlockLockInteraction unlockLock = new UnlockLockInteraction(((Player) src).getUniqueId(), password.get());

            unlockLock.setPersistance(args.hasAny("p"));

            Latch.lockManager.setInteractionData(((Player) src).getUniqueId(), unlockLock);

            ((Player) src).sendMessage(Text.of("You will unlock the next lock you click."));

            return CommandResult.success();
        }

        return CommandResult.empty();
    }

}
