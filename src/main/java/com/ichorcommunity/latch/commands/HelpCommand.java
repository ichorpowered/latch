package com.ichorcommunity.latch.commands;

import com.ichorcommunity.latch.utils.LatchUtils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class HelpCommand implements CommandExecutor {

    public CommandCallable getCommand() {
        return CommandSpec.builder()
                .description(Text.of("Latch help command"))
                .permission("latch.normal.help")
                .executor(this)
                .arguments(GenericArguments.none())
                .build();
    }

    @Override
    public CommandResult execute(@Nonnull CommandSource src, CommandContext args) throws CommandException {
        List<Text> contents = new ArrayList<>();

        contents.add(LatchUtils.formatHelpText("/latch private","Create a private lock",
                Text.builder().append(Text.of("Add -p to persist")).build()));

        contents.add(LatchUtils.formatHelpText("/latch password [password]", "Create a password lock with the specified password",
                Text.builder().append(Text.of("Add -p to persist",Text.NEW_LINE,"Add -o to only require a password the first time")).build()));

        contents.add(LatchUtils.formatHelpText("/latch change", "Change the attributes of one of your locks (hover for flags)",
                Text.builder().append(Text.of("--name=[name] to rename the lock",Text.NEW_LINE,
                        "--type=[PRIVATE, PASSWORD_ALWAYS, PASSWORD_ONCE] to change the lock type",Text.NEW_LINE,
                        "--password=[password] change the password of the lock (resets access list)",Text.NEW_LINE,
                        "--add=[player] add the player to the lock access list",Text.NEW_LINE,
                        "--remove=[player] remove the player from the lock access list",Text.NEW_LINE,
                        "--owner=[player] give the lock to another player")).build()));

        contents.add(LatchUtils.formatHelpText("/latch remove", "Remove a lock you're the owner of",
                Text.builder().append(Text.of("Add -p to persist")).build()));

        contents.add(LatchUtils.formatHelpText("/latch persist", "Continue applying the last Latch command run on block click/place",
                Text.builder().append(Text.of("Run again or /latch stop to stop applying the last Latch command")).build()));

        contents.add(LatchUtils.formatHelpText("/latch info", "Display information about the next lock clicked",
                Text.builder().append(Text.of("Add -p to persist")).build()));

        contents.add(LatchUtils.formatHelpText("/latch list", "List all of your locks",
                Text.builder().append(Text.of("/latch list [player] to list another player's (if you have permission)")).build()));

        contents.add(LatchUtils.formatHelpText("/unlock [password]", "Attempt to open a lock with this password",
                Text.builder().append(Text.of("Or use /latch open [password]")).build()));

        Sponge.getServiceManager().provide(PaginationService.class).get().builder()
                .title(Text.of(TextColors.GRAY, "[ ", TextColors.DARK_GREEN, "Latch Help", TextColors.GRAY, " ]"))
                .linesPerPage(15)
                .padding(Text.of(TextColors.GRAY, "="))
                .contents(contents)
                .sendTo(src);

        return CommandResult.success();
    }
}
