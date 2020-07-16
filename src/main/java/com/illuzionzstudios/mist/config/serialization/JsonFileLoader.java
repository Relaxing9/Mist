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
import com.illuzionzstudios.mist.Logger;

import java.io.*;

/**
 * Simply used to load a {@link com.google.gson.JsonObject} from a {@link java.io.File}
 */
public class JsonFileLoader extends FileLoader<JsonObject> {

    /**
     * @param directory The directory from plugin folder
     * @param fileName The file name without .json
     */
    public JsonFileLoader(String directory, String fileName) {
        super(directory, fileName, "json");
    }


    /**
     * Used to save our {@link JsonObject} to disk
     * at {@link #file}
     *
     * @return If was saved successfully
     */
    @Override
    public boolean save() {
        try {
            FileWriter writer = new FileWriter(file);

            Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
            JsonParser jp = new JsonParser();
            JsonElement je = jp.parse(object.toString());
            String prettyJsonString = gson.toJson(je);

            writer.write(prettyJsonString);

            writer.flush();
            writer.close();
            return true;
        } catch (IOException e) {
            Logger.displayError(e, "Could not save file to disk: " + file.getName());
        }

        return false;
    }

    @Override
    public JsonObject loadObject(File file) {
        JsonObject tempObject;

        // Try assign JSON file
        try {
            tempObject = new JsonParser().parse(new FileReader(file)).getAsJsonObject();
        } catch (FileNotFoundException e) {
            // If couldn't load, it becomes a new object
            tempObject = new JsonObject();
        }

        return tempObject;
    }

    /**
     * Shorthand way to get {@link JsonObject} from a file
     *
     * @param directory The directory from plugin folder
     * @param fileName The file name without .json
     * @return Found JsonObject or a new {@link JsonObject}
     */
    public static JsonObject of(String directory, String fileName) {
        return new JsonFileLoader(directory, fileName).getObject();
    }

}
