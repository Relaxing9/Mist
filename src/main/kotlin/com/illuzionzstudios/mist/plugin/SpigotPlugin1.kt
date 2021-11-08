package com.illuzionzstudios.mist.plugin

import com.illuzionzstudios.mist.Logger.Companion.displayError
import com.illuzionzstudios.mist.Logger.Companion.info
import com.illuzionzstudios.mist.Logger.Companion.warn
import com.illuzionzstudios.mist.command.SpigotCommand
import com.illuzionzstudios.mist.command.SpigotCommandGroup
import com.illuzionzstudios.mist.config.PluginSettings
import com.illuzionzstudios.mist.config.locale.PluginLocale
import com.illuzionzstudios.mist.data.controller.BukkitPlayerController
import com.illuzionzstudios.mist.data.controller.PlayerDataController
import com.illuzionzstudios.mist.data.database.Database
import com.illuzionzstudios.mist.data.player.AbstractPlayer
import com.illuzionzstudios.mist.data.player.AbstractPlayerData
import com.illuzionzstudios.mist.data.player.BukkitPlayer
import com.illuzionzstudios.mist.model.UpdateChecker
import com.illuzionzstudios.mist.plugin.SpigotPlugin
import com.illuzionzstudios.mist.scheduler.MinecraftScheduler
import com.illuzionzstudios.mist.scheduler.bukkit.BukkitScheduler
import com.illuzionzstudios.mist.ui.InterfaceController
import lombok.*
import org.bukkit.Bukkit
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.util.*

/**
 * Represents an instance of a custom spigot plugin with our
 * "Mist" functionality
 *
 *
 * The plugin is only designed to work on versions
 * {@version 1.8.8} to {@version 1.17.1}
 */
abstract class SpigotPlugin : JavaPlugin(), Listener {
    /**
     * An easy way to handle listeners for reloading
     */
    private val reloadables = Reloadables(this)

    @Getter
    protected var checkUpdates = false

    /**
     * If this plugin is currently enabled and running.
     * Checked at different loading stages
     */
    protected var isEnabled = true

    /**
     * The main command for this plugin, can be `null`
     */
    @Getter
    protected var mainCommand: SpigotCommandGroup? = null

    /**
     * Our player controller for our player data objects
     * Also used as a flag if we want to use player data
     */
    protected var playerController: BukkitPlayerController<out BukkitPlayer>? = null

    /**
     * Called when the plugin is loaded into the server
     */
    abstract fun onPluginLoad()

    /**
     * Called before the actual plugin is enabled
     */
    abstract fun onPluginPreEnable()

    /**
     * Called when the plugin is finally being enabled
     */
    abstract fun onPluginEnable()

    /**
     * Called when the plugin is being disabled
     */
    abstract fun onPluginDisable()

    /**
     * Called before the plugin is reloaded
     * This means config reloads, /reload, or if the plugin is
     * disabled and then enabled while the server is running
     */
    abstract fun onPluginPreReload()

    /**
     * Called after the plugin is reloaded
     * This means config reloads, /reload, or if the plugin is
     * disabled and then enabled while the server is running
     */
    abstract fun onPluginReload()

    /**
     * This method is called when registering things like listeners.
     *
     *
     * In your plugin use it to register commands, events etc.
     */
    abstract fun onRegisterReloadables()
    override fun onLoad() {
        try {
            // Try set instance
            instance
        } catch (ex: Throwable) {
            // If can't set manually
            INSTANCE = this
            throw ex
        }
        onPluginLoad()
    }

    override fun onEnable() {
        if (!isEnabled) return

        // Pre start the plugin
        onPluginPreEnable()

        // Return if plugin pre start indicated a fatal problem
        if (!isEnabled || !isEnabled()) return

        // Main enabled
        try {
            // Startup logo
            if (startupLogo != null) Arrays.stream(startupLogo).sequential().forEach { obj: String? -> info() }

            // Load settings and locale
            // Try save config if found
            PluginSettings.Companion.loadSettings(pluginSettings)
            PluginLocale.Companion.loadLocale(pluginLocale)

            // Enable our scheduler
            BukkitScheduler(this).initialize()
            reloadables.registerController(InterfaceController.INSTANCE)
            onRegisterReloadables()

            // Check update
            UpdateChecker.checkVersion(Bukkit.getServer().consoleSender)
            onPluginEnable()

            // Register main events
            registerListener(this)
            // Start reloadables
            reloadables.start()

            // Connect to database and allow player data
            if (playerController != null) {
                playerController!!.initialize(this)
            }
        } catch (ex: Throwable) {
            displayError(ex, "Error enabling plugin")

            // Errors on startup could break the plugin,
            // so just kill it
            this.isEnabled = false
            setEnabled(false)
        }
    }

    override fun onDisable() {
        // Don't shutdown if wasn't enabled
        if (!isEnabled) return
        try {
            onPluginDisable()
        } catch (t: Throwable) {
            warn("Plugin might not shut down property. Got " + t.javaClass.simpleName + ": " + t.message)
            t.printStackTrace()
        }
        unregisterReloadables()

        // Try save all player data
        if (playerController != null) {
            playerController!!.stop(this)
        }
        Objects.requireNonNull(INSTANCE, "Plugin $name has already been shutdown!")
        INSTANCE = null
    }

    /**
     * Attempt to reload the plugin
     */
    fun reload() {
        info("Reloading plugin " + pluginName + " v" + pluginVersion)
        info(" ")
        reloading = true
        try {
            unregisterReloadables()
            onPluginPreReload()

            // Load settings and locale
            // Try save config if found
            PluginSettings.Companion.loadSettings(pluginSettings)
            PluginLocale.Companion.loadLocale(pluginLocale)

            // Restart tickers
            MinecraftScheduler.Companion.get()!!.initialize()

            // Reload controllers etc
            reloadables.registerController(InterfaceController.INSTANCE)
            onPluginReload()
            onRegisterReloadables()

            // Register main events
            registerListener(this)
            reloadables.start()
            if (playerController != null) {
                playerController!!.initialize(this)
            }
        } catch (ex: Throwable) {
            displayError(ex, "Error reloading plugin")
        } finally {
            reloading = false
        }
    }

    /**
     * Un register stuff when reloading
     */
    private fun unregisterReloadables() {
        // Stop ticking all tasks
        MinecraftScheduler.Companion.get()!!.stopInvocation()
        InterfaceController.INSTANCE.stop(this)
        if (getMainCommand() != null && getMainCommand().isRegistered()) getMainCommand().unregister()
        reloadables.shutdown()
    }
    //  -------------------------------------------------------------------------
    //  Additional features of our main plugin
    //  -------------------------------------------------------------------------
    /**
     * @param listener Register a listener for this plugin
     */
    protected fun registerListener(listener: Listener?) {
        reloadables.registerEvent(listener!!)
    }

    /**
     * The start-up fancy logo
     *
     * @return null by default
     */
    val startupLogo: Array<String>?
        get() = null
    //  -------------------------------------------------------------------------
    //  Stuff we need to implement
    //  -------------------------------------------------------------------------
    /**
     * Opt in to using custom player data.
     *
     *
     * This is optional because we don't to load and save
     * data if we don't need it
     *
     * @param playerClass      The class for our custom player data
     * @param database         The type of database to use to save data
     * @param playerController Our custom player controller for operations
     */
    protected fun <BP : BukkitPlayer?> initializePlayerData(
        playerClass: Class<out BukkitPlayer?>?, database: Database?,
        playerController: BukkitPlayerController<BP>?
    ) {
        this.playerController = playerController
        PlayerDataController<AbstractPlayer, AbstractPlayerData<*>>().initialize(playerClass, database)
    }

    /**
     * @return Our custom implementation of [PluginSettings]
     */
    abstract val pluginSettings: PluginSettings

    /**
     * @return Get the [PluginLocale] instance being used for this plugin
     */
    abstract val pluginLocale: PluginLocale

    /**
     * @return Plugin's id for update checking on spigot
     */
    abstract val pluginId: Int

    /**
     * @param command Register a [SpigotCommand] for this plugin
     */
    protected fun registerCommand(command: SpigotCommand?) {
        reloadables.registerCommand(command!!)
    }

    /**
     * @param command Register a [SpigotCommand] for this plugin
     */
    protected fun registerCommand(command: SpigotCommandGroup?, vararg labels: String?) {
        reloadables.registerCommand(command!!, *labels)
    }

    /**
     * @param command Register a new [SpigotCommandGroup]
     */
    protected fun registerMainCommand(command: SpigotCommandGroup?, vararg labels: String?) {
        mainCommand = command
        mainCommand!!.register(*labels)
    }

    companion object {
        //  -------------------------------------------------------------------------
        //  Internal plugin settings that determine if certain things are loaded
        //  -------------------------------------------------------------------------
        /**
         * If our [SpigotPlugin] is currently reloading
         */
        @Getter
        @Volatile
        private var reloading = false
        //  -------------------------------------------------------------------------
        //  Static instances that stay final
        //  -------------------------------------------------------------------------
        /**
         * Singleton instance of our [SpigotPlugin]
         */
        @Volatile
        private var INSTANCE: SpigotPlugin? = null// Assign if null

        /**
         * Return our instance of the [SpigotPlugin]
         *
         *
         * Should be overridden in your own [SpigotPlugin] class
         * as a way to implement your own methods per plugin
         *
         * @return This instance of the plugin
         */
        val instance: SpigotPlugin?
            get() {
                // Assign if null
                if (INSTANCE == null) {
                    INSTANCE = getPlugin(SpigotPlugin::class.java)
                    Objects.requireNonNull(INSTANCE, "Cannot create instance of plugin. Did you reload?")
                }
                return INSTANCE
            }
        //  -------------------------------------------------------------------------
        //  Variables specific to the plugin instance
        //  -------------------------------------------------------------------------
        /**
         * Get if the instance that is used across the library has been set. Normally it
         * is always set, except for testing.
         *
         * @return if the instance has been set.
         */
        fun hasInstance(): Boolean {
            return INSTANCE != null
        }

        /**
         * @return The name of the [SpigotPlugin] from the plugin description
         */
        val pluginName: String
            get() = instance!!.description.name

        /**
         * @return The version of the [SpigotPlugin] from the plugin description
         */
        val pluginVersion: String
            get() = instance!!.description.version
        //  -------------------------------------------------------------------------
        //  Player data - Only loaded if we choose to use player data
        //  -------------------------------------------------------------------------
        /**
         * Shortcut for getFile()
         *
         * @return plugin's jar file
         */
        val source: File
            get() = instance!!.file
        //  -------------------------------------------------------------------------
        //  Plugin loading methods
        //  -------------------------------------------------------------------------
        /**
         * Check if a given label is for the main plugin command
         *
         * @param label Label to check
         * @return If it's an aliases for the main command
         */
        fun isMainCommand(label: String?): Boolean {
            return instance.getMainCommand() != null && instance.getMainCommand().getLabel()
                .equals(label, ignoreCase = true)
        }
    }
}