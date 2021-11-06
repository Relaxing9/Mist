package com.illuzionzstudios.mist.data.controller;

import com.illuzionzstudios.mist.data.player.GamePlayer;
import com.illuzionzstudios.mist.plugin.SpigotPlugin;

import java.util.UUID;

/**
 * Default game player controllers
 */
public class GamePlayerController extends BukkitPlayerController<GamePlayer> {

    public static GamePlayerController INSTANCE;

    @Override
    protected GamePlayer newInstance(UUID uuid, String s) {
        return new GamePlayer(uuid, s);
    }

    @Override
    public void initialize(SpigotPlugin plugin) {
        super.initialize(plugin);
        INSTANCE = this;
    }

}
