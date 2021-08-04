package com.illuzionzstudios.mist.data.controller;

import com.illuzionzstudios.mist.data.player.AbstractPlayer;
import com.illuzionzstudios.mist.data.player.AbstractPlayerData;
import com.illuzionzstudios.mist.data.player.OfflinePlayer;
import com.illuzionzstudios.mist.scheduler.MinecraftScheduler;
import com.illuzionzstudios.mist.scheduler.rate.Rate;
import com.illuzionzstudios.mist.scheduler.rate.Sync;
import lombok.Getter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Handles all players on the server
 */
public abstract class AbstractPlayerController<P extends AbstractPlayer> {

    /**
     * Cache of loaded players
     */
    @Getter
    protected List<P> players = new ArrayList<>();

    /**
     * Cache of offline players
     */
    @Getter
    protected Map<UUID, OfflinePlayer> offlineCache = new ConcurrentHashMap<>();

    /**
     * Perform an action on all players
     *
     * @param action Consumer to apply
     */
    public void each(Consumer<? super P> action) {
        players.forEach(action);
    }

    /**
     * Try find a player from custom filter
     *
     * @param context Predicate to use
     */
    public Optional<P> getPlayer(Predicate<? super P> context) {
        return players.stream().filter(context).findAny();
    }

    /**
     * Try find player from UUID
     *
     * @param uuid The UUID of player
     */
    public P getPlayer(UUID uuid) {
        return getPlayer(player -> player.getUUID().equals(uuid)).orElse(null);
    }

    /**
     * Create a new instance of a player
     *
     * @param uuid Player's UUID
     * @param name Player's Name
     */
    protected abstract P newInstance(UUID uuid, String name);

    /**
     * Autosave all player data
     */
    @Sync(rate = Rate.MIN_04)
    public void autosave() {
        for (P player : players) {
            player.save();
        }
    }

    /**
     * Login a player to the server and prepare data
     *
     * @param uuid UUID of player
     * @param name Name of player
     * @return The loaded player object
     */
    protected P handleLogin(UUID uuid, String name) {
//        Logger.info("Loading %s's player data from database onto server.", name);

        // Load a player's data if set from offline player
        if (offlineCache.containsKey(uuid)) {
            OfflinePlayer player = offlineCache.get(uuid);
            player.unsafeSave();
            try {
                for (AbstractPlayerData<?> info : player.getData()) {
                    info.unregister();
                }
            } finally {
                MinecraftScheduler.get().dismissSynchronizationService(player);
                offlineCache.remove(player.getUUID());
            }
        }

        P player = newInstance(uuid, name);
        player.load();

        players.add(player);
        return player;
    }

    /**
     * Get an offline player from UUID and name
     *
     * @param uuid UUID of player
     * @param name Name of player
     * @return New OfflinePlayer AbstractPlayer
     */
    public OfflinePlayer getOfflinePlayer(UUID uuid, String name) {
        if (this.offlineCache.containsKey(uuid)) {
            return this.offlineCache.get(uuid);
        }

        OfflinePlayer player = new OfflinePlayer(uuid, name);

        player.load();
        this.offlineCache.put(uuid, player);
        return player;
    }

    /**
     * Remove an offline player
     *
     * @param uuid UUID of player
     */
    public void removePlayer(UUID uuid) {
        this.offlineCache.remove(uuid);
    }

    /**
     * Log out the player and save all data
     *
     * @param player The player to logout
     */
    protected void handleLogout(P player) {
        this.unregister(player);
        player.save();
    }

    /**
     * Unregister the player and get ready for logout
     *
     * @param player AbstractPlayer instance
     */
    public void unregister(P player) {
        try {
            for (AbstractPlayerData<?> info : player.getData()) {
                info.unregister();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            MinecraftScheduler.get().dismissSynchronizationService(player);
            players.remove(player);
        }
    }

}
