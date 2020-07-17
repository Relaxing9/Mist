package com.illuzionzstudios.mist.data.database;

/**
 * Copyright © 2020 Property of Illuzionz Studios, LLC
 * All rights reserved. No part of this publication may be reproduced, distributed, or
 * transmitted in any form or by any means, including photocopying, recording, or other
 * electronic or mechanical methods, without the prior written permission of the publisher,
 * except in the case of brief quotations embodied in critical reviews and certain other
 * noncommercial uses permitted by copyright law. Any licensing of this software overrides
 * this statement.
 */

import com.illuzionzstudios.mist.config.YamlConfig;
import com.illuzionzstudios.mist.data.controller.BukkitPlayerController;
import com.illuzionzstudios.mist.data.player.AbstractPlayer;
import com.illuzionzstudios.mist.data.player.OfflinePlayer;
import com.illuzionzstudios.mist.plugin.SpigotPlugin;
import com.illuzionzstudios.mist.util.UUIDFetcher;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Database stored in flat files
 */
public class YamlDatabase implements Database {

    @Override
    public HashMap<String, Object> getFields(AbstractPlayer player) {
        // Local data file
        YamlConfig dataConfig = new YamlConfig(SpigotPlugin.getInstance(), "/data", player.getUUID() + ".yml");
        dataConfig.load();

        HashMap<String, Object> cache = new HashMap<>();

        // Get keys and load if found value
        dataConfig.getDefaultSection().getKeys(true).forEach(path -> {
            // Check if not null value
            if (dataConfig.get(path) != null) {
                // Add to cache
                cache.put(path, dataConfig.get(path));
            }
        });

        return cache;
    }

    @Override
    public Object getFieldValue(AbstractPlayer player, String queryingField) {
        // Local data file
        YamlConfig dataConfig = new YamlConfig(SpigotPlugin.getInstance(), "/data", player.getUUID() + ".yml");

        dataConfig.load();
        return dataConfig.get(queryingField);
    }

    @Override
    public void setFieldValue(AbstractPlayer player, String queryingField, Object value) {
        // Local data file
        YamlConfig dataConfig = new YamlConfig(SpigotPlugin.getInstance(), "/data", player.getUUID() + ".yml");

        dataConfig.load();
        dataConfig.set(queryingField, value);
        dataConfig.saveChanges();
    }

    @Override
    public List<OfflinePlayer> getSavedPlayers() {
        List<OfflinePlayer> savedPlayers = new ArrayList<>();
        File dir = new File(SpigotPlugin.getInstance().getDataFolder().getPath() + File.separator + "data");
        File[] files = dir.listFiles();

        // Can't find players if can't find directory
        if (files == null) return savedPlayers;

        // Go through files
        for (File file : files) {
            // Get name without extension
            String uuid = file.getName().split("\\.")[0];

            String name = UUIDFetcher.getName(UUID.fromString(uuid));

            // Get offline player
            OfflinePlayer player = BukkitPlayerController.INSTANCE.getOfflinePlayer(UUID.fromString(uuid), name);

            // Add to cache
            savedPlayers.add(player);
        }

        return savedPlayers;
    }

    @Override
    public boolean connect() {
        return false;
    }

    @Override
    public boolean disconnect() {
        return false;
    }

    @Override
    public boolean isAlive() {
        return true;
    }
}
