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
import com.illuzionzstudios.mist.plugin.SpigotPlugin;
import com.illuzionzstudios.mist.util.Valid;
import lombok.Getter;
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

        VersionUtil.registerCommand(this);
        registered = true;
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
