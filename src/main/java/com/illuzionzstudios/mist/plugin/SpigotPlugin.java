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

import com.illuzionzstudios.mist.Logger;
import lombok.Getter;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

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

    //  -------------------------------------------------------------------------
    //  Variables specific to the plugin instance
    //  -------------------------------------------------------------------------

    /**
     * If this plugin is currently enabled and running.
     * Checked at different loading stages
     */
    protected boolean isEnabled = true;

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

    }

    @Override
    public final void onDisable() {

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
            // TODO: Reload other stuff
            onPluginPreReload();
            onPluginReload();
        } catch (final Throwable ex) {
            Logger.displayError(ex, "Error reloading plugin");
        } finally {
            Logger.info(" ");
            reloading = false;
        }
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

}
