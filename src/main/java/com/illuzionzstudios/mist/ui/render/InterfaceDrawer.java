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

import com.cryptomorin.xseries.XMaterial;
import com.illuzionzstudios.mist.Mist;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * This instance handles rendering an actual {@link com.illuzionzstudios.mist.ui.UserInterface}
 * It contains the methods like setting title, setting all items, etc
 */
public final class InterfaceDrawer {

    /**
     * Amount of slots in the inventory
     */
    @Getter
    private final int size;

    /**
     * The title for the interface
     *
     * Updating does not update interface, you have
     * to manually redraw it
     */
    @Getter
    @Setter
    private String title;

    /**
     * The items (or content) inside the inventory
     */
    private final ItemStack[] content;

    /**
     * Create a new drawer
     *
     * @param size Pre defined size of the inventory
     * @param title Title to display (supports colour codes)
     */
    private InterfaceDrawer(int size, String title) {
        this.size = size;
        this.title = title;

        this.content = new ItemStack[size];
    }

    /**
     * Push an item onto the stack. This means we set the next available slot,
     * or {@link org.bukkit.Material#AIR} to this item. If there are no free slots,
     * the last slot in the inventory is set
     *
     * @param item The item to push
     */
    public void pushItem(ItemStack item) {
        // If we found a slot and pushed
        boolean added = false;

        for (int i = 0; i < content.length; i++) {
            final ItemStack currentItem = content[i];

            if (currentItem == null) {
                content[i] = item;
                added = true;

                break;
            }
        }

        if (!added)
            content[size - 1] = item;
    }

    /**
     * Simple check for if a slot is not null
     *
     * @param slot Slot to check
     * @return If slot exists
     */
    public boolean isSet(int slot) {
        return getItem(slot) != null;
    }

    /**
     * Retrieve the {@link ItemStack} for a certain slot
     *
     * @param slot The slot to get the item by
     * @return Found {@link ItemStack} otherwise {@code null} if slot is outside
     *         total slots
     */
    public ItemStack getItem(int slot) {
        return slot < content.length ? content[slot] : null;
    }

    /**
     * Shorthand way to set an item in a slot
     *
     * @param slot The slot to set the item in
     * @param item The {@link ItemStack to set}
     */
    public void setItem(int slot, ItemStack item) {
        // Don't set out of bounds
        if (slot >= 0)
            content[slot] = item;
    }

    /**
     * Set the full content of this inventory
     *
     * If the given content is shorter, all additional inventory slots are replaced with air
     *
     * @param newContent the new content
     */
    public void setContent(ItemStack[] newContent) {
        for (int i = 0; i < content.length; i++)
            content[i] = i < newContent.length ? newContent[i] : new ItemStack(XMaterial.AIR.parseMaterial());
    }

    /**
     * Draw the interface for a player with all elements.
     * Closes other inventories when opening this one
     *
     * @param player The player to draw to
     */
    public void display(Player player) {
        // Create our inventory instance
        final Inventory inventory = Bukkit.createInventory(player, size, Mist.colorize("&7" + title));

        inventory.setContents(content);

        // Clear inventories and open
        if (player.getOpenInventory() != null)
            player.closeInventory();

        player.openInventory(inventory);
    }

    /**
     * Shorthand constructor. See {@link #InterfaceDrawer(int, String)}
     */
    public static InterfaceDrawer of(int size, String title) {
        return new InterfaceDrawer(size, title);
    }

}
