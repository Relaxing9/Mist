package com.illuzionzstudios.mist.util;

import com.illuzionzstudios.mist.plugin.SpigotPlugin;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.experimental.UtilityClass;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Utils to help with files
 */
@UtilityClass
public class FileUtil {

    /**
     * Return an internal resource within our plugin's jar file
     *
     * @return the resource input stream, or null if not found
     */
    public InputStream getInternalResource(String path) {
        // First attempt
        InputStream is = SpigotPlugin.getInstance().getClass().getResourceAsStream(path);

        // Try using Bukkit
        if (is == null)
            is = SpigotPlugin.getInstance().getResource(path);

        // The hard way - go in the jar file
        if (is == null)
            try (JarFile jarFile = new JarFile(SpigotPlugin.getSource())) {
                final JarEntry jarEntry = jarFile.getJarEntry(path);

                if (jarEntry != null)
                    is = jarFile.getInputStream(jarEntry);

            } catch (final IOException ex) {
                ex.printStackTrace();
            }

        return is;
    }

    /**
     * Recursively delete a directory and all contents
     *
     * @param dir File path to directory
     */
    public void purgeDirectory(File dir) {
        Valid.checkNotNull(dir, "Cannot purge a null directory");
        // If not a directory just delete the file
        if (!dir.isDirectory()) dir.delete();

        for (File file : dir.listFiles()) {
            if (file.isDirectory())
                purgeDirectory(file);
            file.delete();
        }
    }

}
