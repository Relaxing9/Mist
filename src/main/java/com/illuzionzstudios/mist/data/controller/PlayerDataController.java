package com.illuzionzstudios.mist.data.controller;

/**
 * Copyright © 2020 Property of Illuzionz Studios, LLC
 * All rights reserved. No part of this publication may be reproduced, distributed, or
 * transmitted in any form or by any means, including photocopying, recording, or other
 * electronic or mechanical methods, without the prior written permission of the publisher,
 * except in the case of brief quotations embodied in critical reviews and certain other
 * noncommercial uses permitted by copyright law. Any licensing of this software overrides
 * this statement.
 */

import com.illuzionzstudios.mist.Logger;
import com.illuzionzstudios.mist.data.PlayerData;
import com.illuzionzstudios.mist.data.database.Database;
import com.illuzionzstudios.mist.data.player.AbstractPlayer;
import com.illuzionzstudios.mist.data.player.AbstractPlayerData;
import lombok.Getter;
import org.apache.commons.lang.reflect.ConstructorUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

/**
 * Master class for handling all data between players
 */
public class PlayerDataController<P extends AbstractPlayer, PD extends AbstractPlayerData<?>> {

    /**
     * Instance of the data controller
     */
    public static PlayerDataController<?, ?> INSTANCE;

    /**
     * The database to use for player data
     */
    @Getter
    private Database database;

    /**
     * Registered default data
     */
    private ArrayList<Class<? extends AbstractPlayerData<?>>> defaultData = new ArrayList<>();

    /**
     * The player class we use for our operations
     */
    private Class<? extends AbstractPlayer> playerClass;

    /**
     * Get an instance of our controller
     */
    public static <P extends AbstractPlayer, PD extends AbstractPlayerData<P>> PlayerDataController<P, PD> get() {
        return (PlayerDataController<P, PD>) INSTANCE;
    }

    /**
     * Register default player data
     *
     * @param data The data class to register
     */
    public void registerDefaultData(Class<? extends AbstractPlayerData<?>> data) {
        defaultData.add(data);
    }

    /**
     * Let our data controller be used
     *
     * @param playerClass The player class registered for data
     * @param database The database to use for data
     */
    public void initialize(Class<? extends AbstractPlayer> playerClass, Database database) {
        INSTANCE = this;
        this.playerClass = playerClass;
        this.database = database;

        // Connect now
        if (this.database.connect()) {
            Logger.info("Connected to database successfully");
        }
    }

    /**
     * Try and find default data for a player
     *
     * @param player The player to find
     * @param clazz The class for the data
     * @param <T> Return the player data
     */
    public <T extends PlayerData<?>> T getDefaultData(AbstractPlayer player, Class<T> clazz) {
        try {
            T data = (T) ConstructorUtils.getMatchingAccessibleConstructor(clazz, new Class[]{AbstractPlayer.class}).newInstance(player);
            player.getData().add((AbstractPlayerData<?>) data);
            return data;
        } catch (Exception e) {
        }
        return null;
    }

    /**
     * Apply default data to a player
     *
     * @param player The player to set default data for
     */
    public void applyDefaultData(AbstractPlayer player) {
        addInfo:
        for (Class<?> pi : defaultData) {
            try {
                for (AbstractPlayerData<?> data : player.getData()) {
                    if (data.getClass().isAssignableFrom(pi)) {
                        continue addInfo;
                    }
                }

                Constructor<?> constructor = ConstructorUtils.getMatchingAccessibleConstructor(pi, new Class[]{playerClass});

                if (constructor == null) {
                    continue;
                }

                player.getData().add((PD) constructor.newInstance(player));
            } catch (Exception e) {
                if (!(e instanceof IllegalArgumentException || e instanceof IllegalAccessException || e instanceof InstantiationException || e instanceof InvocationTargetException)) {
                }
            }
        }
    }

    /**
     * Get the default data of a data class
     *
     * @param type The class of the player data
     * @param <T> Return a player data type
     */
    public <T extends PlayerData<?>> T getDefaultData(Class<T> type) {
        for (Class<?> pi : defaultData) {
            if (pi == type) {
                try {
                    return (T) pi.getDeclaredConstructor().newInstance();
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException ignored) {
                }
            }
        }
        return null;
    }

}
