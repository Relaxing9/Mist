/**
 * Copyright © 2020 Property of Illuzionz Studios, LLC
 * All rights reserved. No part of this publication may be reproduced, distributed, or
 * transmitted in any form or by any means, including photocopying, recording, or other
 * electronic or mechanical methods, without the prior written permission of the publisher,
 * except in the case of brief quotations embodied in critical reviews and certain other
 * noncommercial uses permitted by copyright law. Any licensing of this software overrides
 * this statement.
 */
package com.illuzionzstudios.mist.config.json;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.illuzionzstudios.mist.plugin.SpigotPlugin;
import lombok.Getter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

/**
 * Simply used to load a {@link com.google.gson.JsonObject} from a {@link java.io.File}
 */
public class JsonFileLoader {

    /**
     * Our JSON object to get properties from
     */
    @Getter
    protected JsonObject json;

    /**
     * File location for template on disk
     */
    @Getter
    protected final File file;

    /**
     * @param directory The directory from plugin folder
     * @param fileName The file name without .json
     */
    public JsonFileLoader(String directory, String fileName) {
        JsonObject tempObject;

        file = new File(SpigotPlugin.getInstance().getDataFolder() + "/" + directory, fileName + ".json");

        // Try assign JSON file
        try {
            tempObject = new JsonParser().parse(new FileReader(file)).getAsJsonObject();
        } catch (FileNotFoundException e) {
            // If couldn't load, it becomes a new object
            tempObject = new JsonObject();
        }

        json = tempObject;
    }

    /**
     * Shorthand way to get {@link JsonObject} from a file
     *
     * @param directory The directory from plugin folder
     * @param fileName The file name without .json
     * @return Found JsonObject or a new {@link JsonObject}
     */
    public static JsonObject of(String directory, String fileName) {
        return new JsonFileLoader(directory, fileName).getJson();
    }

}
