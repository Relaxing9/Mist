/**
 * Copyright © 2020 Property of Illuzionz Studios, LLC
 * All rights reserved. No part of this publication may be reproduced, distributed, or
 * transmitted in any form or by any means, including photocopying, recording, or other
 * electronic or mechanical methods, without the prior written permission of the publisher,
 * except in the case of brief quotations embodied in critical reviews and certain other
 * noncommercial uses permitted by copyright law. Any licensing of this software overrides
 * this statement.
 */
package com.illuzionzstudios.mist.plugin;

import com.illuzionzstudios.mist.Listeners;
import com.illuzionzstudios.mist.Logger;
import com.illuzionzstudios.mist.command.SpigotCommand;
import com.illuzionzstudios.mist.command.SpigotCommandGroup;
import com.illuzionzstudios.mist.config.PluginSettings;
import com.illuzionzstudios.mist.config.locale.Locale;
import com.illuzionzstudios.mist.scheduler.MinecraftScheduler;
import com.illuzionzstudios.mist.scheduler.bukkit.BukkitScheduler;
import com.illuzionzstudios.mist.ui.InterfaceController;
import com.illuzionzstudios.mist.ui.UserInterface;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.awt.*;
import java.io.File;
import java.util.List;
import java.util.Objects;

/**
 * Represents an instance of a custom spigot plugin with our
 * "Mist" functionality
 *
 * The plugin is only designed to work on versions
 * {@version 1.8.8} to {@version 1.15.2}
 */
public abstract class SpigotPlugin extends JavaPlugin implements Listener {

    //  -------------------------------------------------------------------------
    //  Static instances that stay final
    //  -------------------------------------------------------------------------

    /**
     * If our {@link SpigotPlugin} is currently reloading
     */
    @Getter
    private static volatile boolean reloading = false;

    /**
     * Singleton instance of our {@link SpigotPlugin}
     */
    private static volatile SpigotPlugin INSTANCE;

    /**
     * Return our instance of the {@link SpigotPlugin}
     *
     * Should be overridden in your own {@link SpigotPlugin} class
     * as a way to implement your own methods per plugin
     *
     * @return This instance of the plugin
     */
    public static SpigotPlugin getInstance() {
        // Assign if null
        if (INSTANCE == null) {
            INSTANCE = JavaPlugin.getPlugin(SpigotPlugin.class);

            Objects.requireNonNull(INSTANCE, "Cannot create instance of plugin. Did you reload?");
        }

        return INSTANCE;
    }

    /**
     * Get if the instance that is used across the library has been set. Normally it
     * is always set, except for testing.
     *
     * @return if the instance has been set.
     */
    public static final boolean hasInstance() {
        return INSTANCE != null;
    }

    /**
     * @return The name of the {@link SpigotPlugin} from the plugin description
     */
    public static final String getPluginName() {
        return getInstance().getDescription().getName();
    }

    /**
     * @return The version of the {@link SpigotPlugin} from the plugin description
     */
    public static final String getPluginVersion() {
        return getInstance().getDescription().getVersion();
    }

    /**
     * Shortcut for getFile()
     *
     * @return plugin's jar file
     */
    public static final File getSource() {
        return getInstance().getFile();
    }

    //  -------------------------------------------------------------------------
    //  Variables specific to the plugin instance
    //  -------------------------------------------------------------------------

    /**
     * If this plugin is currently enabled and running.
     * Checked at different loading stages
     */
    protected boolean isEnabled = true;

    /**
     * An easy way to handle listeners for reloading
     */
    private final Listeners listeners = new Listeners();

    //  -------------------------------------------------------------------------
    //  Plugin loading methods
    //  -------------------------------------------------------------------------

    /**
     * Called when the plugin is loaded into the server
     */
    public abstract void onPluginLoad();

    /**
     * Called before the actual plugin is enabled
     */
    public abstract void onPluginPreEnable();

    /**
     * Called when the plugin is finally being enabled
     */
    public abstract void onPluginEnable();

    /**
     * Called when the plugin is being disabled
     */
    public abstract void onPluginDisable();

    /**
     * Called before the plugin is reloaded
     * This means config reloads, /reload, or if the plugin is
     * disabled and then enabled while the server is running
     */
    public abstract void onPluginPreReload();

    /**
     * Called after the plugin is reloaded
     * This means config reloads, /reload, or if the plugin is
     * disabled and then enabled while the server is running
     */
    public abstract void onPluginReload();

    /**
     * This method is called when registering things like listeners.
     *
     * In your plugin use it to register commands, events etc.
     */
    public abstract void onReloadablesStart();

    @Override
    public final void onLoad() {
        try {
            // Try set instance
            getInstance();
        } catch (final Throwable ex) {
            // If can't set manually
            INSTANCE = this;
            throw ex;
        }

        onPluginLoad();
    }

    @Override
    public final void onEnable() {
        if (!isEnabled) return;

        // Pre start the plugin
        onPluginPreEnable();

        // Return if plugin pre start indicated a fatal problem
        if (!isEnabled || !isEnabled()) return;

        // Main enabled
        try {
            // Load settings and locale
            PluginSettings.loadSettings(this, getPluginSettings());
            Locale.loadLocale(this, getPluginLocale());

            // Enable our scheduler
            new BukkitScheduler(this).start();

            onReloadablesStart();

            onPluginEnable();

            // Register main events
            registerListener(this);
            registerListener(InterfaceController.INSTANCE);
        } catch (final Throwable ex) {
            Logger.displayError(ex, "Error enabling plugin");
        }
    }

    @Override
    public final void onDisable() {
        // Don't shutdown if wasn't enabled
        if (!isEnabled) return;

        try {
            onPluginDisable();
        } catch (final Throwable t) {
            Logger.info("&cPlugin might not shut down property. Got " + t.getClass().getSimpleName() + ": " + t.getMessage());
        }

        unregisterReloadables();

        // Try close inventories
        try {
            for (final Player online : getServer().getOnlinePlayers()) {
                final UserInterface userInterface = UserInterface.getInterface(online);

                if (userInterface != null)
                    online.closeInventory();
            }
        } catch (final Throwable t) {
            Logger.displayError(t, "Error closing menu inventories for players..");

            t.printStackTrace();
        }

        Objects.requireNonNull(INSTANCE, "Plugin " + getName() + " has already been shutdown!");
        INSTANCE = null;
    }

    /**
     * Attempt to reload the plugin
     */
    public final void reload() {
        Logger.info(" ");
        Logger.info("Reloading plugin " + getPluginName() + " v" + getPluginVersion());
        Logger.info(" ");

        reloading = true;

        try {
            unregisterReloadables();
            onPluginPreReload();
            listeners.unregister();

            onPluginReload();

            onReloadablesStart();
        } catch (final Throwable ex) {
            Logger.displayError(ex, "Error reloading plugin");
        } finally {
            Logger.info(" ");
            reloading = false;
        }
    }

    /**
     * Un register stuff when reloading
     */
    private void unregisterReloadables() {
        // Stop ticking all tasks
        MinecraftScheduler.get().stopInvocation();
    }

    /**
     * @param listener Register a listener for this plugin
     */
    protected final void registerListener(Listener listener) {
        listeners.register(this, listener);
    }

    /**
     * @param command Register a {@link SpigotCommand} for this plugin
     */
    protected final void registerCommand(final SpigotCommand command) {
        command.register();
    }


    //  -------------------------------------------------------------------------
    //  Additional features of our main plugin
    //  -------------------------------------------------------------------------

    /**
     * The start-up fancy logo
     *
     * @return null by default
     */
    public String[] getStartupLogo() {
        return null;
    }

    //  -------------------------------------------------------------------------
    //  Stuff we need to implement
    //  -------------------------------------------------------------------------

    /**
     * @return The main labels for the main plugin command. For instance, "customfishing", "customf"
     */
    public abstract List<String> getCommandAliases();

    /**
     * @return Our custom implementation of {@link PluginSettings}
     */
    public abstract PluginSettings getPluginSettings();

    /**
     * @return Get the {@link Locale} instance being used for this plugin
     */
    public abstract Locale getPluginLocale();

    /**
     * @return Main {@link SpigotCommandGroup} for this plugin
     */
    public abstract SpigotCommandGroup getMainCommand();

    /**
     * Check if a given label is for the main plugin command
     *
     * @param label Label to check
     * @return If it's an aliases for the main command
     */
    public static boolean isMainCommand(String label) {
        return getInstance().getMainCommand() != null && getInstance().getMainCommand().getLabel().equalsIgnoreCase(label);
    }

}
