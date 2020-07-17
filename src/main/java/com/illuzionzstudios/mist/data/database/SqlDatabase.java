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
import com.illuzionzstudios.mist.data.player.AbstractPlayer;
import com.illuzionzstudios.mist.data.player.OfflinePlayer;
import com.illuzionzstudios.mist.scheduler.MinecraftScheduler;
import lombok.RequiredArgsConstructor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Use MySql or any Sql database for our data
 */
@RequiredArgsConstructor
public class SqlDatabase implements Database {

    /**
     * Our connection to handle SQL operations
     */
    protected Connection connection;

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

    @Override
    public HashMap<String, Object> getFields(AbstractPlayer player) {
        return null;
    }

    @Override
    public Object getFieldValue(AbstractPlayer player, String queryingField) {
        return null;
    }

    @Override
    public void setFieldValue(AbstractPlayer player, String queryingField, Object value) {

    }

    @Override
    public List<OfflinePlayer> getSavedPlayers() {
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
                    connection = DriverManager.getConnection("jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database, this.username, this.password);
                    status.set(true);
                }
            } catch (Exception ex) {
                Logger.displayError(ex, "Couldn't connect to database");
                status.set(false);
            }
        }, 0);

        return status.get();
    }

    @Override
    public boolean disconnect() {
        AtomicBoolean status = new AtomicBoolean(false);

        // Disconnect async
        MinecraftScheduler.get().desynchronize(() -> {
            try {
                connection.close();
                status.set(true);
            } catch (Exception ex) {
                Logger.displayError(ex, "Error disconnecting from database");
                status.set(false);
            }
        }, 0);

        return status.get();
    }

    @Override
    public boolean isAlive() {
        AtomicBoolean status = new AtomicBoolean(false);

        // Check async
        MinecraftScheduler.get().desynchronize(() -> {
            try {
                status.set(!connection.isClosed());
            } catch (SQLException throwables) {
                status.set(false);
            }
        }, 0);

        return status.get();
    }
}
