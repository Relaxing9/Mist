package com.illuzionzstudios.mist.config.locale;

import com.illuzionzstudios.mist.config.PluginSettings;
import com.illuzionzstudios.mist.config.YamlConfig;
import com.illuzionzstudios.mist.plugin.SpigotPlugin;

import java.util.HashMap;

/**
 * Loaded file that handles all game language
 * Also supports different languages apart from English
 * Implement our own instance in our plugin with the different values
 * <p>
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
     * <p>
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
    //  -------------------------------------------------------------------------

    /**
     * Messages loaded on startup
     */
    public static final MistStringGroup STARTUP_GROUP = new MistStringGroup();

    /**
     * The prefix to use before certain messages
     */
    public static MistString GENERAL_PLUGIN_PREFIX = STARTUP_GROUP.create("general.prefix", "&d&lMist Plugin &8\\u00BB&7");

    /**
     * Message sent when reloading the plugin. Used in {@link com.illuzionzstudios.mist.command.type.ReloadCommand}
     */
    public static MistString GENERAL_PLUGIN_RELOAD = STARTUP_GROUP.create("general.reload", "&7Reloaded the plugin (Configuration files & controllers)");

    /**
     * If a command sender that isn't a player tries to execute a command
     */
    public static MistString COMMAND_PLAYER_ONLY = STARTUP_GROUP.create("command.player-only", "&cYou must be a player to execute this command.");

    /**
     * If the player doesn't have a required permission
     */
    public static MistString COMMAND_NO_PERMISSION = STARTUP_GROUP.create("command.no-permission", "&cYou must have the permission {permission} to do this.");

    /**
     * Sent when the executor provides too little arguments
     */
    public static MistString COMMAND_INVALID_USAGE = STARTUP_GROUP.create("command.invalid-usage", "&cInvalid usage. Try /{label} {args}");

    /**
     * If they try use a sub command that doesn't exist
     */
    public static MistString COMMAND_INVALID_SUB = STARTUP_GROUP.create("command.invalid-sub", "&cThat command doesn't exist. Try /{label} help");

    /**
     * The optional arguments label
     */
    public static MistString COMMAND_LABEL_OPTIONAL_ARGS = STARTUP_GROUP.create("command.label-optional-args", "optional arguments");

    /**
     * The required arguments label
     */
    public static MistString COMMAND_LABEL_REQUIRED_ARGS = STARTUP_GROUP.create("command.label-required-args", "required arguments");

    /**
     * Message sent prompting to enter a value
     */
    public static MistString CONFIG_ENTER_VALUE = STARTUP_GROUP.create("config.enter-value", "&7Enter a new value to set (Type 'cancel' to cancel)");

    /**
     * Name of the confirm icon in the confirm inventory
     */
    public static MistString INTERFACE_CONFIRM_CONFIRM_NAME = STARTUP_GROUP.create("interface.confirm.confirm.name", "&a&lConfirm");

    /**
     * Lore of the confirm icon in the confirm inventory
     */
    public static MistString INTERFACE_CONFIRM_CONFIRM_LORE = STARTUP_GROUP.create("interface.confirm.confirm.lore", "&7&o(Click to confirm)");

    /**
     * Name of the deny icon in the confirm inventory
     */
    public static MistString INTERFACE_CONFIRM_DENY_NAME = STARTUP_GROUP.create("interface.confirm.deny.name", "&c&lDeny");

    /**
     * Lore of the deny icon in the confirm inventory
     */
    public static MistString INTERFACE_CONFIRM_DENY_LORE = STARTUP_GROUP.create("interface.confirm.deny.lore", "&7&o(Click to deny)");

    /**
     * The message if a new version is found
     */
    public static MistString UPDATE_AVAILABLE = STARTUP_GROUP.create("update.available", "&2You're on an &a{status}&2 version of &a{plugin_name}&2.\n"
            + "&2Current version: &a{current}&2; Latest version: &a{new}\n"
            + "&2URL: &ahttps://spigotmc.org/resources/{resource_id}/.");


    /**
     * Load the {@link PluginLocale} into the server, setting values
     * if not there, or loading the values into memory
     * <p>
     * Call in the {@link SpigotPlugin#onPluginEnable()} to load plugin locale
     *
     * @param settings The instance of {@link PluginLocale} to load
     */
    public static void loadLocale(PluginLocale settings) {
        // Set instance
        LOCALE_FILE = settings;
        // Load settings loadLocale
        LOCALE_FILE.load();

        // Load our other custom settings
        settings.loadLocale();
        LOCALE_FILE.saveChanges();

        // Reset cache
        invalidateCache();

        // Load locale groups
        STARTUP_GROUP.load();
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
     * @deprecated Should always provide a default value for redundancy
     */
    @Deprecated
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
        // If no key just return default so doesn't get set in file.
        if (key.equalsIgnoreCase(""))
            return def;

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

        // Not found so add default
        LOCALE_FILE.addDefault(key, def);
        LOCALE_FILE.saveChanges();

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
