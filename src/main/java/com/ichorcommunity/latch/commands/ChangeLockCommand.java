package com.ichorcommunity.latch.commands;

import com.ichorcommunity.latch.Latch;
import com.ichorcommunity.latch.enums.LockType;
import com.ichorcommunity.latch.interactions.ChangeLockInteraction;
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

import java.util.Collection;
import java.util.Optional;

public class ChangeLockCommand implements CommandExecutor {

    private CommandFlags.Builder flagBuilder = GenericArguments.flags();

    public CommandCallable getCommand() {
        return CommandSpec.builder()
                .description(Text.of("Change the attributes of a lock"))
                .permission("latch.normal.change")
                .executor(this)
                .arguments(GenericArguments.optionalWeak(
                        flagBuilder
                                .valueFlag(GenericArguments.string(Text.of("name")), "-name")
                                .valueFlag(GenericArguments.enumValue(Text.of("type"), LockType.class), "-type")
                                .valueFlag(GenericArguments.user(Text.of("owner")), "-owner")
                                .valueFlag(GenericArguments.string(Text.of("password")), "-password")
                                .valueFlag(GenericArguments.user(Text.of("add")), "-add")
                                .valueFlag(GenericArguments.user(Text.of("remove")), "-remove")
                                .permissionFlag("latch.normal.persist", "persist", "p")
                                .buildWith(GenericArguments.none())))
                .build();
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {

        if(src instanceof Player) {

            if( !(args.hasAny("name") || args.hasAny("type") || args.hasAny("owner") || args.hasAny("password") || args.hasAny("add") || args.hasAny("remove")) ) {
                ((Player) src).sendMessage(Text.of("You must specify at least one attribute to change."));
                return CommandResult.empty();
            }

            ChangeLockInteraction changeLock = new ChangeLockInteraction(((Player) src).getUniqueId());

            Optional<String> optionalString = args.<String>getOne("name");
            if(optionalString.isPresent()) {
                changeLock.setLockName(optionalString.get());
            }

            Optional<LockType> optionalType = args.<LockType>getOne("type");
            if(optionalType.isPresent()) {
                changeLock.setType(optionalType.get());
            }

            Optional<User> optionalUser = args.<User>getOne("owner");
            if(optionalUser.isPresent()) {
                changeLock.setNewOwner(optionalUser.get().getUniqueId());
            }

            optionalString = args.<String>getOne("password");
            if(optionalString.isPresent()) {
                changeLock.setPassword(optionalString.get());
            }

            Collection<User> members = args.<User>getAll("add");
            if(members.size() > 0) {
                changeLock.setMembersToAdd(members);
            }

             members = args.<User>getAll("remove");
            if(members.size() > 0) {
                changeLock.setMembersToRemove(members);
            }

            changeLock.setPersistance(args.hasAny("p"));

            Latch.getLockManager().setInteractionData(((Player) src).getUniqueId(), changeLock);

            ((Player) src).sendMessage(Text.of("You will change the next lock you click."));

            return CommandResult.success();
        }

        return CommandResult.empty();
    }

}
