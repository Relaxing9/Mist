/**
 * Copyright © 2020 Property of Illuzionz Studios, LLC
 * All rights reserved. No part of this publication may be reproduced, distributed, or
 * transmitted in any form or by any means, including photocopying, recording, or other
 * electronic or mechanical methods, without the prior written permission of the publisher,
 * except in the case of brief quotations embodied in critical reviews and certain other
 * noncommercial uses permitted by copyright law. Any licensing of this software overrides
 * this statement.
 */
package com.illuzionzstudios.mist.scheduler.rate;

import lombok.Getter;

/**
 * Sets a defined rate or interval between ticking
 */
public enum Rate {

    MIN_64(3840000L),
    MIN_32(1920000L),
    MIN_16(960000L),
    MIN_08(480000L),
    MIN_04(240000L),
    MIN_02(120000L),
    MIN_01(60000L),
    SLOWEST(32000L),
    SLOWER(16000L),
    SEC_10(10000L),
    SEC_8(8000L),
    SEC_6(6000L),
    SEC_4(4000L),
    SEC_2(2000L),
    SEC(1000L),
    FAST(500L),
    FASTER(250L),
    FASTEST(125L),
    TICK(50L),
    INSTANT(0L);

    /**
     * The amount of ticks between each tick of the object
     */
    @Getter
    private volatile long time;

    /**
     * The last amount of ticks passed
     */
    private volatile long last;

    /**
     * Total milliseconds spent ticking
     */
    private volatile long timeSpent;

    /**
     * Log the start time
     */
    private volatile long timeCount;

    Rate(long time) {
        this.time = time;
        this.last = System.currentTimeMillis();
    }

    /**
     * If the current time minus a time is greater than another
     *
     * @param from Starting time
     * @param required The time that must be met
     * @return If time is met
     */
    public static synchronized boolean elapsed(long from, long required) {
        return System.currentTimeMillis() - from > required;
    }

    /**
     * @return If total time to fully tick has passed
     */
    public synchronized boolean hasElapsed() {
        if (elapsed(this.last, this.time)) {
            this.last = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    /**
     * Start the time
     */
    public void startTime() {
        this.timeCount = System.currentTimeMillis();
    }

    /**
     * Stop the time
     */
    public void stopTime() {
        this.timeSpent += System.currentTimeMillis() - this.timeCount;
    }

    /**
     * Log and reset the time
     */
    public void printAndResetTime() {
        System.out.println(name() + " in a second: " + this.timeSpent);
        this.timeSpent = 0L;
    }
}
