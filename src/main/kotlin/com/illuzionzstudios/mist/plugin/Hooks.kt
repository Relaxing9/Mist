package com.illuzionzstudios.mist.plugin

import com.illuzionzstudios.mist.controller.PluginController
import me.clip.placeholderapi.PlaceholderAPI
import org.bukkit.Bukkit
import org.bukkit.entity.Player

/**
 * External plugin hooks. Contains methods to interact with hooks
 */
object Hooks: PluginController {

    /**
     * PlaceholderAPI Enabled
     */
    var papiEnabled: Boolean = false
    private set

    override fun initialize(plugin: SpigotPlugin) {
        // Check if plugins loaded
        this.papiEnabled = Bukkit.getServer().pluginManager.getPlugin("PlaceholderAPI") != null
    }

    override fun stop(plugin: SpigotPlugin) {
    }

    /**
     * Replace placeholders if enabled
     */
    fun papiPlaceholders(string: String, player: Player): String = if (papiEnabled) PlaceholderAPI.setPlaceholders(player, string) else string
}