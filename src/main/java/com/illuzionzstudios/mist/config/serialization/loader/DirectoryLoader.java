package com.illuzionzstudios.mist.config.serialization.loader;

import com.illuzionzstudios.mist.Logger;
import com.illuzionzstudios.mist.plugin.SpigotPlugin;
import lombok.Getter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads all files from a directory with a certain file loader
 * @param <T> The type of file to load
 */
public class DirectoryLoader<T extends FileLoader<?>> {

    /**
     * The directory that is being loaded
     */
    @Getter
    public final String directory;

    /**
     * All file loaders for files in directory. From these we can then create the objects
     */
    @Getter
    private final List<T> loaders;

    /**
     * Class of the loader
     */
    private final Class<T> clazz;

    /**
     * @param directory to load from
     * @param clazz The class for the file loader
     */
    public DirectoryLoader(Class<T> clazz, String directory) {
        this.directory = directory;
        loaders = new ArrayList<>();
        this.clazz = clazz;

        load();
    }

    public void load() {
        // Reward directory
        File dir = new File(SpigotPlugin.getInstance().getDataFolder().getPath() + File.separator + directory);

        // If doesn't exist and has to create no point loading
        if (dir.listFiles() == null || !dir.exists()) {
            return;
        }

        // Go through files
        for (File file : dir.listFiles()) {
            // Get name without extension
            String name = file.getName().split("\\.")[0];

            try {
                T loader = clazz.getConstructor(String.class, String.class).newInstance(directory, name);

                // Make sure the file extension matches the loader
                if (file.getName().split("\\.")[1].equalsIgnoreCase(loader.getExtension()))
                    // Add to cache
                    loaders.add(loader);
            } catch (Exception e) {
                Logger.displayError(e, "Could not not load file " + file.getName());
            }

        }
    }

}
