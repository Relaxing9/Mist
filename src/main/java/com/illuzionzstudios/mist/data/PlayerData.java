package com.illuzionzstudios.mist.data;

import com.illuzionzstudios.mist.data.player.AbstractPlayer;

/**
 * Basis of player data
 */
public interface PlayerData<P extends AbstractPlayer> {

    /**
     * Gets the player associated with the data
     */
    P getPlayer();

}
