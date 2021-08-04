package com.illuzionzstudios.mist.util;

import lombok.Getter;

/**
 * Simple class that contains two values at once
 */
public class Pair<K, V> {

    /**
     * First value
     */
    @Getter
    private K key;

    /**
     * Second value
     */
    @Getter
    private V value;

    /**
     * Construct pair from 2 values
     */
    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

}
