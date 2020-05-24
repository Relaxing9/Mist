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
import com.illuzionzstudios.mist.compatibility.util.VersionUtil;
import com.illuzionzstudios.mist.config.locale.Locale;
import com.illuzionzstudios.mist.exception.CommandException;
import com.illuzionzstudios.mist.plugin.SpigotPlugin;
import com.illuzionzstudios.mist.util.PlayerUtil;
import com.illuzionzstudios.mist.util.TextUtils;
import com.illuzionzstudios.mist.util.Valid;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

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
    public final boolean execute(final CommandSender sender, final String label, final String[] args) {

        // Update variables
        this.sender = sender;
        this.label = label;
        this.args = args;

        // Attempt to execute commands and catch errors
        try {

            // Check permissions
            if (getPermission() != null)
                checkPerm(getPermission());

            // Too little arguments
            if (args.length < getMinArguments()) {
                if (!getUsage().trim().equalsIgnoreCase(""))
                    // Inform usage message
                    informError(Locale.Command.INVALID_USAGE.replace("{label}", label)
                        .replace("{args}", String.join(" ", args)));
                return true;
            }

            // Finally execute command
            onCommand();

        } catch (final CommandException ex) {
            if (ex.getMessages() != null)
                informError(ex.getMessages());
        } catch (final Throwable ex) {
            informError("&cFatal error occurred trying to execute command /" + getLabel(),
                    "&cPlease contact an administrator to resolve the issue");
            Logger.displayError(ex, "Failed to execute command /" + getLabel() + " " + String.join(" ", args));
        }

        return true;
    }

    /**
     * This is invoked when the command is run. All dynamic information about the command
     * can be accessed via the class and doesn't need to be passed to here
     */
    protected abstract void onCommand();

    /**
     * A simple util method to log error messages to the {@link CommandSender}
     *
     * @param messages The messages to send
     */
    private void informError(final String... messages) {
        for (final String message : messages) {
            getSender().sendMessage(TextUtils.formatText(message));
        }
    }

    // ----------------------------------------------------------------------
    // Convenience checks
    //
    // Here is how they work: When you command is executed, simply call any
    // of these checks. If they fail, an error will be thrown inside of
    // which will be a message for the player.
    //
    // We catch that error and send the message to the player without any
    // harm or console errors to your plugin. That is intended and saves time.
    // ----------------------------------------------------------------------

    /**
     * Checks if the sender is a console
     *
     * @throws CommandException If is console
     */
    protected final void checkConsole() throws CommandException {
        if (!isPlayer())
            throw new CommandException("&c" + Locale.Command.PLAYER_ONLY);
    }

    /**
     * Checks if the player has the given permission
     *
     * @param perm Permission to check
     * @throws CommandException If player didn't have said permission
     */
    public final void checkPerm(@NonNull final String perm) throws CommandException {
        if (isPlayer() && !PlayerUtil.hasPerm(sender, perm))
            throw new CommandException(getPermissionMessage().replace("{permission}", perm));
    }

    // ----------------------------------------------------------------------
    // Parsers
    // ----------------------------------------------------------------------

    /**
     * Internal method for replacing {label} {sublabel} and {plugin.name} placeholders
     *
     * @param message The message to replace
     * @return Replaced message
     */
    private String replaceBasicPlaceholders(final String message) {
        return message
                .replace("{label}", getLabel())
                .replace("{plugin.name}", SpigotPlugin.getPluginName().toLowerCase());
    }

    // ----------------------------------------------------------------------
    // Temporary variables and safety
    // ----------------------------------------------------------------------

    /**
     * A convenience check for quickly determining if the sender has a given
     * permission.
     *
     * TIP: For a more complete check use {@link #checkPerm(String)} that
     * will automatically return your command if they lack the permission.
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
        return super.getPermissionMessage() != null && !super.getPermissionMessage().trim().equals("") ? super.getPermissionMessage() : Locale.Command.NO_PERMISSION;
    }

    /**
     * By default we check if the player has the permission you set in setPermission.
     *
     * If that is null, we check for the following:
     * {yourpluginname}.command.{label} for {@link SpigotCommand}
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
     * @return
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
}
