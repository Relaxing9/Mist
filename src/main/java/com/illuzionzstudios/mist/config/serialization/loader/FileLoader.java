package com.illuzionzstudios.mist.config.serialization.loader;

import com.illuzionzstudios.mist.config.serialization.DataSerializable;
import com.illuzionzstudios.mist.plugin.SpigotPlugin;
import lombok.Getter;

import java.io.File;

/**
 * An interface to load certain types of files
 *
 * @param <T> Type of data object being loaded
 */
public abstract class FileLoader<T> {

    /**
     * Our data object to get properties from
     */
    @Getter
    protected T object;

    /**
     * File location for template on disk
     */
    @Getter
    protected final File file;

    /**
     * Name of the file without extensions
     */
    @Getter
    protected final String name;

    /**
     * Extension for this file
     */
    @Getter
    protected final String extension;

    /**
     * @param directory The directory from plugin folder
     * @param fileName  The file name without extension
     * @param extension File extension to use
     */
    public FileLoader(final String directory, final String fileName, final String extension) {
        this(new File(SpigotPlugin.getInstance().getDataFolder() + "/" + directory, fileName + "." + extension), extension);
    }

    /**
     * @param file File to load from
     */
    public FileLoader(final File file, final String extension) {
        this.file = file;

        object = loadObject(file);

        // Get name without extension
        this.name = file.getName().split("\\.")[0];
        this.extension = extension;
    }

    /**
     * Serialize a {@link DataSerializable} object and save to disk
     */
    public boolean serialize(final DataSerializable<T> object) {
        return serialize(object.serialize());
    }

    /**
     * Serialize a {@link T} to this file
     *
     * @param object {@link T to serialize}
     */
    public boolean serialize(final T object) {
        this.object = object;
        return save();
    }

    /**
     * Used to save our {@link T} to disk
     * at {@link #file}
     *
     * @return If was saved successfully
     */
    public abstract boolean save();

    /**
     * Load basic object to memory from disk.
     * Loaded on creation of loader
     */
    public abstract T loadObject(final File file);

}
