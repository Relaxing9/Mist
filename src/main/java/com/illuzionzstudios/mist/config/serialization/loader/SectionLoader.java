package com.illuzionzstudios.mist.config.serialization.loader;

import com.illuzionzstudios.mist.config.ConfigSection;
import lombok.Getter;

/**
 * Works much like {@link FileLoader} but loads a configuration
 * section. Good if we want to loop through a section
 * with nodes and build a custom object from there.
 * <p>
 * With a config that looks like
 * items:
 * itemone:
 * example: 1
 * itemtwo:
 * example: 2
 * Loop over items manually and pass each child config section to this class
 * and invoke the getObject() on this class.
 */
public abstract class SectionLoader<T> {

    /**
     * Our data object to get properties from
     */
    @Getter
    protected T object;

    /**
     * The section to load and save from
     */
    @Getter
    protected ConfigSection section;

    /**
     * Name of the config section
     */
    @Getter
    protected final String sectionName;

    public SectionLoader(ConfigSection section) {
        this.section = section;
        this.sectionName = section.getName();

        object = loadObject(section);
    }

    /**
     * Used to save our updated object to the config section.
     * Updates the section then can call {@link #getSection()} to get
     * the updated section
     *
     * @return If was saved successfully
     */
    public abstract boolean save();

    /**
     * Load basic object to memory from disk.
     * Loaded on creation of loader
     */
    public abstract T loadObject(final ConfigSection file);

}
