package com.illuzionzstudios.mist.compatibility.util;

import com.illuzionzstudios.mist.Logger;
import com.illuzionzstudios.mist.util.ReflectionUtil;
import com.illuzionzstudios.mist.util.Valid;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * Main util class for cross-version compatibility.
 *
 * Contains a lot of static methods in order to make our plugin
 * work from {@version 1.8.8} to the latest version
 */
@UtilityClass
public final class VersionUtil {

    /**
     * Injects an existing command into the command map
     *
     * @param command The {@link Command} instance to register
     */
    public void registerCommand(final Command command) {
        final CommandMap commandMap = getCommandMap();

        if (commandMap == null) return;

        commandMap.register(command.getLabel(), command);

        Valid.checkBoolean(command.isRegistered(), "Command /" + command.getLabel() + " could not have been registered properly!");
    }

    /**
     * Removes a command by its label from command map, includes all aliases
     *
     * @param label the label
     */
    public void unregisterCommand(final String label) {
        unregisterCommand(label, true);
    }

    /**
     * Removes a command by its label from command map, optionally can also remove
     * aliases
     *
     * @param label          the label
     * @param removeAliases, also remove aliases?
     */
    public void unregisterCommand(final String label, final boolean removeAliases) {
        try {
            // Unregister the commandMap from the command itself.
            final PluginCommand command = Bukkit.getPluginCommand(label);

            if (command != null) {
                final Field commandField = Command.class.getDeclaredField("commandMap");
                commandField.setAccessible(true);

                if (command.isRegistered())
                    command.unregister((CommandMap) commandField.get(command));
            }

            // Delete command + aliases from server's command map.
            final Field f = SimpleCommandMap.class.getDeclaredField("knownCommands");
            f.setAccessible(true);

            final Map<String, Command> cmdMap = (Map<String, Command>) f.get(VersionUtil.getCommandMap());

            cmdMap.remove(label);

            if (command != null && removeAliases)
                for (final String alias : command.getAliases())
                    cmdMap.remove(alias);

        } catch (final ReflectiveOperationException ex) {
            Logger.displayError(ex, "Failed to unregister command /" + label);
        }
    }

    /**
     * @return Server's command map
     */
    private SimpleCommandMap getCommandMap() {
        try {
            return (SimpleCommandMap) ReflectionUtil.getOBCClass("CraftServer").getDeclaredMethod("getCommandMap").invoke(Bukkit.getServer());
        } catch (final ReflectiveOperationException ex) {
            Logger.displayError(ex, "Couldn't load server command map");
        }

        return null;
    }

}
