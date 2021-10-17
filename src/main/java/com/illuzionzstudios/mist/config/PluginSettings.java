package com.illuzionzstudios.mist.config;

import com.illuzionzstudios.mist.Mist;
import com.illuzionzstudios.mist.plugin.SpigotPlugin;

/**
 * An implementation of basic plugin settings. This handles settings that
 * all plugins with this library will have. For instance locale, main command etc.
 * Typically, used as the "config.yml"
 *
 * This should be implemented by our {@link SpigotPlugin} and
 * define our own {@link ConfigSetting} specific to the plugin
 */
public abstract class PluginSettings extends YamlConfig {

    /**
     * The current loaded {@link PluginSettings} instance
     */
    public static YamlConfig SETTINGS_FILE;

    /**
     * @param plugin Make sure we pass owning plugin
     */
    public PluginSettings(SpigotPlugin plugin) {
        super(plugin, Mist.SETTINGS_NAME);
    }

    /**
     * Add any defaults from internal resources
     */
    @Override
    public final boolean load() {
        return loadResourceToServer("", Mist.SETTINGS_NAME);
    }

    //  -------------------------------------------------------------------------
    //  Main config settings provided by default
    //  -------------------------------------------------------------------------

    public static final ConfigSettings GENERAL_GROUP = new ConfigSettings("settings");

    /**
     * The locale type to use, for instance
     * "en_US"
     */
    public static ConfigSetting LOCALE = GENERAL_GROUP.create("settings.locale", "en_US",
            "The language file to use for the plugin",
            "More language files (if available) can be found in the plugins locale folder.");

    /**
     * Load these {@link PluginSettings} into the server, setting values
     * if not there, or loading the values into memory
     *
     * Call in the {@link SpigotPlugin#onPluginEnable()} to load plugin settings
     *
     * @param settings The instance of {@link PluginSettings} to load
     */
    public static void loadSettings(PluginSettings settings) {
        SETTINGS_FILE = settings;
        // Load settings file
        SETTINGS_FILE.load();

        GENERAL_GROUP.load();

        // Load our other custom settings
        settings.loadSettings();
        SETTINGS_FILE.saveChanges();
    }

    /**
     * Invoked to load all other custom settings that we implement
     * in our own {@link PluginSettings}
     */
    public abstract void loadSettings();

}
