package com.ichorcommunity.latch.commands;

import org.apache.commons.lang3.StringUtils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BaseCommand implements CommandExecutor {

    Map<List<String>, CommandSpec> children = new HashMap<>();

    public final CommandSpec baseCommand = CommandSpec.builder()
            .description(Text.of("The base Latch command."))
            .permission("latch.normal")
            .child(new CreatePrivateLockCommand().getCommand(), "private")
            .child(new CreatePasswordLockCommand().getCommand(), "password")
            .child(new PersistCommand().getCommand(), "persist", "clear", "unpersist")
            .child(new RemoveLockCommand().getCommand(), "remove", "rem")
            .executor(this)
            .build();

    @Override
    public CommandResult execute(@Nonnull CommandSource source, CommandContext args) throws CommandException {
        Text help = Text.builder("Click here for ")
                .color(TextColors.GRAY)
                .onClick(TextActions.runCommand("latch help"))
                .onHover(TextActions.showText(Text.of("/latch help")))
                .append(Text.builder("Latch")
                        .color(TextColors.DARK_GREEN)
                        .onClick(TextActions.runCommand("/latch help"))
                        .onHover(TextActions.showText(Text.of("/latch help")))
                        .append(Text.builder(" help.")
                                .color(TextColors.GRAY)
                                .onClick(TextActions.runCommand("/latch help"))
                                .onHover(TextActions.showText(Text.of("/latch help")))
                                .build())
                        .build())
                .build();

        source.sendMessage(Text.of(TextColors.DARK_GREEN, Sponge.getPluginManager().getPlugin("latch").get().getName(), " v", Sponge.getPluginManager().getPlugin("latch").get().getVersion().get()));
        source.sendMessage(Text.of(TextColors.GRAY, "Created by ", StringUtils.join(Sponge.getPluginManager().getPlugin("latch").get().getAuthors(), ", ")));
        source.sendMessage(help);

        return CommandResult.success();
    }
}
