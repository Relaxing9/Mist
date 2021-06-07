package com.illuzionzstudios.mist.config.serialization.loader;

import com.google.gson.*;
import com.illuzionzstudios.mist.config.YamlConfig;

import java.io.File;

public class YamlFileLoader extends FileLoader<YamlConfig> {

    /**
     * @param directory The directory from plugin folder
     * @param fileName The file name without .yml
     */
    public YamlFileLoader(String directory, String fileName) {
        super(directory, fileName, "yml");
    }

    /**
     * Used to save our {@link JsonObject} to disk
     * at {@link #file}
     *
     * @return If was saved successfully
     */
    public boolean save() {
        object.load();
        return object.saveChanges();
    }

    @Override
    public YamlConfig loadObject(final File file) {
        YamlConfig config = new YamlConfig(file);
        config.load();
        config.saveChanges();
        return config;
    }

    /**
     * Shorthand way to get {@link YamlConfig} from a file
     *
     * @param directory The directory from plugin folder
     * @param fileName The file name without .yaml
     * @return Found {@link YamlConfig} or a new {@link YamlConfig}
     */
    public static YamlConfig of(String directory, String fileName) {
        return new YamlFileLoader(directory, fileName).getObject();
    }

}
