package com.illuzionzstudios.mist.config.serialization.loader;

import lombok.Getter;

/**
 * Works by taking a base object and loading that into another
 * object. Also contains a way to implement saving the other
 * way for serialization
 *
 * @param <T> The type of object being loaded
 * @param <L> The type of object loading from
 */
public abstract class ObjectLoader<T, L> {

    /**
     * Our data object to get properties from
     */
    @Getter
    protected T object;

    /**
     * The section to load and save from
     */
    @Getter
    protected L loader;

    public ObjectLoader(final L load) {
        this.loader = load;

        object = loadObject(load);
    }

    /**
     * Used to save our updated object to the loader.
     * Updates the loader then can call {@link #getObject()} to get
     * the updated object
     *
     * @return If was saved successfully
     */
    public abstract boolean save();

    /**
     * Load basic object to memory from disk.
     * Loaded on creation of loader
     */
    public abstract T loadObject(final L file);

}
