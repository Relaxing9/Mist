package com.illuzionzstudios.mist.config

import com.illuzionzstudios.mist.config.format.CommentStyle
import lombok.*
import java.util.function.Consumer

/**
 * A group of config settings that we load from a config file.
 */
@RequiredArgsConstructor
class ConfigSettings {
    /**
     * Name of this group
     */
    @Getter
    val name: String? = null

    /**
     * List of strings in group
     */
    private val settings: MutableList<ConfigSetting> = ArrayList()

    /**
     * Create a new config setting in a group
     */
    fun create(key: String, defaultValue: Any, vararg comment: String?): ConfigSetting {
        val setting = ConfigSetting(key, defaultValue, *comment)
        settings.add(setting)
        return setting
    }

    fun create(key: String, defaultValue: Any, commentStyle: CommentStyle, vararg comment: String?): ConfigSetting {
        val setting = ConfigSetting(key, defaultValue, commentStyle, *comment)
        settings.add(setting)
        return setting
    }

    /**
     * Load all strings into cache and locale
     */
    fun load() {
        settings.forEach(Consumer { setting: ConfigSetting ->
            // Set config file then load
            setting.loadSetting(PluginSettings.Companion.SETTINGS_FILE!!)
        })
    }
}