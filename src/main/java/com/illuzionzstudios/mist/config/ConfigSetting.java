package com.illuzionzstudios.mist.config;

import com.illuzionzstudios.mist.config.format.CommentStyle;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * This represents a set option in a {@link YamlConfig}
 * It provides convenience to getting and setting this value. This way
 * we don't always have to make calls to the {@link YamlConfig} object
 * and can just access this. This is usually for values that we know will be in
 * the config not for custom configurations. For instance the language to use
 */
public class ConfigSetting {

    /**
     * The {@link YamlConfig} instance this value is apart of
     */
    private final YamlConfig config;

    /**
     * The node path (or key) this value is set at
     */
    @Getter
    private final String key;

    public ConfigSetting(@NotNull YamlConfig config, @NotNull String key) {
        this.config = config;
        this.key = key;
    }

    public ConfigSetting(@NotNull YamlConfig config, @NotNull String key, @NotNull Object defaultValue, String... comment) {
        this.config = config;
        this.key = key;
        config.setDefault(key, defaultValue, comment);
    }

    public ConfigSetting(@NotNull YamlConfig config, @NotNull String key, @NotNull Object defaultValue, CommentStyle commentStyle, String... comment) {
        this.config = config;
        this.key = key;
        config.setDefault(key, defaultValue, commentStyle, comment);
    }

    /**
     * Set our value
     *
     * @param value Object to place as a value
     */
    public void set(@Nullable Object value) {
        config.set(key, value);
    }

    //  -------------------------------------------------------------------------
    //  Shorthand to get values from the config
    //  -------------------------------------------------------------------------

    public List<Integer> getIntegerList() {
        return config.getIntegerList(key);
    }

    public List<String> getStringList() {
        return config.getStringList(key);
    }

    public boolean getBoolean() {
        return config.getBoolean(key);
    }

    public boolean getBoolean(boolean def) {
        return config.getBoolean(key, def);
    }

    public int getInt() {
        return config.getInt(key);
    }

    public int getInt(int def) {
        return config.getInt(key, def);
    }

    public long getLong() {
        return config.getLong(key);
    }

    public long getLong(long def) {
        return config.getLong(key, def);
    }

    public double getDouble() {
        return config.getDouble(key);
    }

    public double getDouble(double def) {
        return config.getDouble(key, def);
    }

    public String getString() {
        return config.getString(key);
    }

    public String getString(String def) {
        return config.getString(key, def);
    }

    public Object getObject() {
        return config.get(key);
    }

    public Object getObject(Object def) {
        return config.get(key, def);
    }

    public <T> T getObject(@NotNull Class<T> clazz) {
        return config.getObject(key, clazz);
    }

    public <T> T getObject(@NotNull Class<T> clazz, @Nullable T def) {
        return config.getObject(key, clazz, def);
    }

    public char getChar() {
        return config.getChar(key);
    }

    public char getChar(char def) {
        return config.getChar(key, def);
    }

}
