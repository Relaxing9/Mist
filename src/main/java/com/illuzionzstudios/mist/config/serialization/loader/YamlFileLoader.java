package com.illuzionzstudios.mist.config.serialization.loader;

import com.illuzionzstudios.mist.config.YamlConfig;

import java.io.File;

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
