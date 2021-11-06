package com.illuzionzstudios.mist.data.player;

import com.illuzionzstudios.mist.data.PlayerData;
import com.illuzionzstudios.mist.scheduler.MinecraftScheduler;
import lombok.Getter;

import java.util.HashMap;

/**
 * Registered player data
 */
public abstract class AbstractPlayerData<P extends AbstractPlayer> implements PlayerData<P> {

    /**
     * Keys to replace when querying
     */
    public HashMap<String, String> localKeys = new HashMap<>();
    /**
     * The player that owns this data
     */
    @Getter
    protected P player;
    /**
     * If the scheduler is registered
     */
    private boolean schedulerRegistered = false;

    public AbstractPlayerData(P player) {
        this.player = player;
    }

    /**
     * Run when attempting to save data
     */
    public void onSave() {
    }

    public void unregister() {
        if (schedulerRegistered) {
            MinecraftScheduler.get().dismissSynchronizationService(this);
        }
    }

    protected void registerScheduler() {
        MinecraftScheduler.get().registerSynchronizationService(this);
        schedulerRegistered = true;
    }

}
