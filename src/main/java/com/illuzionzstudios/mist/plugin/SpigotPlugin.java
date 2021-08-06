package com.illuzionzstudios.mist.plugin;

import com.illuzionzstudios.mist.Listeners;
import com.illuzionzstudios.mist.Logger;
import com.illuzionzstudios.mist.command.SpigotCommand;
import com.illuzionzstudios.mist.command.SpigotCommandGroup;
import com.illuzionzstudios.mist.config.PluginSettings;
import com.illuzionzstudios.mist.config.locale.PluginLocale;
import com.illuzionzstudios.mist.data.controller.BukkitPlayerController;
import com.illuzionzstudios.mist.data.controller.PlayerDataController;
import com.illuzionzstudios.mist.data.database.Database;
import com.illuzionzstudios.mist.data.player.BukkitPlayer;
import com.illuzionzstudios.mist.scheduler.MinecraftScheduler;
import com.illuzionzstudios.mist.scheduler.bukkit.BukkitScheduler;
import com.illuzionzstudios.mist.ui.InterfaceController;
import com.illuzionzstudios.mist.util.UpdateChecker;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
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

    /**
     * The main command for this plugin, can be {@code null}
     */
    @Getter
    protected SpigotCommandGroup mainCommand;

    //  -------------------------------------------------------------------------
    //  Player data - Only loaded if we choose to use player data
    //  -------------------------------------------------------------------------

    /**
     * Our player controller for our player data objects
     * Also used as a flag if we want to use player data
     */
    protected BukkitPlayerController<SpigotPlugin, ? extends BukkitPlayer> playerController;

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
            // Try save config if found
            PluginSettings.loadSettings(getPluginSettings());
            PluginLocale.loadLocale(getPluginLocale());

            // Load locale groups
            PluginLocale.GENERAL_GROUP.load();
            PluginLocale.COMMAND_GROUP.load();
            PluginLocale.CONFIG_GROUP.load();
            PluginLocale.INTERFACE_GROUP.load();
            PluginLocale.UPDATE_GROUP.load();

            // Enable our scheduler
            new BukkitScheduler(this).initialize();
            InterfaceController.INSTANCE.initialize(this);

            onReloadablesStart();

            // Check update
            UpdateChecker.checkVersion(Bukkit.getServer().getConsoleSender());

            onPluginEnable();

            // Register main events
            registerListener(this);
            registerListener(InterfaceController.INSTANCE);

            // Connect to database and allow player data
            if (playerController != null) {
                playerController.initialize(this);
            }
        } catch (final Throwable ex) {
            Logger.displayError(ex, "Error enabling plugin");

            // Errors on startup could break the plugin,
            // so just kill it
            this.isEnabled = false;
            setEnabled(false);
        }
    }

    @Override
    public final void onDisable() {
        // Don't shutdown if wasn't enabled
        if (!isEnabled) return;

        try {
            onPluginDisable();
        } catch (final Throwable t) {
            Logger.warn("Plugin might not shut down property. Got " + t.getClass().getSimpleName() + ": " + t.getMessage());
            t.printStackTrace();
        }

        unregisterReloadables();

        InterfaceController.INSTANCE.stop(this);

        // Try save all player data
        if (playerController != null) {
            playerController.stop(this);
        }

        Objects.requireNonNull(INSTANCE, "Plugin " + getName() + " has already been shutdown!");
        INSTANCE = null;
    }

    /**
     * Attempt to reload the plugin
     */
    public final void reload() {
        Logger.info("Reloading plugin " + getPluginName() + " v" + getPluginVersion());
        Logger.info(" ");

        reloading = true;

        try {
            unregisterReloadables();
            onPluginPreReload();
            listeners.unregister();

            // Load settings and locale
            // Try save config if found
            PluginSettings.loadSettings(getPluginSettings());
            PluginLocale.loadLocale(getPluginLocale());
            PluginLocale.invalidateCache();

            // Restart tickers
            MinecraftScheduler.get().initialize();
            InterfaceController.INSTANCE.initialize(this);
            onPluginReload();

            onReloadablesStart();

            // Register main events
            registerListener(this);
            registerListener(InterfaceController.INSTANCE);
        } catch (final Throwable ex) {
            Logger.displayError(ex, "Error reloading plugin");
        } finally {
            reloading = false;
        }
    }

    /**
     * Un register stuff when reloading
     */
    private void unregisterReloadables() {
        // Stop ticking all tasks
        MinecraftScheduler.get().stopInvocation();
        InterfaceController.INSTANCE.stop(this);

        if (getMainCommand() != null && getMainCommand().isRegistered())
            getMainCommand().unregister();
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

    /**
     * Opt in to using custom player data.
     *
     * This is optional because we don't to load and save
     * data if we don't need it
     *
     * @param playerClass The class for our custom player data
     * @param database The type of database to use to save data
     * @param playerController Our custom player controller for operations
     */
    protected <BP extends BukkitPlayer> void initializePlayerData(Class<? extends BukkitPlayer> playerClass, Database database,
                                                               BukkitPlayerController<SpigotPlugin, BP> playerController) {
        this.playerController = playerController;

        new PlayerDataController<>().initialize(playerClass, database);
    }

    //  -------------------------------------------------------------------------
    //  Stuff we need to implement
    //  -------------------------------------------------------------------------

    /**
     * @return Our custom implementation of {@link PluginSettings}
     */
    public abstract PluginSettings getPluginSettings();

    /**
     * @return Get the {@link PluginLocale} instance being used for this plugin
     */
    public abstract PluginLocale getPluginLocale();

    /**
     * @return Plugin's id for update checking on spigot
     */
    public abstract int getPluginId();

    /**
     * @param command Register a new {@link SpigotCommandGroup}
     */
    protected void registerMainCommand(SpigotCommandGroup command, String... labels) {
        this.mainCommand = command;
        this.mainCommand.register(labels);
    }

    /**
     * Check if a given label is for the main plugin command
     *
     * @param label Label to check
     * @return If it's an aliases for the main command
     */
    public static boolean isMainCommand(String label) {
        return getInstance().getMainCommand() != null && getInstance().getMainCommand().getLabel().equalsIgnoreCase(label);
    }

    /**
     * Check for updates
     */
    @EventHandler
    public void checkUpdates(PlayerJoinEvent event) {
        UpdateChecker.checkVersion(event.getPlayer());
    }

}
