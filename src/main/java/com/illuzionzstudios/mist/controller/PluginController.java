package com.illuzionzstudios.mist.controller;

import com.illuzionzstudios.mist.plugin.SpigotPlugin;
import org.bukkit.plugin.Plugin;

/**
 * A tag that indicates a controller. A controller is class that handles things
 * of a certain type. Has a start and stop method
 *
 * These can usually be an {@link Enum} object that has a single
 * member, INSTANCE. That way you can simply call Controller.INSTANCE.<method>
 *
 * @param <P> The instance of the plugin this controller is for
 */
public interface PluginController<P extends SpigotPlugin> {

    /**
     * Starts up our controller
     *
     * @param plugin The plugin starting the controller
     */
    void initialize(P plugin);

    /**
     * Stops our controller
     *
     * @param plugin The plugin stopping the controller
     */
    void stop(P plugin);

}
