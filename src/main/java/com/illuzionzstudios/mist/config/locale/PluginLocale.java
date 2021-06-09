package com.illuzionzstudios.mist.config.locale;

import com.illuzionzstudios.mist.config.PluginSettings;
import com.illuzionzstudios.mist.config.YamlConfig;
import com.illuzionzstudios.mist.plugin.SpigotPlugin;

import java.util.HashMap;

/**
 * Loaded file that handles all game language
 * Also supports different languages apart from English
 * Implement our own instance in our plugin with the different values
 *
 * We provide some common messages but can implement your own
 */
public abstract class PluginLocale extends YamlConfig {

    /**
     * @param plugin Make sure we pass owning plugin
     */
    public PluginLocale(SpigotPlugin plugin) {
        super(plugin, "/locales", PluginSettings.LOCALE.getString() + ".lang");
    }

    /**
     * The current loaded {@link PluginLocale} instance
     */
    public static YamlConfig LOCALE_FILE;

    /**
     * This is a cache of all loaded translations for a key. If we go to get a value by
     * a key it will first check here. If not found it will look through the file and if found
     * update it here. If not found anywhere it will simply return the default.
     */
    protected final static HashMap<String, String> localeCache = new HashMap<>();

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
        return loadLocale(PluginSettings.LOCALE.getString());
    }

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
            if (LOCALE_FILE.isSet("general.prefix"))
                PLUGIN_PREFIX = LOCALE_FILE.getString("general.prefix");

            if (LOCALE_FILE.isSet("general.reload"))
                PLUGIN_RELOAD = LOCALE_FILE.getString("general.reload");
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
            if (LOCALE_FILE.isSet("command.player-only"))
                PLAYER_ONLY = LOCALE_FILE.getString("command.player-only");

            if (LOCALE_FILE.isSet("command.no-permission"))
                NO_PERMISSION = LOCALE_FILE.getString("command.no-permission");

            if (LOCALE_FILE.isSet("command.invalid-usage"))
                INVALID_USAGE = LOCALE_FILE.getString("command.invalid-usage");

            if (LOCALE_FILE.isSet("command.invalid-sub"))
                INVALID_SUB = LOCALE_FILE.getString("command.invalid-sub");

            if (LOCALE_FILE.isSet("command.label-optional-args"))
                LABEL_OPTIONAL_ARGS = LOCALE_FILE.getString("command.label-optional-args");

            if (LOCALE_FILE.isSet("command.label-required-args"))
                LABEL_REQUIRED_ARGS = LOCALE_FILE.getString("command.label-required-args");
        }
    }

    /**
     * Language dealing with configuration
     */
    public static class Config {

        /**
         * Message sent prompting to enter a value
         */
        public static String ENTER_VALUE = "&7Enter a new value to set (Type 'cancel' to cancel)";

        public static void init() {
            if (LOCALE_FILE.isSet("config.enter-value"))
                ENTER_VALUE = LOCALE_FILE.getString("config.enter-value");
        }
    }

    /**
     * Language dealing with interfaces
     */
    public static class Interface {

        /**
         * Name of the confirm icon in the confirm inventory
         */
        public static String CONFIRM_CONFIRM_NAME = "&a&lConfirm";

        /**
         * Lore of the confirm icon in the confirm inventory
         */
        public static String CONFIRM_CONFIRM_LORE = "&7&o(Click to confirm)";

        /**
         * Name of the deny icon in the confirm inventory
         */
        public static String CONFIRM_DENY_NAME = "&c&lDeny";

        /**
         * Lore of the deny icon in the confirm inventory
         */
        public static String CONFIRM_DENY_LORE = "&7&o(Click to deny)";

        /**
         * Name for an option in the options inventory
         */
        public static String OPTIONS_OPTION_NAME = "&d&l{valueName}";

        /**
         * Lore for an option in the options inventory
         */
        public static String OPTIONS_OPTION_LORE = "&7Value: {value}\n&r\n&7{description}\n&r\n&7&o(Left click to edit value)";

        /**
         * Name for displaying values in the edit list inventory
         */
        public static String LIST_VALUES_NAME = "&d&lCurrent Values";

        /**
         * Name for clearing values in the edit list inventory
         */
        public static String LIST_CLEAR_NAME = "&c&lClear Values";

        /**
         * Lore for clearing values in the edit list inventory
         */
        public static String LIST_CLEAR_LORE = "&7&o(Click to clear values)";

        /**
         * Name for adding values in the edit list inventory
         */
        public static String LIST_ADD_NAME = "&a&lAdd value";

        /**
         * Lore for adding values in the edit list inventory
         */
        public static String LIST_ADD_LORE = "&7&o(Click to add a value)";

        public static void init() {
            if (LOCALE_FILE.isSet("interface.confirm.confirm.name"))
                CONFIRM_CONFIRM_NAME = LOCALE_FILE.getString("interface.confirm.confirm.name");

            if (LOCALE_FILE.isSet("interface.confirm.confirm.lore"))
                CONFIRM_CONFIRM_LORE = LOCALE_FILE.getString("interface.confirm.confirm.lore");

            if (LOCALE_FILE.isSet("interface.confirm.deny.name"))
                CONFIRM_DENY_NAME = LOCALE_FILE.getString("interface.confirm.deny.name");

            if (LOCALE_FILE.isSet("interface.confirm.deny.lore"))
                CONFIRM_DENY_LORE = LOCALE_FILE.getString("interface.confirm.deny.lore");

            if (LOCALE_FILE.isSet("interface.options.option.name"))
                OPTIONS_OPTION_NAME = LOCALE_FILE.getString("interface.options.option.name");

            if (LOCALE_FILE.isSet("interface.options.option.lore"))
                OPTIONS_OPTION_LORE = LOCALE_FILE.getString("interface.options.option.lore");

            if (LOCALE_FILE.isSet("interface.list.values.name"))
                LIST_VALUES_NAME = LOCALE_FILE.getString("interface.list.values.name");

            if (LOCALE_FILE.isSet("interface.list.clear.name"))
                LIST_CLEAR_NAME = LOCALE_FILE.getString("interface.list.clear.name");

            if (LOCALE_FILE.isSet("interface.list.clear.lore"))
                LIST_CLEAR_LORE = LOCALE_FILE.getString("interface.list.clear.lore");

            if (LOCALE_FILE.isSet("interface.list.add.name"))
                LIST_ADD_NAME = LOCALE_FILE.getString("interface.list.add.name");

            if (LOCALE_FILE.isSet("interface.list.add.lore"))
                LIST_ADD_LORE = LOCALE_FILE.getString("interface.list.add.lore");
        }
    }

    /**
     * Language dealing with updates
     */
    public static class Update {

        /**
         * The message if a new version is found
         */
        public static String AVAILABLE = "&2You're on an &a{status}&2 version of &a{plugin_name}&2.\n"
                + "&2Current version: &a{current}&2; New version: &a{new}\n"
                + "&2URL: &ahttps://spigotmc.org/resources/{resource_id}/.";

        public static void init() {
            if (LOCALE_FILE.isSet("update.available"))
                AVAILABLE = LOCALE_FILE.getString("update.available");
        }
    }

    /**
     * Load the {@link PluginLocale} into the server, setting values
     * if not there, or loading the values into memory
     *
     * Call in the {@link SpigotPlugin#onPluginEnable()} to load plugin locale
     *
     * @param settings The instance of {@link PluginLocale} to load
     */
    public static void loadLocale(PluginLocale settings) {
        // Set instance
        LOCALE_FILE = settings;
        // Load settings loadLocale
        settings.load();

        // Load common messages
        General.init();
        Command.init();
        Config.init();
        Interface.init();
        Update.init();

        // Load our other custom settings
        settings.loadLocale();
        settings.saveChanges();
    }

    /**
     * Invoked to load all other custom settings that we implement
     * in our own {@link PluginLocale}
     */
    public abstract void loadLocale();

    /**
     * Retrieve a message from the locale
     *
     * @param key Node key to search for
     * @return The found message
     */
    public static String getMessage(String key) {
        return getMessage(key, key);
    }

    /**
     * Retrieve a message from the locale. Will first look in the cache to see if
     * they key is there. If not found it will look for it in the file and then update
     * the cache. If couldn't be found anywhere will return {@param def}. Should
     * be defined in lang file manually anyway.
     *
     * @param key Node key to search for
     * @param def The default message to use if not found
     * @return The found message or default
     */
    public static String getMessage(String key, String def) {
        // Try find in cache
        if (localeCache.containsKey(key))
            return localeCache.get(key);

        // Try find in locale file
        if (LOCALE_FILE.isSet(key)) {
            String foundValue = LOCALE_FILE.getString(key);
            // Update cache
            localeCache.put(key, foundValue);
            return foundValue;
        }

        // Return default
        return def;
    }

    /**
     * Clear the locale cache. Usually good for a plugin reload
     */
    public static void invalidateCache() {
        localeCache.clear();
    }

}
