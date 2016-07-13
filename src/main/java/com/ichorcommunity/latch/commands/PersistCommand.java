package com.ichorcommunity.latch.commands;

import com.ichorcommunity.latch.Latch;
import com.ichorcommunity.latch.interactions.AbstractLockInteraction;
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

public class PersistCommand implements CommandExecutor {

    private CommandFlags.Builder flagBuilder = GenericArguments.flags();

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

            if(!Latch.lockManager.hasInteractionData(((Player) src).getUniqueId())) {
                ((Player) src).sendMessage(Text.of("You must have run a latch command to persist/clear it."));
                return CommandResult.empty();
            }

            AbstractLockInteraction interaction = Latch.lockManager.getInteractionData(((Player) src).getUniqueId());
            interaction.setPersistance(!interaction.shouldPersist());

            if(interaction.shouldPersist()) {
                ((Player) src).sendMessage(Text.of("Your latch command will now persist."));
            } else {
                Latch.lockManager.removeInteractionData(((Player) src).getUniqueId());
                ((Player) src).sendMessage(Text.of("Your latch command has been cleared."));
            }
            return CommandResult.success();
        }

        return CommandResult.empty();
    }
}
