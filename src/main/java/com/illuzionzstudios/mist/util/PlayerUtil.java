/**
 * Copyright © 2020 Property of Illuzionz Studios, LLC
 * All rights reserved. No part of this publication may be reproduced, distributed, or
 * transmitted in any form or by any means, including photocopying, recording, or other
 * electronic or mechanical methods, without the prior written permission of the publisher,
 * except in the case of brief quotations embodied in critical reviews and certain other
 * noncommercial uses permitted by copyright law. Any licensing of this software overrides
 * this statement.
 */
package com.illuzionzstudios.mist.util;

import com.illuzionzstudios.mist.plugin.SpigotPlugin;
import lombok.NonNull;
import org.bukkit.permissions.Permissible;
import org.jetbrains.annotations.Nullable;

/**
 * Utility method for dealing with players
 */
public final class PlayerUtil {

    /**
     * Return if the given sender has a certain permission
     * You can use {plugin.name} to replace with your plugin name (lower-cased)
     *
     * @param sender The {@link Permissible} object to check permissions
     * @param permission The unformatted permission to check
     * @return If the {@link Permissible} had that permission set to true
     */
    public static boolean hasPerm(@NonNull final Permissible sender, @Nullable final String permission) {
        return permission == null || sender.hasPermission(permission.replace("{plugin.name}", SpigotPlugin.getPluginName().toLowerCase()));
    }

}
