package com.illuzionzstudios.mist.compatibility;

import com.illuzionzstudios.mist.Logger;
import com.illuzionzstudios.mist.exception.PluginException;
import com.illuzionzstudios.mist.util.Valid;
import lombok.Getter;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Bukkit;

/**
 * The major server version the current server software is
 * eg, 1.15, 1.8
 */
/**
 * Represents the current Minecraft version the plugin loaded on
 */
public final class ServerVersion {

    /**
     * The string representation of the version, for example V1_13
     */
    private static String serverVersion;

    /**
     * The wrapper representation of the version
     */
    @Getter
    private static V current;

    /**
     * The version wrapper
     */
    public enum V {
        v1_16(16),
        v1_15(15),
        v1_14(14),
        v1_13(13),
        v1_12(12),
        v1_11(11),
        v1_10(10),
        v1_9(9),
        v1_8(8),
        v1_7(7, false),
        v1_6(6, false),
        v1_5(5, false),
        v1_4(4, false),
        v1_3_AND_BELOW(3, false);

        /**
         * The numeric version (the second part of the 1.x number)
         */
        private final int ver;

        /**
         * Is this library tested with this Minecraft version?
         */
        @Getter
        private final boolean tested;

        /**
         * Creates new enum for a MC version that is tested
         */
        V(int version) {
            this(version, true);
        }

        /**
         * Creates new enum for a MC version
         */
        V(int version, boolean tested) {
            this.ver = version;
            this.tested = tested;
        }

        /**
         * Attempts to get the version from number
         *
         * @throws RuntimeException if number not found
         */
        protected static V parse(int number) {
            for (final V v : values())
                if (v.ver == number)
                    return v;

            throw new PluginException("Invalid version number: " + number);
        }
    }

    /**
     * Does the current Minecraft version equal the given version?
     */
    public static boolean equals(V version) {
        return compareWith(version) == 0;
    }

    /**
     * Is the current Minecraft version older than the given version?
     */
    public static boolean olderThan(V version) {
        return compareWith(version) < 0;
    }

    /**
     * Is the current Minecraft version newer than the given version?
     */
    public static boolean newerThan(V version) {
        return compareWith(version) > 0;
    }

    /**
     * Is the current Minecraft version at equals or newer than the given version?
     */
    public static boolean atLeast(V version) {
        return equals(version) || newerThan(version);
    }

    // Compares two versions by the number
    private static int compareWith(V version) {
        try {
            return getCurrent().ver - version.ver;

        } catch (final Throwable t) {
            t.printStackTrace();

            return 0;
        }
    }

    /**
     * Return the class versioning such as v1_14_R1
     */
    public static String getServerVersion() {
        return serverVersion.equals("craftbukkit") ? "" : serverVersion;
    }

    // Initialize the version
    static {
        try {

            final String packageName = Bukkit.getServer() == null ? "" : Bukkit.getServer().getClass().getPackage().getName();
            final String curr = packageName.substring(packageName.lastIndexOf('.') + 1);
            final boolean hasGatekeeper = !"craftbukkit".equals(curr);

            serverVersion = curr;

            if (hasGatekeeper) {
                int pos = 0;

                for (final char ch : curr.toCharArray()) {
                    pos++;

                    if (pos > 2 && ch == 'R')
                        break;
                }

                final String numericVersion = curr.substring(1, pos - 2).replace("_", ".");

                int found = 0;

                for (final char ch : numericVersion.toCharArray())
                    if (ch == '.')
                        found++;

                Valid.checkBoolean(found == 1, "Minecraft Version checker malfunction. Could not detect your server version. Detected: " + numericVersion + " Current: " + curr);

                current = V.parse(Integer.parseInt(numericVersion.split("\\.")[1]));
            } else current = V.v1_3_AND_BELOW;

        } catch (final Throwable t) {
            Logger.displayError(t, "Error detecting your Minecraft version. Check your server compatibility.");
        }
    }
}