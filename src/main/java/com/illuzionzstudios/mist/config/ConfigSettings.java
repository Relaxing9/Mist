package com.illuzionzstudios.mist.config;

import com.illuzionzstudios.mist.config.format.CommentStyle;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * A group of config settings that we load from a config file.
 */
@RequiredArgsConstructor
public class ConfigSettings {

    /**
     * Name of this group
     */
    @Getter
    public final String name;

    /**
     * List of strings in group
     */
    private final List<ConfigSetting> settings = new ArrayList<>();

    /**
     * Create a new config setting in a group
     */
    public ConfigSetting create(@NotNull String key, @NotNull Object defaultValue, String... comment) {
        ConfigSetting setting = new ConfigSetting(key, defaultValue, comment);
        settings.add(setting);
        return setting;
    }

    public ConfigSetting create(@NotNull String key, @NotNull Object defaultValue, CommentStyle commentStyle, String... comment) {
        ConfigSetting setting = new ConfigSetting(key, defaultValue, commentStyle, comment);
        settings.add(setting);
        return setting;
    }

    /**
     * Load all strings into cache and locale
     */
    public final void load() {
        settings.forEach(setting -> {
            // Set config file then load
            setting.loadSetting(PluginSettings.SETTINGS_FILE);
        });
    }

}
