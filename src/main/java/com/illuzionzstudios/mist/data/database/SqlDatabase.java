/**
 * Copyright © 2020 Property of Illuzionz Studios, LLC
 * All rights reserved. No part of this publication may be reproduced, distributed, or
 * transmitted in any form or by any means, including photocopying, recording, or other
 * electronic or mechanical methods, without the prior written permission of the publisher,
 * except in the case of brief quotations embodied in critical reviews and certain other
 * noncommercial uses permitted by copyright law. Any licensing of this software overrides
 * this statement.
 */
package com.illuzionzstudios.mist.data.database;

import com.illuzionzstudios.mist.Logger;
import com.illuzionzstudios.mist.data.controller.BukkitPlayerController;
import com.illuzionzstudios.mist.data.player.AbstractPlayer;
import com.illuzionzstudios.mist.data.player.OfflinePlayer;
import com.illuzionzstudios.mist.plugin.SpigotPlugin;
import com.illuzionzstudios.mist.scheduler.MinecraftScheduler;
import com.illuzionzstudios.mist.util.UUIDFetcher;
import lombok.RequiredArgsConstructor;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Use MySql or any Sql database for our data
 */
@RequiredArgsConstructor
public class SqlDatabase implements Database {

    /**
     * Host server of the database
     */
    private final String host;
    /**
     * Port to connect to
     */
    private final int port;
    /**
     * Database to use
     */
    private final String database;
    /**
     * Connection username
     */
    private final String username;
    /**
     * Connection password
     */
    private final String password;
    /**
     * Table to store our player data
     */
    private final String tableName = SpigotPlugin.getPluginName() + "Data";
    /**
     * Our connection to handle SQL operations
     */
    protected Connection connection;

    @Override
    public HashMap<String, Object> getFields(AbstractPlayer player) {
        HashMap<String, Object> data = new HashMap<>();

        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + tableName + " WHERE uuid = ?");

            statement.setString(1, player.getUUID().toString());
            ResultSet set = statement.executeQuery();
            ResultSetMetaData meta = set.getMetaData();
            set.next();

            int columnIndex = 0;
            while (true) {
                try {
                    columnIndex++;
                    // Get field value
                    String queryingField = meta.getColumnName(columnIndex);
                    Logger.debug("Getting all fields: " + queryingField);
                    data.put(queryingField, set.getObject(queryingField));
                } catch (Exception ex) {
                    break;
                }
            }
        } catch (Exception ex) {
            Logger.displayError(ex, "Error preforming SQL operation");
        }

        return data;
    }

    @Override
    public Object getFieldValue(AbstractPlayer player, String queryingField) {
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT ? FROM " + tableName + " WHERE uuid = ?");

            statement.setString(1, queryingField);
            statement.setString(2, player.getUUID().toString());
            ResultSet set = statement.executeQuery();

            set.next();
            return set.getObject(queryingField);
        } catch (Exception ex) {
            Logger.displayError(ex, "Error preforming SQL operation");
        }
        return null;
    }

    @Override
    public void setFieldValue(AbstractPlayer player, String queryingField, Object value) {
        try {
            // Try create column if doesn't exist
            PreparedStatement createColumn = connection.prepareStatement("IF COL_LENGTH(?, ?) IS NULL" +
                    " BEGIN" +
                    " ALTER TABLE " + tableName +
                    " ADD ? varchar(255)" +
                    " END");
            createColumn.setString(1, tableName);
            createColumn.setString(2, queryingField);
            createColumn.setString(3, queryingField);

            // Attempt to set value
            PreparedStatement statement = connection.prepareStatement("IF EXISTS (SELECT * FROM " + tableName + " WHERE uuid = ?)" +
                    " BEGIN" +
                    " UPDATE " + tableName + " SET ? = ? WHERE uuid = ?" +
                    " END" +
                    " ELSE" +
                    " BEGIN" +
                    " INSERT INTO " + tableName + " (uuid, ?) VALUES (?, ?)" +
                    " END");

            statement.setString(1, player.getUUID().toString());
            statement.setString(2, queryingField);
            statement.setObject(3, value);
            statement.setString(4, player.getUUID().toString());
            statement.setString(5, queryingField);
            statement.setString(6, player.getUUID().toString());
            statement.setObject(7, value);
            statement.executeUpdate();
        } catch (Exception ex) {
            Logger.displayError(ex, "Error preforming SQL operation");
        }
    }

    @Override
    public List<OfflinePlayer> getSavedPlayers() {
        try {
            List<OfflinePlayer> players = new ArrayList<>();
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + tableName);
            ResultSet set = statement.executeQuery();

            // Go through and construct from ids
            while (set.next()) {
                String uuidString = set.getString("uuid");
                OfflinePlayer player = BukkitPlayerController.INSTANCE.getOfflinePlayer(UUID.fromString(uuidString), UUIDFetcher.getName(UUID.fromString(uuidString)));
                players.add(player);
            }

            return players;
        } catch (Exception ex) {
            Logger.displayError(ex, "Error preforming SQL operation");
        }

        return null;
    }

    @Override
    public boolean connect() {
        AtomicBoolean status = new AtomicBoolean(false);

        // Connect async
        MinecraftScheduler.get().desynchronize(() -> {
            try {
                // Don't connect if already connected
                if (connection != null && isAlive()) {
                    status.set(false);
                }

                synchronized (this) {
                    if (connection != null && isAlive()) {
                        status.set(false);
                    }
                    Class.forName("com.mysql.jdbc.Driver");
                    connection = DriverManager.getConnection("jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database, this.username, this.password
                    + "?useSSL=false");
                    status.set(true);
                }
            } catch (Exception ex) {
                Logger.displayError(ex, "Couldn't connect to database");
                status.set(false);
            }

            if (isAlive()) {
                Logger.debug("Creating Table");
                try {
                    PreparedStatement statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS `" + tableName + "` (" +
                            " `uuid` varchar(255) NOT NULL," +
                            "PRIMARY KEY  (`uuid`))");
                    statement.executeUpdate();
                } catch (Exception ex) {
                    Logger.displayError(ex, "Error preforming SQL operation");
                }
            }
        }, 0);

        return status.get();
    }

    @Override
    public boolean disconnect() {
        try {
            connection.close();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    @Override
    public boolean isAlive() {
        AtomicBoolean status = new AtomicBoolean(false);

        // Check async
        MinecraftScheduler.get().desynchronize(() -> {
            try {
                status.set(!connection.isClosed());
            } catch (Exception ex) {
                status.set(false);
            }
        }, 0);

        return status.get();
    }
}
