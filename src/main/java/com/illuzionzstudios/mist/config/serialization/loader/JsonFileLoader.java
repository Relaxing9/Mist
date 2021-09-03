package com.illuzionzstudios.mist.config.serialization.loader;

import com.google.gson.*;
import com.illuzionzstudios.mist.Logger;

import java.io.*;

/**
 * Provides a way to load on object from a JSON file.
 *
 * @param <T> The object to load
 */
public abstract class JsonFileLoader<T> extends FileLoader<T> {

    /**
     * The JSON object to use for loading
     */
    protected JsonObject json;

    /**
     * @param directory The directory from plugin folder
     * @param fileName The file name without .json
     */
    public JsonFileLoader(String directory, String fileName) {
        super(directory, fileName, "json");
    }

    /**
     * Save the object T into a json object to be saved to disk
     */
    public abstract void saveJson();

    /**
     * Used to save our {@link JsonObject} to disk
     * at {@link #file}
     *
     * @return If was saved successfully
     */
    @Override
    public boolean save() {
        // Load new object before saving
        saveJson();

        try {
            FileWriter writer = new FileWriter(file);

            Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
            JsonParser jp = new JsonParser();
            JsonElement je = jp.parse(json.toString());
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
    public T loadObject(final File file) {
        // Try assign JSON file
        try {
            json = new JsonParser().parse(new FileReader(file)).getAsJsonObject();
        } catch (FileNotFoundException e) {
            // If couldn't load, it becomes a new object
            json = new JsonObject();
        }

        return loadJsonObject();
    }

    /**
     * Load the object from a {@link JsonObject}
     *
     * @return The loaded object
     */
    public abstract T loadJsonObject();

}
