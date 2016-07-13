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

import java.util.Optional;

public class CreatePasswordLockCommand implements CommandExecutor {

    private CommandFlags.Builder flagBuilder = GenericArguments.flags();

    public CommandCallable getCommand() {
        return CommandSpec.builder()
                .description(Text.of("Create a password lock"))
                .extendedDescription(Text.of(" on the next block placed/clicked."))
                .executor(this)
                .arguments(
                        GenericArguments.optionalWeak(
                                flagBuilder
                                        .permissionFlag("latch.normal.create.password.always", "always", "a")
                                        .permissionFlag("latch.normal.create.password.once", "once", "o")
                                        .permissionFlag("latch.normal.persist", "persist", "p")
                                        .buildWith(GenericArguments.none())),
                        GenericArguments.optionalWeak(GenericArguments.string(Text.of("password")))
                )
                .build();
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {

        if(src instanceof Player) {
            //Check for password
            Optional<String> password = args.<String>getOne("password");

            if(!password.isPresent()) {
                ((Player) src).sendMessage(Text.of("Missing a password: /latch password [password]"));
                return CommandResult.empty();
            }

            //Default to the always password if no flag present
            LockType typeToUse = LockType.PASSWORD_ALWAYS;

            if(args.hasAny("o")) {
                typeToUse= LockType.PASSWORD_ONCE;
            }

            CreateLockInteraction passwordLock = new CreateLockInteraction(((Player) src).getUniqueId(), typeToUse, password.get());
            passwordLock.setPersistance(args.hasAny("p"));

            Latch.lockManager.setInteractionData(((Player) src).getUniqueId(), passwordLock);

            ((Player) src).sendMessage(Text.of("You will lock the next latchable block you click or place."));

            return CommandResult.success();
        }

        return CommandResult.empty();
    }
}
