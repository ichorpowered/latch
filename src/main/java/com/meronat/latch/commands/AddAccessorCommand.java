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

public class AddAccessorCommand implements CommandExecutor {

    private final CommandFlags.Builder flagBuilder = GenericArguments.flags();

    public CommandCallable getCommand() {
        return CommandSpec.builder()
                .description(Text.of("Add a player to a locked block of yours."))
                .permission("latch.normal.change")
                .executor(this)
                .arguments(GenericArguments.optionalWeak(GenericArguments.onlyOne(GenericArguments.user(Text.of("add")))),
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

        List<UUID> members = args.<User>getAll("add").stream().map(User::getUniqueId).collect(GuavaCollectors.toImmutableList());

        ChangeLockInteraction addPlayers = new ChangeLockInteraction(player.getUniqueId());

        if(members.size() > 0) {
            addPlayers.setMembersToAdd(members);
        } else {
            throw new CommandException(Text.of(TextColors.RED, "You must specify a user to add."));
        }

        Latch.getLockManager().setInteractionData(player.getUniqueId(), addPlayers);

        player.sendMessage(Text.of(TextColors.DARK_GREEN, "You will add them on the next lock of yours you click."));

        return CommandResult.success();

    }

}
