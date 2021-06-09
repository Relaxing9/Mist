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

import com.illuzionzstudios.mist.Logger;
import com.illuzionzstudios.mist.command.response.ReturnType;
import com.illuzionzstudios.mist.compatibility.util.VersionUtil;
import com.illuzionzstudios.mist.config.locale.PluginLocale;
import com.illuzionzstudios.mist.plugin.SpigotPlugin;
import com.illuzionzstudios.mist.util.PlayerUtil;
import com.illuzionzstudios.mist.util.TextUtil;
import com.illuzionzstudios.mist.util.Valid;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * This is an instance of a custom command for a {@link com.illuzionzstudios.mist.plugin.SpigotPlugin}
 * Here we can create our own custom commands for functionality. These commands
 * can have child commands which are executed by specifying certain arguments,
 * eg, "/main arg", where "arg" will execute it's own functionality
 */
public abstract class SpigotCommand extends Command {

    /**
     * A unique immutable list of all registered commands in the {@link com.illuzionzstudios.mist.plugin.SpigotPlugin}
     */
    @Getter
    private static final HashSet<SpigotCommand> registeredCommands = new HashSet<>();

    //  -------------------------------------------------------------------------
    //  Properties of the command
    //  -------------------------------------------------------------------------

    /**
     * This is the default permission syntax for a {@link SpigotCommand}
     * {plugin.name} The plugin's name
     * {label} The main command label
     */
    protected static final String DEFAULT_PERMISSION_SYNTAX = "{plugin.name}.command.{label}";

    /**
     * Flag indicating if this {@link SpigotCommand} has been registered
     */
    @Getter
    private boolean registered = false;

    /**
     * These are the minimum amount of arguments to be passed to the command
     * for it to actually execute
     */
    @Getter
    @Setter
    private int minArguments = 0;

    /**
     * Should we automatically send usage message when the first argument
     * equals to "help" or "?" ?
     */
    @Setter
    @Getter
    private boolean autoHandleHelp = true;

    //  -------------------------------------------------------------------------
    //  Values that get set upon execution
    //  -------------------------------------------------------------------------

    /**
     * The main label for the command. This means when this label is
     * passed as a command, use this {@link SpigotCommand} functionality
     * This is updated based on what is sent, which must be on of our aliases
     */
    @Getter
    protected String label;

    /**
     * This is the instance of {@link CommandSender} who executed this command.
     * Updated dynamically when executing the command
     */
    @Getter
    protected CommandSender sender;

    /**
     * These are the last parsed arguments to the command. Updated
     * dynamically every time we execute the command
     */
    protected String[] args;

    /**
     * Create a new {@link SpigotCommand} with certain labels
     *
     * @param label The main label for this command
     * @param aliases Additional labels that correspond to this {@link SpigotCommand}
     */
    protected SpigotCommand(@NotNull String label, String... aliases) {
        super(label, "", "", Arrays.asList(aliases));
        this.label = label;

        // Set our permission formatting
        setPermission(DEFAULT_PERMISSION_SYNTAX);

        // When creating this command instance, register it
        // Not actually registered for execution
        registeredCommands.add(this);
    }

    /**
     * See {@link #register(boolean)}
     */
    public final void register() {
        register(true);
    }
    
    /**
     * Register this command into Bukkit to be used.
     * Can throw {@link com.illuzionzstudios.mist.exception.PluginException} if {@link #isRegistered()}
     * 
     * @param unregisterOldAliases If to unregister old aliases
     */
    public final void register(final boolean unregisterOldAliases) {
        Valid.checkBoolean(!registered, "The command /" + getLabel() + " has already been registered!");

        final PluginCommand oldCommand = Bukkit.getPluginCommand(getLabel());

        if (oldCommand != null) {
            final String owningPlugin = oldCommand.getPlugin().getName();

            if (!owningPlugin.equals(SpigotPlugin.getPluginName()))
                Logger.info("&eCommand &f/" + getLabel() + " &ealready used by " + owningPlugin + ", we take it over...");

            VersionUtil.unregisterCommand(oldCommand.getLabel(), unregisterOldAliases);
        }

        registered = true;
        VersionUtil.registerCommand(this);
    }

    /**
     * Removes the command from Bukkit.
     *
     * Throws an error if the command is not {@link #isRegistered()}.
     */
    public final void unregister() {
        Valid.checkBoolean(registered, "The command /" + getLabel() + " is not registered!");

        VersionUtil.unregisterCommand(getLabel());
        registered = false;
    }

    // ----------------------------------------------------------------------
    // Execution
    // ----------------------------------------------------------------------w

    /**
     * Execute this command, updates the {@link #sender}, {@link #label} and {@link #args} variables,
     * checks permission and returns if the sender lacks it,
     * checks minimum arguments and finally passes the command to the child class.
     *
     * Also contains various error handling scenarios
     */
    @Override
    public final boolean execute(@NotNull final CommandSender sender, @NotNull final String label, @NotNull final String[] args) {

        // Update variables
        this.sender = sender;
        this.label = label;
        this.args = args;

        // Attempt to execute commands and catch errors
        try {

            // Check permissions
            if (getPermission() != null) {
                if (!hasPerm(getPermission())) {
                    // Inform
                    tell(Objects.requireNonNull(getPermissionMessage()).replace("permission", getPermission()));
                    return true;
                }
            }

            // Too little arguments and inform help
            if (args.length < getMinArguments() || autoHandleHelp && args.length == 1 && ("help".equals(args[0]) || "?".equals(args[0]))) {
                if (!getUsage().trim().equalsIgnoreCase(""))
                    // Inform usage message
                    tell(PluginLocale.Command.INVALID_USAGE.replace("{label}", label).replace("{args}", String.join(" ", args)));
                return true;
            }

            // Finally execute command
            ReturnType response = onCommand();

            // TODO: Implement properly once new lang system is written
            if (response == ReturnType.NO_PERMISSION) {
                tell("&cYou don't have enough permissions to do this...");
            } else if (response == ReturnType.PLAYER_ONLY) {
                tell(PluginLocale.Command.PLAYER_ONLY);
            } else if (response == ReturnType.UNKNOWN_ERROR) {
                tell("&cThis was not supposed to happen...");
            }
        } catch (final Throwable ex) {
            tell("&cFatal error occurred trying to execute command /" + getLabel(), "&cPlease contact an administrator to resolve the issue");
            Logger.displayError(ex, "Failed to execute command /" + getLabel() + " " + String.join(" ", args));
        }

        return true;
    }

    /**
     * This is invoked when the command is run. All dynamic information about the command
     * can be accessed via the class and doesn't need to be passed to here
     */
    protected abstract ReturnType onCommand();

    /**
     * @param message Tell the command sender a single message
     */
    protected void tell(String message) {
        if (getSender() != null)
            getSender().sendMessage(TextUtil.formatText(message));
    }

    /**
     * @param messages Tell the command sender a set of messages
     */
    protected void tell(final String... messages) {
        if (getSender() != null) {
            for (final String message : messages) {
                getSender().sendMessage(TextUtil.formatText(message));
            }
        }
    }

    // ----------------------------------------------------------------------
    // Parsers
    // ----------------------------------------------------------------------

    /**
     * Replaces placeholders in the message with arguments. For instance if the message is
     * "set user {0} to {1} rank" it will take the first args and become maybe
     * "set user IlluzionzDev to Owner rank".
     *
     * @param message Message to replace
     * @return Message with placeholders handled
     */
    protected String replacePlaceholders(String message) {
        // Replace basic labels
        message = replaceBasicPlaceholders(message);

        // Replace {X} with arguments
        for (int i = 0; i < args.length; i++)
            message = message.replace("{" + i + "}", args[i] != null ? args[i] : "");

        return message;
    }

    /**
     * Internal method for replacing {label} {sublabel} and {plugin.name} placeholders
     *
     * @param message The message to replace
     * @return Replaced message
     */
    private String replaceBasicPlaceholders(final String message) {
        return message
                .replace("{label}", getLabel())
                .replace("{sublabel}", this instanceof SpigotSubCommand ? ((SpigotSubCommand) this).getSubLabels()[0] : super.getLabel())
                .replace("{plugin.name}", SpigotPlugin.getPluginName().toLowerCase());
    }

    // ----------------------------------------------------------------------
    // Temporary variables and safety
    // ----------------------------------------------------------------------

    /**
     * Checks if the sender is a console
     */
    protected final boolean checkConsole() {
        return !isPlayer();
    }

    /**
     * A convenience check for quickly determining if the sender has a given
     * permission.
     *
     * @param permission Permission to check
     * @return If player does have permission
     */
    protected final boolean hasPerm(final String permission) {
        return PlayerUtil.hasPerm(sender, permission);
    }

    /**
     * This is the instance of {@link Player} who executed this command.
     * This only applies if the {@link #sender} is an instance of {@link Player}
     * If is not an instance of {@link CommandSender}, returns null
     */
    protected final Player getPlayer() {
        return isPlayer() ? (Player) getSender() : null;
    }

    /**
     * See {@link #getPlayer()}
     *
     * @return Isn't null
     */
    protected final boolean isPlayer() {
        return sender instanceof Player;
    }

    /**
     * Get the permission for this command, either the one you set or our from Localization
     */
    @Override
    public final String getPermissionMessage() {
        return super.getPermissionMessage() != null && !super.getPermissionMessage().trim().equals("") ? super.getPermissionMessage() : PluginLocale.Command.NO_PERMISSION;
    }

    /**
     * By default we check if the player has the permission you set in setPermission.
     *
     * If that is null, we check for the following:
     * {plugin.name}.command.{label} for {@link SpigotCommand}
     *
     * We handle lacking permissions automatically and return with an no-permission message
     * when the player lacks it.
     *
     * @return The formatted permission
     */
    @Override
    public final String getPermission() {
        return super.getPermission() == null ? null : replaceBasicPlaceholders(super.getPermission());
    }

    /**
     * Get the permission without replacing {plugin.name}, {label} or {sublabel}
     *
     * @deprecated internal use only
     */
    @Deprecated
    public final String getRawPermission() {
        return super.getPermission();
    }

    /**
     * Sets the permission required for this command to run. If you set the
     * permission to null we will not require any permission (unsafe).
     *
     * @param permission The permission required for the command
     */
    @Override
    public final void setPermission(final String permission) {
        super.setPermission(permission);
    }

    /**
     * Get aliases for this command
     */
    @Override
    public final List<String> getAliases() {
        return super.getAliases();
    }

    /**
     * Get description for this command
     */
    @Override
    public final String getDescription() {
        return super.getDescription();
    }

    /**
     * Get the name of this command
     */
    @Override
    public final String getName() {
        return super.getName();
    }

    /**
     * Get the usage message of this command
     */
    @Override
    public final String getUsage() {
        return super.getUsage();
    }

    /**
     * Get the label given when the command was created or last updated with {@link #setLabel(String)}
     */
    public final String getMainLabel() {
        return super.getLabel();
    }

    /**
     * Updates the label of this command
     */
    @Override
    public final boolean setLabel(@NotNull final String name) {
        label = name;
        return super.setLabel(name);
    }

    /**
     * Show tab completion suggestions when the given sender
     * writes the command with the given arguments
     *
     * Tab completion is only shown if the sender has {@link #getPermission()}
     */
    @NotNull
    @Override
    public final List<String> tabComplete(@NotNull final CommandSender sender, @NotNull final String alias, @NotNull final String[] args) throws IllegalArgumentException {
        this.sender = sender;
        this.label = alias;
        this.args = args;

        if (hasPerm(getPermission())) {
            return tabComplete();
        }

        return new ArrayList<>();
    }

    /**
     * Override this method to support tab completing in your command.
     *
     * You can then use "sender", "label" or "args" fields from {@link SpigotCommand}
     * class normally and return a list of tab completion suggestions.
     *
     * We already check for {@link #getPermission()} and only call this method if the
     * sender has it.
     *
     * @return the list of suggestions to complete, or null to complete player names automatically
     */
    protected List<String> tabComplete() {
        return null;
    }
}
