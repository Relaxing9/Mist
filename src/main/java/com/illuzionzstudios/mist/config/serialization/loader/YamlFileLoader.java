package com.illuzionzstudios.mist.config.serialization.loader;

import com.google.gson.*;
import com.illuzionzstudios.mist.Logger;
import com.illuzionzstudios.mist.config.YamlConfig;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Provides a way to load on object from a YAML file.
 *
 * @param <T> The object to load
 */
public abstract class YamlFileLoader<T> extends FileLoader<T> {

    /**
     * The YAML file for this loader
     */
    protected YamlConfig config;

    /**
     * @param directory The directory from plugin folder
     * @param fileName The file name without .yml
     */
    public YamlFileLoader(String directory, String fileName) {
        super(directory, fileName, "yml");
    }

    /**
     * Save the object T into a json object to be saved to disk
     */
    public abstract void saveYaml();

    /**
     * Used to save our {@link JsonObject} to disk
     * at {@link #file}
     *
     * @return If was saved successfully
     */
    @Override
    public boolean save() {
        // Load new object before saving
        saveYaml();

        return config.save();
    }

    @Override
    public T loadObject(final File file) {
        config = new YamlConfig(file);
        config.load();
        return loadYamlObject();
    }

    /**
     * Load the object from a {@link YamlConfig}
     *
     * @return The loaded object
     */
    public abstract T loadYamlObject();

}
