package com.illuzionzstudios.mist.data.player;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

/*
 * Player data which can be a bukkit listener
 */
public abstract class BukkitPlayerData<BP extends BukkitPlayer> extends AbstractPlayerData<BP> implements Listener {

    private boolean eventsRegistered = false;

    public BukkitPlayerData(BP player) {
        super(player);
    }

    @Override
    public void unregister() {
        super.unregister();

        if (eventsRegistered) {
            HandlerList.unregisterAll(this);
        }
    }

    protected void registerEvents(Plugin plugin) {
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
        eventsRegistered = true;
    }
}
