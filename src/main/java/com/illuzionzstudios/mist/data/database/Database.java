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


import com.illuzionzstudios.mist.data.player.AbstractPlayer;
import com.illuzionzstudios.mist.data.player.OfflinePlayer;

import java.util.HashMap;
import java.util.List;

/**
 * A database where data can be stored
 */
public interface Database {

    /**
     * Get a cached value
     *
     * @param player The player who's data to get
     * @param queryingField The field we're trying to access
     * @return The value as an {@link Object} to be cast
     */
    default Object getCachedValue(AbstractPlayer player, String queryingField) {
        return player.getCachedData().getOrDefault(queryingField, null);
    }

    /**
     * Set a cached value
     *
     * @param player The player who's data to set
     * @param queryingField The field we're trying to access
     * @param value The value we're setting in the database
     */
    default void setCachedValue(AbstractPlayer player, String queryingField, Object value) {
        player.getCachedData().put(queryingField, value);
    }

    /**
     * Get's all stored fields for a player
     *
     * @return Field, Value pairs
     */
    HashMap<String, Object> getFields(AbstractPlayer player);

    /**
     * Get a value from the database
     *
     * @param player The player who's data to get
     * @param queryingField The field we're trying to access
     * @return The value as an {@link Object} to be cast
     */
    Object getFieldValue(AbstractPlayer player, String queryingField);

    /**
     * Set a value in the database
     *
     * @param player The player who's data to set
     * @param queryingField The field we're trying to access
     * @param value The value we're setting in the database
     */
    void setFieldValue(AbstractPlayer player, String queryingField, Object value);

    /**
     * Return a list of all saved players in the database
     * Returned as offline player as may not be online
     */
    List<OfflinePlayer> getSavedPlayers();

    /**
     * Open connection to database if necessary
     *
     * @return Connected successfully
     */
    boolean connect();

    /**
     * Dispose database if needed
     *
     * @return Disconnected successfully
     */
    boolean disconnect();

    /**
     * Check if the database is open and running
     *
     * @return If database can set and retrieve data
     */
    boolean isAlive();

}
