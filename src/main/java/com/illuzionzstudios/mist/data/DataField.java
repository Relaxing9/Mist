/**
 * Copyright © 2020 Property of Illuzionz Studios, LLC
 * All rights reserved. No part of this publication may be reproduced, distributed, or
 * transmitted in any form or by any means, including photocopying, recording, or other
 * electronic or mechanical methods, without the prior written permission of the publisher,
 * except in the case of brief quotations embodied in critical reviews and certain other
 * noncommercial uses permitted by copyright law. Any licensing of this software overrides
 * this statement.
 */
package com.illuzionzstudios.mist.data;

import com.illuzionzstudios.mist.data.controller.PlayerDataController;
import com.illuzionzstudios.mist.data.player.AbstractPlayerData;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A field in a players data
 */
public class DataField<T> {

    /**
     * Local instance of our controller
     */
    private PlayerDataController<?, ?> controller = PlayerDataController.get();

    /**
     * Point in file where data is stored
     */
    @Getter
    private String field;

    /**
     * Data level this field is apart of
     */
    @Getter
    private AbstractPlayerData<?> playerData;

    /**
     * Default data for this field if not set
     */
    @Getter
    @Setter
    private T defaultData;

    /**
     * Stored data of this field
     */
    public T localValue;

    public DataField(AbstractPlayerData<?> playerData, String field, T defaultData) {
        this.playerData = playerData;
        this.field = field;
        this.defaultData = defaultData;
    }

    /**
     * If the actual field is set
     */
    public boolean isSet() {
        T value = (T) controller.getDatabase().getCachedValue(playerData.getPlayer(), getQueryingField());

        // Check value is actually set
        return value != null;
    }

    /**
     * Get the actual value stored in data
     */
    public T get() {
        T fetch = (T) controller.getDatabase().getCachedValue(playerData.getPlayer(), getQueryingField());

        // If not set or null, set default
        if (fetch == null) {
            set(defaultData);

            // Default is null so ultimately return null
            if (defaultData == null) return null;

            // Rerun as is set
            return get();
        }

        // Make sure we update local
        this.localValue = fetch;

        // Finally return found value
        return fetch;
    }

    /**
     * Set a value in the field
     *
     * @param value The value of type T to set
     */
    public void set(T value) {
        // Current set value if any
        T current = (T) controller.getDatabase().getCachedValue(playerData.getPlayer(), getQueryingField());

        // Clear any existing modifications queries if new value is null
        if (value == null) {
            playerData.getPlayer().getModifiedKeys().removeIf(s -> s.contains(getQueryingField()));
        }

        // New value is not the current so we can modify it
        if (current == null || !current.equals(value)) {
            playerData.getPlayer().modifyKey(getQueryingField());
        }

        // Update local value
        this.localValue = value;
        controller.getDatabase().setCachedValue(playerData.getPlayer(), getQueryingField(), value);
    }

    /**
     * Used to update the current querying field ready for upload
     *
     * Used when the get() value modified something (i.e a list from get() was modified)... Basically only used for key tracking
     */
    public void set() {
        String queryingField = getQueryingField();
        playerData.getPlayer().modifyKey(queryingField);
    }

    public T getLocalValue() {
        if (this.localValue == null) {
            this.localValue = get();
        }

        return this.localValue;
    }

    /**
     * This will grab the final field we use for querying based
     * on local keys to replace
     */
    private String getQueryingField() {
        String queryingField = field;

        // Replace necessary metadata
        HashMap<String, String> globalKeyMetadata = playerData.getPlayer().getKeyMetadata();
        HashMap<String, String> localKeyMetadata = playerData.localKeys;

        Iterator<Map.Entry<String, String>> localIter = globalKeyMetadata.entrySet().iterator();
        Iterator<Map.Entry<String, String>> globalIter = localKeyMetadata.entrySet().iterator();

        while (localIter.hasNext()) {
            Map.Entry<String, String> localEntry = localIter.next();

            if (contains(field, localEntry.getKey())) {
                queryingField = StringUtils.replace(queryingField, localEntry.getKey(), localEntry.getValue());
            }
        }

        while (globalIter.hasNext()) {
            Map.Entry<String, String> globalEntry = globalIter.next();

            if (contains(field, globalEntry.getKey())) {
                queryingField = StringUtils.replace(queryingField, globalEntry.getKey(), globalEntry.getValue());
            }
        }

        if (contains(queryingField, "{")) {
            throw new RuntimeException("Tried to access player metadata before prepared... " + field + " for " + playerData.getPlayer().getName());
        }

        return queryingField;
    }

    private boolean contains(String queryingField, String s) {
        return queryingField.toLowerCase().contains(s.toLowerCase());
    }
}
