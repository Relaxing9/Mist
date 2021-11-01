package com.illuzionzstudios.mist.data.player;

import com.illuzionzstudios.mist.Logger;
import com.illuzionzstudios.mist.data.PlayerData;
import com.illuzionzstudios.mist.data.controller.PlayerDataController;
import com.illuzionzstudios.mist.scheduler.MinecraftScheduler;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Player abstraction for data loading/saving
 */
@Getter
public abstract class AbstractPlayer {

    /**
     * Cached name of the player
     */
    private final String name;

    /**
     * Identifier of the player
     */
    private final UUID uuid;

    /**
     * Keys in the data to always be replaced
     */
    private final HashMap<String, String> keyMetadata = new HashMap<>();

    /**
     * Keys in the data that have been modified
     * Used for tracking whether to bother setting data
     */
    private final CopyOnWriteArrayList<String> modifiedKeys = new CopyOnWriteArrayList<>();

    /**
     * Local data stored before being saved
     */
    private final HashMap<String, Object> cachedData = new HashMap<>();

    /**
     * Player data associated with this player
     */
    private final ArrayList<AbstractPlayerData<?>> data = new ArrayList<>();

    /**
     * If the player data has been loaded into the cache
     */
    private final AtomicBoolean loaded = new AtomicBoolean(false);

    public AbstractPlayer(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }

    public UUID getUUID() {
        return uuid;
    }

    /**
     * Get player data from a class
     *
     * @param type The class type
     * @param <T>  Player data type to return
     */
    public <T extends PlayerData<?>> T get(Class<T> type) {
        for (AbstractPlayerData<?> info : data) {
            if (info.getClass() == type || type.isAssignableFrom(info.getClass())) {
                return (T) info;
            }
        }

        return PlayerDataController.INSTANCE.getDefaultData(type);
    }

    /**
     * Set that a key has been modified in data
     *
     * @param key Key to set modified
     */
    public void modifyKey(String key) {
        if (!modifiedKeys.contains(key)) {
            modifiedKeys.add(key);
        }
    }

    /**
     * Reset any keys we modified
     */
    public void resetModifiedKeys() {
        modifiedKeys.clear();
    }

    /**
     * Called when loading into server
     */
    public void load() {
        // Shouldn't try to load twice
        if (loaded.get()) return;

        // Loading stored data into cache
        // Simply insert into cached data
        MinecraftScheduler.get().desynchronize(() -> {
            // Async fetch data
            PlayerDataController.get().getDatabase().getFields(this).forEach(this.cachedData::put);

            // If stored data is empty, try upload cached data first
            if (PlayerDataController.get().getDatabase().getFields(this).isEmpty()) {
                upload();
            }
        }, 0);

        this.loaded.set(true);
        PlayerDataController.get().applyDefaultData(this);
    }

    /**
     * Shorthand for just saving
     */
    public void save() {
        this.save(null);
    }

    /**
     * Save cached data to the database
     * <p>
     * CAN BE PERFORMED ON MAIN THREAD
     *
     * @param doAfter Perform an action afterwards
     */
    public void save(Consumer<Boolean> doAfter) {
        prepareSaveData();

        // Saving data async
        MinecraftScheduler.get().desynchronize(this::upload, consumer -> {
            try {
                boolean insert = consumer.get();
                if (doAfter != null) {
                    doAfter.accept(insert);
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Quick save
     */
    public void unsafeSave() {
        prepareSaveData();
        upload();
    }

    /**
     * BE VERY CAREFUL USING THIS
     * <p>
     * This will wipe all data for this current user
     */
    public void clearAllData() {
        // Clear loaded/cached data
        this.cachedData.forEach((key, data) -> PlayerDataController.get().getDatabase().setFieldValue(this, key, null));

        // Now clear cached data as not to save it
        this.cachedData.clear();

        // Don't save any data trying to be saved
        this.modifiedKeys.clear();
    }

    /**
     * Upload cached data into database
     * <p>
     * NEVER SERVER THREAD SAFE
     */
    public boolean upload() {
        // Upload modified data
        for (String key : this.modifiedKeys) {
            Object value = this.cachedData.getOrDefault(key, null);

            // Don't save if nothing to save
            if (value == null) continue;

            // Set the field in the database
            PlayerDataController.get().getDatabase().setFieldValue(this, key, value);
        }

        resetModifiedKeys();

        return true;
    }

    /**
     * Get data ready to save
     */
    public void prepareSaveData() {
        data.forEach(data -> {
            try {
                data.onSave();
            } catch (Exception e) {
                Logger.severe("Error occurred while preparing save data");
                e.printStackTrace();
            }
        });
    }

}
