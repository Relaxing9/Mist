package com.illuzionzstudios.mist.plugin;

import com.illuzionzstudios.mist.command.SpigotCommand;
import com.illuzionzstudios.mist.command.SpigotCommandGroup;
import com.illuzionzstudios.mist.controller.PluginController;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.HashSet;

/**
 * A wrapper for objects that can be safely reloaded for the plugin. This
 * allows us to call a universal reload method for our controllers etc.
 */
@RequiredArgsConstructor
public final class Reloadables {

    private final SpigotPlugin plugin;

    /**
     * A list of currently registered listeners for this {@link com.illuzionzstudios.mist.plugin.SpigotPlugin}
     * Stored in a hashset so we don't double register a listener
     */
    private final HashSet<Listener> listeners = new HashSet<>();

    /**
     * A list of registered command groups
     */
    private final HashMap<SpigotCommandGroup, String[]> commandGroups = new HashMap<>();

    /**
     * Base plugin commands
     */
    private final HashSet<SpigotCommand> commands = new HashSet<>();

    /**
     * Plugin controllers
     */
    private final HashSet<PluginController> controllers = new HashSet<>();

    /**
     * Startup all our reloadables
     */
    public void start() {
        listeners.forEach(listener -> {
            Bukkit.getServer().getPluginManager().registerEvents(listener, SpigotPlugin.getInstance());
        });

        commandGroups.forEach(SpigotCommandGroup::register);

        commands.forEach(SpigotCommand::register);

        controllers.forEach(controller -> {
            Bukkit.getServer().getPluginManager().registerEvents(controller, SpigotPlugin.getInstance());
            controller.initialize(plugin);
        });
    }

    /**
     * Shutdown all reloadable tasks
     */
    public void shutdown() {
        for (final Listener listener : listeners)
            HandlerList.unregisterAll(listener);

        listeners.clear();

        for (final SpigotCommandGroup commandGroup : commandGroups.keySet())
            commandGroup.unregister();

        commandGroups.clear();
    }

    /**
     * Register events to Bukkit
     */
    public void registerEvent(final Listener listener) {
        listeners.add(listener);
    }

    /**
     * Register the given command group
     */
    public void registerCommand(final SpigotCommandGroup group, final String... labels) {
        commandGroups.put(group, labels);
    }

    /**
     * Register a base spigot command
     */
    public void registerCommand(final SpigotCommand command) {
        commands.add(command);
    }

    /**
     * Register a controller for startup
     */
    public void registerController(final PluginController controller) {
        controllers.add(controller);
    }

}
