package com.illuzionzstudios.mist.data.player;

import com.illuzionzstudios.mist.data.PlayerData;
import com.illuzionzstudios.mist.scheduler.MinecraftScheduler;
import lombok.Getter;

import java.util.HashMap;

/**
 * Copyright © 2020 Property of Illuzionz Studios, LLC
 * All rights reserved. No part of this publication may be reproduced, distributed, or
 * transmitted in any form or by any means, including photocopying, recording, or other
 * electronic or mechanical methods, without the prior written permission of the publisher,
 * except in the case of brief quotations embodied in critical reviews and certain other
 * noncommercial uses permitted by copyright law. Any licensing of this software overrides
 * this statement.
 */

/**
 * Registered player data
 */
public abstract class AbstractPlayerData<P extends AbstractPlayer> implements PlayerData<P> {

    public AbstractPlayerData(P player) {
        this.player = player;
    }

    /**
     * The player that owns this data
     */
    @Getter
    protected P player;

    /**
     * Run when attempting to save data
     */
    public void onSave() {
    }

    /**
     * Keys to replace when querying
     */
    public HashMap<String, String> localKeys = new HashMap<>();

    /**
     * If the scheduler is registered
     */
    private boolean schedulerRegistered = false;

    public void unregister() {
        if (schedulerRegistered) {
            MinecraftScheduler.get().dismissSynchronizationService(this);
        }
    }

    protected void registerScheduler() {
        MinecraftScheduler.get().registerSynchronizationService(this);
        schedulerRegistered = true;
    }

}
