package com.illuzionzstudios.mist.scheduler.timer;

import lombok.Getter;
import lombok.Setter;

/**
 * A pre set cooldown that can be started when we wish
 */
@Getter
@Setter
public class PresetCooldown extends Cooldown {

    /**
     * The amount of ticks to wait
     */
    private int wait;

    public PresetCooldown(int defaultWait) {
        wait = defaultWait;
    }

    /**
     * Start the timer
     */
    public void go() {
        super.setWait(wait);
    }
}
