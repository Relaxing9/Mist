package com.illuzionzstudios.mist.data.player;

/**
 * Copyright © 2020 Property of Illuzionz Studios, LLC
 * All rights reserved. No part of this publication may be reproduced, distributed, or
 * transmitted in any form or by any means, including photocopying, recording, or other
 * electronic or mechanical methods, without the prior written permission of the publisher,
 * except in the case of brief quotations embodied in critical reviews and certain other
 * noncommercial uses permitted by copyright law. Any licensing of this software overrides
 * this statement.
 */

import com.illuzionzstudios.mist.data.PlayerData;
import com.illuzionzstudios.mist.data.controller.PlayerDataController;

import java.util.UUID;

/**
 * A player that we don't know if is offline or online
 */
public class OfflinePlayer extends AbstractPlayer {

    public OfflinePlayer(UUID uuid, String name) {
        super(uuid, name);
    }

    /*
     * Automatically creates a new abstract player data object
     * if not already applied by default.
     */
    @Override
    public <T extends PlayerData<?>> T get(Class<T> type) {
        T data = super.get(type);

        if (data != null) {
            return data;
        }

        data = PlayerDataController.INSTANCE.getDefaultData(this, type);
        return data;
    }

}
