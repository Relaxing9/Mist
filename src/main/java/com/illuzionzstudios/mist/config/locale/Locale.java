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
        super(plugin, "/locales", PluginSettings.Settings.LOCALE.getString() + ".lang");
    }

    /**
     * The current loaded {@link Locale} instance
     */
    public static YamlConfig LOCALE_FILE;

    /**
     * This loads our lang file if the "locales/LOCALE.lang" exists
     * in internal resources.
     *
     * Prefix comes from {@link PluginSettings}
     *
     * @return If the locale was loaded/exists
     */
    @Override
    public final boolean load() {
        return loadLocale(PluginSettings.Settings.LOCALE.getString());
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
     * General messages
     */
    public static class General {

        /**
         * The prefix to use before certain messages
         */
        public static String PLUGIN_PREFIX = "&d&lMist Plugin &8»&7";

        /**
         * Message sent when reloading the plugin. Used in {@link com.illuzionzstudios.mist.command.type.ReloadCommand}
         */
        public static String PLUGIN_RELOAD = "&7Reloaded the plugin (Configuration files & controllers)";

        public static void init() {
            PLUGIN_PREFIX = loadMessage("general.prefix", PLUGIN_PREFIX);
            PLUGIN_RELOAD = loadMessage("general.reload", PLUGIN_RELOAD);
        }
    }

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

        /**
         * Sent when the executor provides too little arguments
         */
        public static String INVALID_USAGE = "&cInvalid usage. Try /{label} {args}";

        /**
         * If they try use a sub command that doesn't exist
         */
        public static String INVALID_SUB = "&cThat command doesn't exist. Try /{label} help";

        /**
         * The optional arguments label
         */
        public static String LABEL_OPTIONAL_ARGS = "optional arguments";

        /**
         * The required arguments label
         */
        public static String LABEL_REQUIRED_ARGS = "required arguments";

        public static void init() {
            PLAYER_ONLY = loadMessage("command.player-only", PLAYER_ONLY);
            NO_PERMISSION = loadMessage("command.no-permission", NO_PERMISSION);
            INVALID_USAGE = loadMessage("command.invalid-usage", INVALID_USAGE);
            INVALID_SUB = loadMessage("command.invalid-sub", INVALID_SUB);
            LABEL_OPTIONAL_ARGS = loadMessage("command.label-optional-args", LABEL_OPTIONAL_ARGS);
            LABEL_REQUIRED_ARGS = loadMessage("command.label-required-args", LABEL_REQUIRED_ARGS);
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
        General.init();
        Command.init();

        // Load our other custom settings
        settings.loadLocale();
        settings.saveChanges();
    }

    /**
     * Invoked to load all other custom settings that we implement
     * in our own {@link Locale}
     */
    public abstract void loadLocale();

    /**
     * Shorthand to update key
     */
    private static String loadMessage(String key, String value) {
        value = LOCALE_FILE.getString(key, value);
        LOCALE_FILE.set(key, value);
        return value;
    }

}
