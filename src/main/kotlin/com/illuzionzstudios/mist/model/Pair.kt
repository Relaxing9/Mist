package com.illuzionzstudios.mist.model

import com.illuzionzstudios.mist.compatibility.ServerVersion.V
import lombok.Getter

/**
 * Simple class that contains two values at once
 */
class Pair<K, V>
/**
 * Construct pair from 2 values
 */(
    /**
     * First value
     */
    val key: K,
    /**
     * Second value
     */
    val value: V
)