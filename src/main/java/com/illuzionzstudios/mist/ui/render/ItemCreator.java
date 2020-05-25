/**
 * Copyright © 2020 Property of Illuzionz Studios, LLC
 * All rights reserved. No part of this publication may be reproduced, distributed, or
 * transmitted in any form or by any means, including photocopying, recording, or other
 * electronic or mechanical methods, without the prior written permission of the publisher,
 * except in the case of brief quotations embodied in critical reviews and certain other
 * noncommercial uses permitted by copyright law. Any licensing of this software overrides
 * this statement.
 */
package com.illuzionzstudios.mist.ui.render;

import com.illuzionzstudios.mist.Mist;
import com.illuzionzstudios.mist.compatibility.ServerVersion;
import com.illuzionzstudios.mist.compatibility.UItemFlag;
import com.illuzionzstudios.mist.compatibility.UMaterial;
import com.illuzionzstudios.mist.compatibility.UProperty;
import com.illuzionzstudios.mist.util.TextUtil;
import com.illuzionzstudios.mist.util.Valid;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import net.minecraft.server.v1_15_R1.MinecraftVersion;
import net.minecraft.server.v1_15_R1.Tuple;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * Utility class to easily build custom items. We can set flags,
 * names and lore, and enchantments.
 */
@Builder
public final class ItemCreator {

    /**
     * The actual item stack this represents
     */
    private final ItemStack item;

    /**
     * The {@link UMaterial} of the item
     */
    private final UMaterial material;

    /**
     * The amount of items in the stack
     */
    @Builder.Default
    private final int amount = 1;

    /**
     * Damage of the item for setting custom metadata
     */
    @Builder.Default
    private final int damage = -1;

    /**
     * The display name of the item
     */
    private final String name;

    /**
     * The lore strings to display
     */
    @Singular
    private final List<String> lores;


    /**
     * The enchants applied for the item mapped by level
     */
    @Singular
    private final Map<Enchantment, Integer> enchants;

    /**
     * The item flags
     */
    @Singular
    private List<UItemFlag> flags;

    /**
     * If the {@link ItemStack} has the unbreakable flag
     */
    private boolean unbreakable;

    /**
     * Should we hide all tags from the item (enchants, etc.)?
     */
    @Builder.Default
    private boolean hideTags = false;

    /**
     * Should we add glow to the item? (adds a fake enchant and uses
     * item flags to hide it)
     *
     * The enchant is visible on older MC versions.
     */
    private final boolean glow;

    /**
     * The actual metadata of the item stack
     */
    private final ItemMeta meta;

    //  -------------------------------------------------------------------------
    //  Constructing
    //  -------------------------------------------------------------------------

    /**
     * @return This item suitable for a {@link com.illuzionzstudios.mist.ui.UserInterface}
     */
    public ItemStack makeUIItem() {
        unbreakable = true;
        hideTags = true;

        return make();
    }

    /**
     * Finally construct the {@link ItemStack} from all parameters
     *
     * @return The built item
     */
    public ItemStack make() {

        // Make sure base item and material are set
        Valid.checkBoolean((material != null && material.getMaterial() != null) || item != null, "Material or item must be set!");

        // Actual item we're building on
        ItemStack stack = item != null ? item.clone() : new ItemStack(material.getMaterial(), amount);
        final ItemMeta stackMeta = meta != null ? meta.clone() : stack.getItemMeta();

        Valid.checkNotNull(stackMeta, "Item metadata was somehow null");

        // Skip if trying to build on air
        if (material == UMaterial.AIR)
            return stack;

        // Ensure flags exist
        flags = flags != null ? flags : new ArrayList<>();

        // Set damage
        if (damage != -1) {
            try {
                stack.setDurability((short) damage);
            } catch (final Throwable ignored) {
            }

            try {
                if (stackMeta instanceof Damageable) {
                    ((Damageable) stackMeta).setDamage(damage);
                }
            } catch (final Throwable ignored) {
            }
        }

        // Glow
        if (glow) {
            stackMeta.addEnchant(Enchantment.DURABILITY, 1, true);

            flags.add(UItemFlag.HIDE_ENCHANTS);
        }

        // Enchantments
        if (enchants != null) {
            for (final Enchantment ench : enchants.keySet()) {
                stackMeta.addEnchant(ench, enchants.get(ench), true);
            }
        }

        // Name and lore
        if (name != null) {
            stackMeta.setDisplayName(Mist.colorize("&r" + name));
        }

        if (lores != null && !lores.isEmpty()) {
            final List<String> coloredLores = new ArrayList<>();

            lores.forEach(line -> coloredLores.add(Mist.colorize("&7" + line)));
            stackMeta.setLore(coloredLores);
        }

        // Unbreakable
        if (unbreakable) {
            flags.add(UItemFlag.HIDE_ATTRIBUTES);
            flags.add(UItemFlag.HIDE_UNBREAKABLE);

            if (ServerVersion.olderThan(ServerVersion.V.v1_12)) {
                try {
                    final Object spigot = stackMeta.getClass().getMethod("spigot").invoke(stackMeta);

                    spigot.getClass().getMethod("setUnbreakable", boolean.class).invoke(spigot, true);

                } catch (final Throwable ignored) {
                    // Probably 1.7.10, tough luck
                }
            } else {
                UProperty.UNBREAKABLE.apply(stackMeta, true);
            }
        }

        // Hide flags
        if (hideTags) {
            for (final UItemFlag f : UItemFlag.values()) {
                if (!flags.contains(f)) {
                    flags.add(f);
                }
            }
        }

        // Apply flags
        for (final UItemFlag flag : flags) {
            try {
                stackMeta.addItemFlags(ItemFlag.valueOf(flag.toString()));
            } catch (final Throwable ignored) {
            }
        }

        // Finally apply metadata
        item.setItemMeta(stackMeta);

        return item;
    }

    /**
     * Convenience method to get a new item creator with material, name and lore set
     *
     * @param material The {@link UMaterial} to set
     * @param name The name of the item
     * @param lore Collection of lore strings
     * @return THe builder with these properties
     */
    public static ItemCreatorBuilder of(final UMaterial material, final String name, @NonNull final Collection<String> lore) {
        return of(material, name, lore.toArray(new String[lore.size()]));
    }

    /**
     * See {@link #of(UMaterial, String, Collection)}
     */
    public static ItemCreatorBuilder of(final UMaterial material, final String name, @NonNull final String... lore) {
        return ItemCreator.builder().material(material).name("&r" + name).lores(Arrays.asList(lore)).hideTags(true);
    }

    /**
     * Get a new item creator from material
     *
     * @param mat existing material
     * @return the new item creator
     */
    public static ItemCreatorBuilder of(final UMaterial mat) {
        Valid.checkNotNull(mat, "Material cannot be null!");

        return ItemCreator.builder().material(mat);
    }

}
