package com.ichorcommunity.latch.commands;

import com.ichorcommunity.latch.Latch;
import com.ichorcommunity.latch.entities.Lock;
import com.ichorcommunity.latch.enums.LockType;
import com.ichorcommunity.latch.utils.LatchUtils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandFlags;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.ArrayList;
import java.util.List;

public class ListCommand implements CommandExecutor {
    private CommandFlags.Builder flagBuilder = GenericArguments.flags();

    public CommandCallable getCommand() {
        return CommandSpec.builder()
                .description(Text.of("List all of a player's locks"))
                .permission("latch.normal.list")
                .executor(this)
                .arguments(GenericArguments.optionalWeak(
                        flagBuilder
                                .valueFlag(GenericArguments.enumValue(Text.of("type"), LockType.class), "-type")
                                .buildWith(GenericArguments.none())),
                        GenericArguments.userOrSource(Text.of("owner")))
                .build();
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {

        List<Text> contents = new ArrayList<>();

        User userToUse = args.<User>getOne("owner").get();

        //If the user doesn't have the permission to check someone else's lock, default to them
        if( src instanceof User && !src.hasPermission("latch.admin.list")) {
            userToUse = (User) src;
        } else {
            src.sendMessage(Text.of("You don't have permission to use this command."));
            return CommandResult.empty();
        }

        String displayName = src.getName().equalsIgnoreCase(userToUse.getName()) ? "Your" : userToUse.getName()+"'s";

        List<Lock> locks = Latch.getLockManager().getPlayersLocks(userToUse.getUniqueId());

        for(Lock lock : locks) {
            String location = lock.getFirstLocation().isPresent() ? LatchUtils.getLocationString(lock.getFirstLocation().get()) : "N/A";
            contents.add(Text.of("  ",lock.getName(),
                    TextColors.GRAY,", type: ",
                    TextColors.WHITE,lock.getLockType(),
                    TextColors.GRAY,", location: ",
                    TextColors.WHITE, location));
        }

        Sponge.getServiceManager().provide(PaginationService.class).get().builder()
                .title(Text.of(TextColors.GRAY, "[ ", TextColors.DARK_GREEN, displayName + " Locks", TextColors.GRAY, " ]"))
                .header(Text.of(TextColors.GRAY, "There are ", TextColors.WHITE, locks.size(), TextColors.GRAY, " lock(s):"))
                .linesPerPage(10)
                .padding(Text.of(TextColors.GRAY, "="))
                .contents(contents)
                .sendTo(src);

        return CommandResult.success();
    }

}