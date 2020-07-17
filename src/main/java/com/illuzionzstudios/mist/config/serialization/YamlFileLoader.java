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

import com.google.gson.*;
import com.illuzionzstudios.mist.config.ConfigSection;
import com.illuzionzstudios.mist.config.YamlConfig;

import java.io.*;

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
    public YamlConfig loadObject() {
        YamlConfig config = new YamlConfig(file);
        config.load();
        config.saveChanges();
        return config;
    }

    /**
     * Shorthand way to get {@link JsonObject} from a file
     *
     * @param directory The directory from plugin folder
     * @param fileName The file name without .json
     * @return Found JsonObject or a new {@link JsonObject}
     */
    public static ConfigSection of(String directory, String fileName) {
        return new YamlFileLoader(directory, fileName).getObject();
    }

}
