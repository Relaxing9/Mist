/**
 * Copyright © 2020 Property of Illuzionz Studios, LLC
 * All rights reserved. No part of this publication may be reproduced, distributed, or
 * transmitted in any form or by any means, including photocopying, recording, or other
 * electronic or mechanical methods, without the prior written permission of the publisher,
 * except in the case of brief quotations embodied in critical reviews and certain other
 * noncommercial uses permitted by copyright law. Any licensing of this software overrides
 * this statement.
 */
package com.illuzionzstudios.mist.config.serialization;

import com.illuzionzstudios.mist.config.DataSerializable;
import com.illuzionzstudios.mist.plugin.SpigotPlugin;
import lombok.Getter;

import java.io.*;

/**
 * An interface to load certain types of files
 *
 * @param <T> Type of object being loaded
 */
public abstract class FileLoader<T> {

    /**
     * Our T object to get properties from
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
     * @param fileName The file name without .json
     * @param extension File extension to use
     */
    public FileLoader(String directory, String fileName, String extension) {
        this(new File(SpigotPlugin.getInstance().getDataFolder() + "/" + directory, fileName + "." + extension), extension);
    }

    /**
     * @param file File to load from
     */
    public FileLoader(File file, String extension) {
        this.file = file;

        object = loadObject(file);

        // Get name without extension
        this.name = file.getName().split("\\.")[0];
        this.extension = extension;
    }

    /**
     * Serialize a {@link com.illuzionzstudios.mist.config.DataSerializable} object
     */
    public boolean serialize(DataSerializable<T> object) {
        return serialize(object.serialize());
    }

    /**
     * Serialize a {@link T} to this file
     *
     * @param object {@link T to serialize}
     */
    public boolean serialize(T object) {
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
     * @param file Load basic object to memory from disk
     */
    public abstract T loadObject(File file);

}
