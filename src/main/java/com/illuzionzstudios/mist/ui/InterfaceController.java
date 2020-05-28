/**
 * Copyright © 2020 Property of Illuzionz Studios, LLC
 * All rights reserved. No part of this publication may be reproduced, distributed, or
 * transmitted in any form or by any means, including photocopying, recording, or other
 * electronic or mechanical methods, without the prior written permission of the publisher,
 * except in the case of brief quotations embodied in critical reviews and certain other
 * noncommercial uses permitted by copyright law. Any licensing of this software overrides
 * this statement.
 */
package com.illuzionzstudios.mist.ui;

import com.illuzionzstudios.mist.Logger;
import com.illuzionzstudios.mist.Mist;
import com.illuzionzstudios.mist.controller.PluginController;
import com.illuzionzstudios.mist.plugin.SpigotPlugin;
import com.illuzionzstudios.mist.ui.button.Button;
import com.illuzionzstudios.mist.ui.render.ClickLocation;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * This controller handles events for {@link UserInterface}
 */
public enum InterfaceController implements PluginController<SpigotPlugin>, Listener {
    INSTANCE;

    @Override
    public void initialize(SpigotPlugin plugin) {
        // Register the events
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void stop(SpigotPlugin plugin) {
    }

    /**
     * This event handles closing of {@link UserInterface}
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onInterfaceClose(final InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player))
            return;

        final Player player = (Player) event.getPlayer();
        final UserInterface userInterface = UserInterface.getInterface(player);

        if (userInterface != null) {
            userInterface.onInterfaceClose(player, event.getInventory());

            player.removeMetadata(UserInterface.TAG_CURRENT, SpigotPlugin.getInstance());
        }
    }

    /**
     * Handles invoking the {@link UserInterface} click listeners
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMenuClick(final InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player))
            return;

        final Player player = (Player) event.getWhoClicked();
        final UserInterface userInterface = UserInterface.getInterface(player);

        if (userInterface != null) {
            final ItemStack slotItem = event.getCurrentItem();
            final ItemStack cursor = event.getCursor();
            final Inventory clickedInv = event.getClickedInventory();

            final InventoryAction action = event.getAction();
            final ClickLocation whereClicked = clickedInv != null
                    ? clickedInv.getType() == InventoryType.CHEST ? ClickLocation.INTERFACE
                    : ClickLocation.PLAYER_INVENTORY
                    : ClickLocation.OUTSIDE;

            if (action.toString().contains("PICKUP") || action.toString().contains("PLACE")
                    || action.toString().equals("SWAP_WITH_CURSOR") || action == InventoryAction.CLONE_STACK) {
                if (whereClicked == ClickLocation.INTERFACE)
                    try {
                        final Button button = userInterface.getButton(slotItem);

                        if (button != null)
                            userInterface.onButtonClick(player, event.getSlot(), action, event.getClick(), button);
                        else
                            userInterface.onInterfaceClick(player, event.getSlot(), action, event.getClick(), cursor, slotItem,
                                    false);

                    } catch (final Throwable t) {
                        // Notify of error
                        player.sendMessage(Mist.colorize("&cOops! There was a problem with this menu! Please contact the administrator to review the console for details."));
                        player.closeInventory();

                        Logger.displayError(t, "Error clicking in menu " + userInterface);
                    }
            } else if (action == InventoryAction.MOVE_TO_OTHER_INVENTORY
                    || whereClicked != ClickLocation.PLAYER_INVENTORY) {
                event.setResult(Event.Result.DENY);

                player.updateInventory();
            }
        }
    }
}
