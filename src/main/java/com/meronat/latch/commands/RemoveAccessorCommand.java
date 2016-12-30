package com.meronat.latch.commands;

import com.meronat.latch.Latch;
import com.meronat.latch.interactions.ChangeLockInteraction;
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
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.GuavaCollectors;

import java.util.List;
import java.util.UUID;

public class RemoveAccessorCommand implements CommandExecutor {

    private final CommandFlags.Builder flagBuilder = GenericArguments.flags();

    public CommandCallable getCommand() {
        return CommandSpec.builder()
                .description(Text.of("Remove a player from a locked block of yours."))
                .permission("latch.normal.change")
                .executor(this)
                .arguments(GenericArguments.optionalWeak(GenericArguments.allOf(GenericArguments.user(Text.of("remove")))),
                        GenericArguments.optionalWeak(
                        flagBuilder
                                .permissionFlag("latch.normal.persist", "persist", "p")
                                .buildWith(GenericArguments.none())))
                .build();
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {

        if (!(src instanceof Player)) {
            throw new CommandException(Text.of(TextColors.RED, "You must be a player to use this command."));
        }

        Player player = (Player) src;

        List<UUID> members = args.<User>getAll("remove").stream().map(User::getUniqueId).collect(GuavaCollectors.toImmutableList());

        ChangeLockInteraction removePlayers = new ChangeLockInteraction(player.getUniqueId());

        if(members.size() > 0) {
            removePlayers.setMembersToRemove(members);
        } else {
            throw new CommandException(Text.of(TextColors.RED, "You must specify a user to remove."));
        }

        Latch.getLockManager().setInteractionData(player.getUniqueId(), removePlayers);

        player.sendMessage(Text.of(TextColors.DARK_GREEN, "You will remove them on the next lock of yours you click."));

        return CommandResult.success();

    }

}
