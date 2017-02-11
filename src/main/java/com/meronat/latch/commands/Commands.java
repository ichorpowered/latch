/*
 * This file is part of Latch, licensed under the MIT License.
 *
 * Copyright (c) 2016-2017 IchorPowered <https://github.com/IchorPowered>
 * Copyright (c) Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.meronat.latch.commands;

import com.meronat.latch.Latch;
import com.meronat.latch.enums.LockType;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandManager;
import org.spongepowered.api.command.args.CommandFlags;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;

public final class Commands {

    public void register() {

        final CommandFlags.Builder flagBuilder = GenericArguments.flags();

        final CommandSpec addAccessorCommand = CommandSpec.builder()
                .description(Text.of("Add a player to a locked block of yours."))
                .permission("latch.normal.change")
                .executor(new AddAccessorCommand())
                .arguments(GenericArguments.optionalWeak(GenericArguments.onlyOne(GenericArguments.user(Text.of("add")))),
                        GenericArguments.optionalWeak(
                                flagBuilder
                                        .permissionFlag("latch.normal.persist", "persist", "p")
                                        .buildWith(GenericArguments.none())))
                .build();

        final CommandSpec removeAccessorCommand = CommandSpec.builder()
                .description(Text.of("Remove a player from a locked block of yours."))
                .permission("latch.normal.change")
                .executor(new RemoveAccessorCommand())
                .arguments(GenericArguments.optionalWeak(GenericArguments.onlyOne(GenericArguments.user(Text.of("remove")))),
                        GenericArguments.optionalWeak(
                                flagBuilder
                                        .permissionFlag("latch.normal.persist", "persist", "p")
                                        .buildWith(GenericArguments.none())))
                .build();

        final CommandSpec latchInfoCommand = CommandSpec.builder()
                .description(Text.of("Latch info command"))
                .permission("latch.normal.info")
                .executor(new LatchInfoCommand())
                .build();

        final CommandSpec displayLockCommand = CommandSpec.builder()
                .description(Text.of("Display information of a lock"))
                .permission("latch.normal.info")
                .executor(new DisplayLockCommand())
                .arguments(GenericArguments.optionalWeak(
                        flagBuilder
                                .permissionFlag("latch.normal.persist", "persist", "p")
                                .buildWith(GenericArguments.none())))
                .build();

        final CommandSpec adminBypassCommand = CommandSpec.builder()
                .description(Text.of("Put yourself in or remove yourself from admin bypass mode."))
                .permission("latch.admin.bypass")
                .executor(new AdminBypassCommand())
                .build();

        final CommandSpec changeLockCommand = CommandSpec.builder()
                .description(Text.of("Change the attributes of a lock"))
                .permission("latch.normal.change")
                .executor(new ChangeLockCommand())
                .arguments(GenericArguments.optionalWeak(
                        flagBuilder
                                .valueFlag(GenericArguments.string(Text.of("name")), "-name")
                                .valueFlag(GenericArguments.enumValue(Text.of("type"), LockType.class), "-type")
                                .valueFlag(GenericArguments.user(Text.of("owner")), "-owner")
                                .valueFlag(GenericArguments.string(Text.of("password")), "-password")
                                .valueFlag(GenericArguments.user(Text.of("add")), "-add")
                                .valueFlag(GenericArguments.user(Text.of("remove")), "-remove")
                                .valueFlag(GenericArguments.bool(Text.of("redstone")), "-redstone")
                                .permissionFlag("latch.normal.persist", "persist", "p")
                                .buildWith(GenericArguments.none())))
                .build();

        final CommandSpec cleanCommand = CommandSpec.builder()
                .description(Text.of("Purges all locks not accessed in more than a certain amount of days"))
                .permission("latch.admin.clean")
                .executor(new CleanCommand())
                .arguments(GenericArguments.optionalWeak(GenericArguments.onlyOne(GenericArguments.integer(Text.of("days")))))
                .build();

        final CommandSpec donationLockCommand = CommandSpec.builder()
                .description(Text.of("Create a donation lock"))
                .extendedDescription(Text.of(" on the next block placed/clicked."))
                .permission("latch.normal.create.donation")
                .executor(new CreateDonationLockCommand())
                .arguments(GenericArguments.optionalWeak(
                        flagBuilder
                                .permissionFlag("latch.normal.persist", "persist", "p")
                                .buildWith(GenericArguments.none())))
                .build();

        final CommandSpec passwordLockCommand = CommandSpec.builder()
                .description(Text.of("Create a password lock"))
                .extendedDescription(Text.of(" on the next block placed/clicked."))
                .executor(new CreatePasswordLockCommand())
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

        final CommandSpec privateLockCommand = CommandSpec.builder()
                .description(Text.of("Create a password lock"))
                .extendedDescription(Text.of(" on the next block placed/clicked."))
                .permission("latch.normal.create.private")
                .executor(new CreatePrivateLockCommand())
                .arguments(GenericArguments.optionalWeak(GenericArguments.onlyOne(GenericArguments.string(Text.of("name")))),
                        GenericArguments.optionalWeak(flagBuilder
                                .permissionFlag("latch.normal.persist", "persist", "p")
                                .buildWith(GenericArguments.none())))
                .build();

        final CommandSpec publicLockCommand = CommandSpec.builder()
                .description(Text.of("Create a public lock"))
                .extendedDescription(Text.of(" on the next block placed/clicked."))
                .permission("latch.normal.create.public")
                .executor(new CreatePublicLockCommand())
                .arguments(GenericArguments.optionalWeak(
                        flagBuilder
                                .permissionFlag("latch.normal.persist", "persist", "p")
                                .buildWith(GenericArguments.none())))
                .build();

        final CommandSpec limitsCommand = CommandSpec.builder()
                .description(Text.of("List all of a player's limits"))
                .permission("latch.normal.limits")
                .executor(new LimitsCommand())
                .arguments(GenericArguments.optionalWeak(
                        flagBuilder
                                .valueFlag(GenericArguments.enumValue(Text.of("type"), LockType.class), "-type")
                                .buildWith(GenericArguments.none())),
                        GenericArguments.optionalWeak(GenericArguments.user(Text.of("user"))))
                .build();

        final CommandSpec listCommand = CommandSpec.builder()
                .description(Text.of("List all of a player's locks"))
                .permission("latch.normal.list")
                .executor(new ListCommand())
                .arguments(GenericArguments.optionalWeak(
                        flagBuilder
                                .valueFlag(GenericArguments.enumValue(Text.of("type"), LockType.class), "-type")
                                .buildWith(GenericArguments.none())),
                        GenericArguments.optionalWeak(GenericArguments.user(Text.of("owner"))))
                .build();

        final CommandSpec persistCommand = CommandSpec.builder()
                .description(Text.of("Persist or clear a persisted latch command"))
                .permission("latch.normal.persist")
                .executor(new PersistCommand())
                .build();

        final CommandSpec purgeCommand = CommandSpec.builder()
                .description(Text.of("Purge all locks of yourself or a player"))
                .permission("latch.normal.purge")
                .arguments(GenericArguments.optionalWeak(GenericArguments.onlyOne(GenericArguments.user(Text.of("target")))))
                .executor(new PurgeCommand())
                .build();

        final CommandSpec removeLockCommand = CommandSpec.builder()
                .description(Text.of("Remove the next lock clicked"))
                .permission("latch.normal.remove")
                .executor(new RemoveLockCommand())
                .arguments(GenericArguments.optionalWeak(
                        flagBuilder
                                .permissionFlag("latch.normal.persist", "persist", "p")
                                .buildWith(GenericArguments.none())))
                .build();

        final CommandSpec unlockCommand = CommandSpec.builder()
                .description(Text.of("Unlock a password lock"))
                .permission("latch.normal.unlock")
                .executor(new UnlockCommand())
                .arguments(GenericArguments.optionalWeak(
                        flagBuilder
                                .permissionFlag("latch.normal.persist", "persist", "p")
                                .buildWith(GenericArguments.none())),
                        GenericArguments.optionalWeak(GenericArguments.string(Text.of("password"))))
                .build();

        final HelpCommand help = new HelpCommand();

        final CommandSpec helpCommand = CommandSpec.builder()
                .description(Text.of("Latch help command"))
                .permission("latch.normal.help")
                .executor(help)
                .build();

        CommandManager commandManager = Sponge.getCommandManager();

        PluginContainer plugin = Latch.getPluginContainer();

        commandManager.register(plugin, CommandSpec.builder()
                .description(Text.of("The base Latch command."))
                .permission("latch.normal")
                .child(privateLockCommand, "private", "priv")
                .child(donationLockCommand, "donation", "donate")
                .child(publicLockCommand, "public", "public")
                .child(passwordLockCommand, "password", "pass")
                .child(persistCommand, "persist", "clear", "unpersist", "stop", "cancel")
                .child(removeLockCommand, "delete", "removelock")
                .child(changeLockCommand, "change")
                .child(displayLockCommand, "info", "display")
                .child(unlockCommand, "open", "unlock")
                .child(listCommand, "list", "displayall")
                .child(helpCommand, "help", "?")
                .child(addAccessorCommand, "add", "plus")
                .child(removeAccessorCommand, "remove", "minus", "rem", "removeplayer")
                .child(adminBypassCommand, "bypass", "adminbypass", "admin")
                .child(purgeCommand, "purge", "destroyall")
                .child(latchInfoCommand, "version", "authors")
                .child(limitsCommand, "limits", "max")
                .child(cleanCommand, "clean", "misterclean")
                .executor(help)
                .build(), "latch", "lock");

        commandManager.register(plugin, unlockCommand, "unlock", "unlatch", "lunlock", "lopen");
        commandManager.register(plugin, privateLockCommand, "lprivate", "lpriv");
        commandManager.register(plugin, donationLockCommand, "ldonate", "ldonation");
        commandManager.register(plugin, passwordLockCommand, "lpassword", "lpass");
        commandManager.register(plugin, publicLockCommand, "lpublic", "lpub");
        commandManager.register(plugin, displayLockCommand, "linfo", "ldisplay");
        commandManager.register(plugin, listCommand, "llist");
        commandManager.register(plugin, removeLockCommand, "ldelete");
        commandManager.register(plugin, persistCommand, "lpersist", "lclear");
        commandManager.register(plugin, changeLockCommand, "lmodify", "lchange");
        commandManager.register(plugin, purgeCommand, "lpurge");
        commandManager.register(plugin, limitsCommand, "llimits");
        commandManager.register(plugin, cleanCommand, "lclean");
        commandManager.register(plugin, adminBypassCommand, "lbypass", "lby");
        commandManager.register(plugin, addAccessorCommand, "ladd");
        commandManager.register(plugin, removeAccessorCommand, "lremove");
        commandManager.register(plugin, helpCommand, "lhelp");
        commandManager.register(plugin, latchInfoCommand, "llatch", "lversion");

    }

    private static Commands ourInstance = new Commands();

    public static Commands getCommands() {
        return ourInstance;
    }

    private Commands() {}

}
