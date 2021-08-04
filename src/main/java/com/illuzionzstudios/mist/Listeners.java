package com.illuzionzstudios.mist;

import com.illuzionzstudios.mist.plugin.SpigotPlugin;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * An object to encapsulate all listeners and allow us to
 * register them
 */
public final class Listeners {

    /**
     * A list of currently registered listeners for this {@link com.illuzionzstudios.mist.plugin.SpigotPlugin}
     * Stored in a hashset so we don't double register a listener
     */
    private final HashSet<Listener> registeredListeners = new HashSet<>();

    /**
     * @param plugin The plugin to register this to
     * @param listener Register a new listener
     */
    public void register(SpigotPlugin plugin, Listener listener) {
        Bukkit.getServer().getPluginManager().registerEvents(listener, plugin);
        registeredListeners.add(listener);
    }

    /**
     * Un register all current listeners
     */
    public void unregister() {
        for (final Listener listener : registeredListeners) {
            HandlerList.unregisterAll(listener);
        }

        registeredListeners.clear();
    }

}
