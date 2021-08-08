package com.illuzionzstudios.mist.util;

import com.google.common.io.Resources;
import com.google.common.net.HttpHeaders;
import com.illuzionzstudios.mist.config.locale.MistString;
import com.illuzionzstudios.mist.config.locale.PluginLocale;
import com.illuzionzstudios.mist.config.locale.Message;
import com.illuzionzstudios.mist.plugin.SpigotPlugin;
import com.illuzionzstudios.mist.scheduler.MinecraftScheduler;
import org.bukkit.command.CommandSender;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.function.BiConsumer;

/**
 * Util class to check for updates for this plugin
 */
public final class UpdateChecker {

    private static final String SPIGOT_URL = "https://api.spigotmc.org/legacy/update.php?resource=%d";

    /**
     * Check for latest version
     *
     * @param callback Callback to call
     */
    public static void check(BiConsumer<VersionType, String> callback) {
        MinecraftScheduler.get().desynchronize(() -> {
            try {
                int resourceId = SpigotPlugin.getInstance().getPluginId();

                HttpURLConnection httpURLConnection = (HttpsURLConnection) new URL(String.format(SPIGOT_URL, resourceId)).openConnection();
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.setRequestProperty(HttpHeaders.USER_AGENT, "Mozilla/5.0");

                String currentVersion = SpigotPlugin.getPluginVersion();
                String fetchedVersion = Resources.toString(httpURLConnection.getURL(), Charset.defaultCharset());

                boolean devVersion = currentVersion.contains("DEV");
                boolean latestVersion = fetchedVersion.equalsIgnoreCase(currentVersion);
                MinecraftScheduler.get().synchronize(() -> callback.accept(latestVersion ? VersionType.LATEST : devVersion ? VersionType.EXPERIMENTAL : VersionType.OUTDATED, latestVersion ? currentVersion : fetchedVersion));
            } catch (IOException exception) {
                MinecraftScheduler.get().synchronize(() -> callback.accept(VersionType.UNKNOWN, null));
            }
        });
    }

    /**
     * Check with a sender for a new version and notify of anything
     *
     * @param sender The sender to check
     */
    public static void checkVersion(CommandSender sender) {
        // Only notify ops
        if (!sender.isOp())
            return;

        check((version, name) -> {
            if (version != VersionType.LATEST)
            PluginLocale.UPDATE_AVAILABLE
                    .toString("plugin_name", SpigotPlugin.getPluginName())
                    .toString("current", SpigotPlugin.getPluginVersion())
                    .toString("new", name)
                    .toString("status", version.name().toLowerCase())
                    .toString("resource_id", SpigotPlugin.getInstance().getPluginId())
                    .sendMessage(sender);
        });
    }

    /**
     * Type of version found
     */
    public enum VersionType {

        /**
         * Version couldn't be found
         */
        UNKNOWN,

        /**
         * The version is old and a new is available
         */
        OUTDATED,

        /**
         * The current version is the latest
         */
        LATEST,

        /**
         * Running a development build
         */
        EXPERIMENTAL
    }
}
