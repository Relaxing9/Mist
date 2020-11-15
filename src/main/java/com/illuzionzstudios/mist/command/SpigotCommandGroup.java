/**
 * Copyright © 2020 Property of Illuzionz Studios, LLC
 * All rights reserved. No part of this publication may be reproduced, distributed, or
 * transmitted in any form or by any means, including photocopying, recording, or other
 * electronic or mechanical methods, without the prior written permission of the publisher,
 * except in the case of brief quotations embodied in critical reviews and certain other
 * noncommercial uses permitted by copyright law. Any licensing of this software overrides
 * this statement.
 */
package com.illuzionzstudios.mist.command;

import com.illuzionzstudios.mist.Mist;
import com.illuzionzstudios.mist.config.locale.Locale;
import com.illuzionzstudios.mist.plugin.SpigotPlugin;
import com.illuzionzstudios.mist.util.Valid;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * Contains a group of commands for execution. Contains the main command,
 * for instance "/customfishing", and the subs for that command, eg "/customfishing rewards"
 * allows us to group functionality for commands and interact with each other
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class SpigotCommandGroup {

    /**
     * The {@link SpigotSubCommand} that belong to this group
     */
    @Getter
    protected final HashSet<SpigotSubCommand> subCommands = new HashSet<>();

    /**
     * The main {@link SpigotCommand} which {@link SpigotSubCommand} are
     * executed through
     */
    protected SpigotCommand mainCommand;

    //  -------------------------------------------------------------------------
    //  Registration
    //  -------------------------------------------------------------------------

    /**
     * Register this command group with the main label,
     * then add sub commands to it
     *
     * @param labels List of main label and aliases
     */
    public final void register(String... labels) {
        Valid.checkBoolean(!isRegistered(), "Main command was already registered as: " + mainCommand);

        // Set command
        mainCommand = new MainCommand(labels[0]);

        // Set the aliases
        if (labels.length > 1)
            mainCommand.setAliases(Arrays.asList(Arrays.copyOf(labels, 1)));

        // Register it
        mainCommand.register();

        // Register sub commands
        registerSubcommands();
    }

    /**
     * Unregister the main command and all aliases
     */
    public final void unregister() {
        Valid.checkBoolean(isRegistered(), "Main command not registered!");

        mainCommand.unregister();
        mainCommand = null;

        subCommands.clear();
    }

    /**
     * Register our sub commands to this command group
     */
    public abstract void registerSubcommands();

    /**
     * @param command Add a {@link SpigotSubCommand} to this group
     */
    protected final void registerSubCommand(SpigotSubCommand command) {
        Valid.checkNotNull(mainCommand, "Cannot add subcommands when main command is missing! Call register()");
        Valid.checkBoolean(!subCommands.contains(command), "Subcommand /" + mainCommand.getLabel() + " " + command.getSubLabel() + " already registered!");

        subCommands.add(command);
    }

    /**
     * Get the label for this command group, failing if not yet registered
     *
     * @return The string label
     */
    public final String getLabel() {
        Valid.checkBoolean(isRegistered(), "Main command has not yet been set!");

        return mainCommand.getMainLabel();
    }

    /**
     * Has the command group been registered yet?
     *
     * @return If main command is set
     */
    public final boolean isRegistered() {
        return mainCommand != null;
    }

    //  -------------------------------------------------------------------------
    //  Functions
    //  -------------------------------------------------------------------------

    /**
     * Return which subcommands should trigger the automatic help
     * menu that shows all subcommands sender has permission for.
     *
     * Default: help and ?
     *
     * @return List of labels
     */
    protected List<String> getHelpLabel() {
        return Arrays.asList("help", "?");
    }

    /**
     * Return the header messages used in /{label} help|? typically
     * used to tell all available subcommands from this command group
     *
     * @return String array of messages
     */
    protected String[] getHelpHeader() {
        return new String[] {
                "&8",
                "&8" + Mist.SMOOTH_LINE,
                getHeaderPrefix() + "  " + SpigotPlugin.getPluginName() + " &7v" + SpigotPlugin.getPluginVersion()
                + (!SpigotPlugin.getInstance().getDescription().getAuthors().isEmpty() ? " by " + SpigotPlugin.getInstance().getDescription().getAuthors().get(0) :
                        ""),
                " ",
                "&2  [] &7= " + Locale.Command.LABEL_OPTIONAL_ARGS,
                "&6  <> &7= " + Locale.Command.LABEL_REQUIRED_ARGS,
                " "
        };
    }

    /**
     * Return the default color in the {@link #getHelpHeader()},
     * LIGHT_PURPLE + BOLD colors by default
     *
     * @return Header prefix colours
     */
    protected String getHeaderPrefix() {
        return "" + ChatColor.LIGHT_PURPLE + ChatColor.BOLD;
    }


    //  -------------------------------------------------------------------------
    //  Execution
    //  -------------------------------------------------------------------------

    /**
     * Handles our main command to detect sub commands and run functionality
     */
    private final class MainCommand extends SpigotCommand {

        /**
         * @param label Create the main command with the main label
         */
        public MainCommand(String label) {
            super(label);

            // Let everyone view info
            setPermission(null);

            setAutoHandleHelp(false);
        }

        /**
         * Here we handle our main help, showing sub commands etc.
         * Also handle execution of sub commands
         */
        @Override
        protected void onCommand() {
            // Show our help
            // Assures arg[0] isn't null
            if (args.length == 0 || (args.length >= 1 && getHelpLabel().contains(args[0]))) {
                tellSubCommandsHelp();
                return;
            }

            final String subArg = args[0];
            final SpigotSubCommand command = findSubcommand(subArg);

            // Attempt to run
            if (command != null) {
                command.setSubLabel(subArg);

                // Run the command
                command.execute(sender, getLabel(), args.length == 1 ? new String[] {} : Arrays.copyOfRange(args, 1, args.length));
            } else {
                // Couldn't find sub command
                tell(Locale.Command.INVALID_SUB.replace("{label}", getMainLabel()));
            }
        }

        /**
         * Inform help on sub commands for the player
         */
        private void tellSubCommandsHelp() {
            // Send the header
            tell(getHelpHeader());

            // Amount of subs shown
            int shown = 0;

            for (final SpigotSubCommand subcommand : subCommands) {
                if (subcommand.showInHelp() && hasPerm(subcommand.getPermission())) {

                    final String usage = colorizeUsage(subcommand.getUsage());
                    final String desc = subcommand.getDescription() != null ? subcommand.getDescription() : "";

                    tell("  &7/" + getLabel() + " " + subcommand.getSubLabels()[0] + (!usage.startsWith("/") ? " " + usage : "") + (!desc.isEmpty() ? "&e- " + desc : ""));

                    shown++;
                }
            }

            // End line
            tell("&8" + Mist.SMOOTH_LINE);
        }

        /**
         * Replaces some usage parameters such as <> or [] with colorized brackets
         *
         * @param message Message to colorize
         * @return Formatted message
         */
        private String colorizeUsage(String message) {
            return message == null ? "" : message.replace("<", "&6<").replace(">", "&6>&7").replace("[", "&2[").replace("]", "&2]&7");
        }

        /**
         * Finds a subcommand by label
         *
         * @param label Label to search by
         * @return If a subcommand contains that label as main
         *         or an aliases, return that
         */
        private SpigotSubCommand findSubcommand(String label) {
            for (final SpigotSubCommand command : subCommands) {
                for (final String alias : command.getSubLabels())
                    if (alias.equalsIgnoreCase(label))
                        return command;
            }

            return null;
        }

        /**
         * Handle tabcomplete for subcommands and their tabcomplete
         */
        @Override
        public List<String> tabComplete() {
            if (args.length == 1)
                return tabCompleteSubcommands(sender, args[0]);

            if (args.length > 1) {
                final SpigotSubCommand cmd = findSubcommand(args[0]);

                if (cmd != null)
                    return cmd.tabComplete(sender, getLabel(), Arrays.copyOfRange(args, 1, args.length));
            }

            return null;
        }

        /**
         * Automatically tab-complete subcommands
         */
        private List<String> tabCompleteSubcommands(CommandSender sender, String param) {
            param = param.toLowerCase();

            final List<String> tab = new ArrayList<>();

            for (final SpigotSubCommand subcommand : subCommands)
                if (hasPerm(subcommand.getPermission()))
                    for (final String label : subcommand.getSubLabels())
                        if (!label.trim().isEmpty() && label.startsWith(param))
                            tab.add(label);

            return tab;
        }
    }

}
