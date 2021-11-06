package com.illuzionzstudios.mist.compatibility;

import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Wrapper for cross-version for {@link org.bukkit.inventory.ItemFlag}
 */
public enum XItemFlag {

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
    HIDE_POTION_EFFECTS,

    HIDE_DYE;

    /**
     * Tries to apply this item flag to the given item, fails silently
     */
    public final void applyTo(final ItemStack item) {
        try {
            final ItemMeta meta = item.getItemMeta();
            final ItemFlag bukkitFlag = ItemFlag.valueOf(toString());

            if (meta != null)
                meta.addItemFlags(bukkitFlag);
            item.setItemMeta(meta);

        } catch (final Throwable ignored) {
            // Unsupported MC version
        }
    }

}
