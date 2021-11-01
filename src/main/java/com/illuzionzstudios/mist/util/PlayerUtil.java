package com.illuzionzstudios.mist.util;

import com.illuzionzstudios.mist.plugin.SpigotPlugin;
import lombok.NonNull;
import org.bukkit.permissions.Permissible;
import org.jetbrains.annotations.Nullable;

/**
 * Utility method for dealing with players
 */
public final class PlayerUtil {

    /**
     * Return if the given sender has a certain permission
     * You can use {plugin.name} to replace with your plugin name (lower-cased)
     *
     * @param sender     The {@link Permissible} object to check permissions
     * @param permission The unformatted permission to check
     * @return If the {@link Permissible} had that permission set to true
     */
    public static boolean hasPerm(@NonNull final Permissible sender, @Nullable final String permission) {
        return permission == null || sender.hasPermission(permission.replace("{plugin.name}", SpigotPlugin.getPluginName().toLowerCase()));
    }

}
