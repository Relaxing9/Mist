/**
 * Copyright © 2020 Property of Illuzionz Studios, LLC
 * All rights reserved. No part of this publication may be reproduced, distributed, or
 * transmitted in any form or by any means, including photocopying, recording, or other
 * electronic or mechanical methods, without the prior written permission of the publisher,
 * except in the case of brief quotations embodied in critical reviews and certain other
 * noncommercial uses permitted by copyright law. Any licensing of this software overrides
 * this statement.
 */
package com.illuzionzstudios.mist.config.locale;

import com.illuzionzstudios.mist.config.ConfigSetting;
import com.illuzionzstudios.mist.config.PluginSettings;
import com.illuzionzstudios.mist.config.YamlConfig;
import com.illuzionzstudios.mist.plugin.SpigotPlugin;

/**
 * Loaded file that handles all game language
 * Also supports different languages apart from English
 * Implement our own instance in our plugin with the different values
 *
 * We provide some common messages but can implement your own
 */
public abstract class Locale extends YamlConfig {

    /**
     * @param plugin Make sure we pass owning plugin
     */
    public Locale(SpigotPlugin plugin) {
        super(plugin);
    }

    /**
     * The current loaded {@link Locale} instance
     */
    public static YamlConfig LOCALE_FILE;

    /**
     * This loads our lang file if the "locales/LOCALE.lang" exists
     * in internal resources.
     *
     * Prefix comes from {@link PluginSettings#LOCALE}
     *
     * @return If the locale was loaded/exists
     */
    @Override
    public final boolean load() {
        return loadLocale(PluginSettings.LOCALE.getString());
    }

    //  -------------------------------------------------------------------------
    //  Versioning
    //  -------------------------------------------------------------------------

    /**
     * This is the loaded config version, found under the "Version" key
     * We can use this for compatibility between versions
     */
    protected static int VERSION;

    /**
     * Here we want to update the current configuration version set
     * in the config to the static version in the code.
     *
     * Note: Call {@code super#preLoad} when overriding
     */
    @Override
    protected void postLoad() {
        // Load version first so we can use it later
        if ((VERSION = getInt("Version")) != getConfigVersion())
            set("Version", getConfigVersion());
    }

    /**
     * Return the very latest config version
     * Any changes here must also be made to the "Version" key in your settings file.
     *
     * @return Current latest config version
     */
    protected abstract int getConfigVersion();

    //  -------------------------------------------------------------------------
    //  Main messages provided by default
    //  If these are found in the locale, we use those, otherwise use these
    //  defaults
    //
    //  Init methods are called to set variables
    //
    //  When creating your own messages that have the same name, make sure to
    //  extend the super class and call the super init method
    //  -------------------------------------------------------------------------

    /**
     * Messages relating to commands
     */
    public static class Command {

        /**
         * If a command sender that isn't a player tries to execute a command
         */
        public static String PLAYER_ONLY = "&cYou must be a player to execute this command.";

        /**
         * If the player doesn't have a required permission
         */
        public static String NO_PERMISSION = "&cYou must have the permission {permission} to do this.";

        public static void init() {
            PLAYER_ONLY = LOCALE_FILE.getString("Command.Player Only", PLAYER_ONLY);
            NO_PERMISSION = LOCALE_FILE.getString("Command.No Permission", NO_PERMISSION);
        }
    }

    /**
     * Load these {@link PluginSettings} into the server, setting values
     * if not there, or loading the values into memory
     *
     * Call in the {@link SpigotPlugin#onPluginEnable()} to load plugin settings
     *
     * @param plugin The {@link SpigotPlugin} to load settings for
     * @param settings The instance of {@link Locale} to load
     */
    public static void loadLocale(SpigotPlugin plugin, Locale settings) {
        // Load settings loadLocale
        settings.load();
        // Set instance
        LOCALE_FILE = settings;

        // Load common messages
        Command.init();

        // Load our other custom settings
        settings.loadLocale();
    }

    /**
     * Invoked to load all other custom settings that we implement
     * in our own {@link Locale}
     */
    public abstract void loadLocale();

}
