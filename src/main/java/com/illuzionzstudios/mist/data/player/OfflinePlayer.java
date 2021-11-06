package com.illuzionzstudios.mist.data.player;

import com.illuzionzstudios.mist.data.PlayerData;
import com.illuzionzstudios.mist.data.controller.PlayerDataController;

import java.util.UUID;

/**
 * A player that we don't know if is offline or online
 */
public class OfflinePlayer extends AbstractPlayer {

    public OfflinePlayer(UUID uuid, String name) {
        super(uuid, name);
    }

    /*
     * Automatically creates a new abstract player data object
     * if not already applied by default.
     */
    @Override
    public <T extends PlayerData<?>> T get(Class<T> type) {
        T data = super.get(type);

        if (data != null) {
            return data;
        }

        data = PlayerDataController.INSTANCE.getDefaultData(this, type);
        return data;
    }

}
