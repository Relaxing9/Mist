/**
 * Copyright © 2020 Property of Illuzionz Studios, LLC
 * All rights reserved. No part of this publication may be reproduced, distributed, or
 * transmitted in any form or by any means, including photocopying, recording, or other
 * electronic or mechanical methods, without the prior written permission of the publisher,
 * except in the case of brief quotations embodied in critical reviews and certain other
 * noncommercial uses permitted by copyright law. Any licensing of this software overrides
 * this statement.
 */
package com.illuzionzstudios.mist.config;

import com.illuzionzstudios.mist.Mist;
import com.illuzionzstudios.mist.plugin.SpigotPlugin;

/**
 * An implementation of basic plugin settings. This handles settings that
 * all plugins with this library will have. For instance locale, main command etc.
 * Typically used as the "config.yml"
 *
 * This should be implemented by our {@link com.illuzionzstudios.mist.plugin.SpigotPlugin} and
 * define our own {@link ConfigSetting} specific to the plugin
 */
public abstract class PluginSettings extends YamlConfig {

    /**
     * @return Get the file name for these settings, by default config.yml
     */
    protected String getSettingsFileName() {
        return Mist.File.SETTINGS_NAME;
    }

    //  -------------------------------------------------------------------------
    //  Versioning
    //  -------------------------------------------------------------------------

    /**
     * This is the loaded config version, found under the "Version" key
     * We can use this for compatibility between versions
     */
    protected static int VERSION;

    @Override
    protected void preLoad() {
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
    //  Main config settings provided by default
    //  -------------------------------------------------------------------------

    /**
     * These are the main command aliases for the plugin.
     * For instance "customfishing, customf, cf". This way the user
     * can change what main commands they want, but keep functionality
     */
    public static ConfigSetting MAIN_COMMAND_ALIASES;

    /**
     * The locale type to use, for instance
     * "en_US"
     */
    public static ConfigSetting LOCALE;

    /**
     * Load these {@link PluginSettings} into the server, setting values
     * if not there, or loading the values into memory
     *
     * @param plugin The {@link SpigotPlugin} to load settings for
     * @param settings The instance of {@link PluginSettings} to load
     */
    public static void loadSettings(SpigotPlugin plugin, PluginSettings settings) {
        MAIN_COMMAND_ALIASES = new ConfigSetting(settings, "Main Command Aliases", plugin.getCommandAliases());
        LOCALE = new ConfigSetting(settings, "Locale", "en_US");
    }

}
