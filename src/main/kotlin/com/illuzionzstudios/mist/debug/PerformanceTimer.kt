package com.illuzionzstudios.mist.debug

/**
 * Check how long a task takes to run
 */
class PerformanceTimer {

    private var startTime: Long = 0L
    var timeTaken = 0L

    init {
        this.startTime = System.currentTimeMillis()
    }

    fun complete() {
        this.timeTaken = System.currentTimeMillis() - this.startTime
    }

}