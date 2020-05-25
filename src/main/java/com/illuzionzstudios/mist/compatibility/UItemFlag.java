/**
 * Copyright © 2020 Property of Illuzionz Studios, LLC
 * All rights reserved. No part of this publication may be reproduced, distributed, or
 * transmitted in any form or by any means, including photocopying, recording, or other
 * electronic or mechanical methods, without the prior written permission of the publisher,
 * except in the case of brief quotations embodied in critical reviews and certain other
 * noncommercial uses permitted by copyright law. Any licensing of this software overrides
 * this statement.
 */
package com.illuzionzstudios.mist.compatibility;

import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Wrapper for cross-version for {@link org.bukkit.inventory.ItemFlag}
 */
public enum UItemFlag {

    /**
     * Setting to show/hide enchants
     */
    HIDE_ENCHANTS,

    /**
     * Setting to show/hide Attributes like Damage
     */
    HIDE_ATTRIBUTES,

    /**
     * Setting to show/hide the unbreakable State
     */
    HIDE_UNBREAKABLE,

    /**
     * Setting to show/hide what the ItemStack can break/destroy
     */
    HIDE_DESTROYS,

    /**
     * Setting to show/hide where this ItemStack can be build/placed on
     */
    HIDE_PLACED_ON,

    /**
     * Setting to show/hide potion effects on this ItemStack
     */
    HIDE_POTION_EFFECTS;

    /**
     * Tries to apply this item flag to the given item, fails silently
     *
     * @param item
     */
    public final void applyTo(ItemStack item) {
        try {
            final ItemMeta meta = item.getItemMeta();
            final ItemFlag bukkitFlag = ItemFlag.valueOf(toString());

            meta.addItemFlags(bukkitFlag);

            item.setItemMeta(meta);

        } catch (final Throwable ignored) {
            // Unsupported MC version
        }
    }

}
