package com.illuzionzstudios.mist.model;

import lombok.Getter;

/**
 * Simple class that contains two values at once
 */
public class Pair<K, V> {

    /**
     * First value
     */
    @Getter
    private final K key;

    /**
     * Second value
     */
    @Getter
    private final V value;

    /**
     * Construct pair from 2 values
     */
    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

}
