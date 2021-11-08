package com.illuzionzstudios.mist.config.serialization.loader

import com.google.gson.JsonObject
import com.illuzionzstudios.mist.config.YamlConfig
import java.io.File

/**
 * Provides a way to load on object from a YAML file.
 *
 * @param <T> The object to load
</T> */
abstract class YamlFileLoader<T>
/**
 * @param directory The directory from plugin folder
 * @param fileName  The file name without .yml
 */
    (directory: String, fileName: String) : FileLoader<T>(directory, fileName, "yml") {
    /**
     * The YAML file for this loader
     */
    protected var config: YamlConfig? = null

    /**
     * Save the object T into a yaml object to be saved to disk
     */
    abstract fun saveYaml()

    /**
     * Used to save our [JsonObject] to disk
     * at [.file]
     *
     * @return If was saved successfully
     */
    override fun save(): Boolean {
        // Load new object before saving
        saveYaml()
        return config!!.save()
    }

    override fun loadObject(file: File): T {
        config = YamlConfig(file)
        config!!.load()
        return loadYamlObject()
    }

    /**
     * Load the object from a [YamlConfig]
     *
     * @return The loaded object
     */
    abstract fun loadYamlObject(): T
}