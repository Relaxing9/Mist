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

import com.illuzionzstudios.mist.exception.PluginException;
import com.illuzionzstudios.mist.plugin.SpigotPlugin;
import com.illuzionzstudios.mist.ui.button.Button;
import com.illuzionzstudios.mist.util.ReflectionUtil;
import com.illuzionzstudios.mist.util.Valid;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.awt.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Our main menu (or interface) for all inventory interaction. We provide
 * a lot of functionality for adding buttons, storing items, and being
 * able to navigate through the menu easily. We can also have a parent menu
 * that can be returned to.
 */
public abstract class UserInterface {

    //  -------------------------------------------------------------------------
    //  Static variables
    //  -------------------------------------------------------------------------

    /**
     * This is an internal metadata tag that the player has.
     *
     * This will set the name of the current menu in order to keep
     * track of what menu is currently open
     */
    public static final String TAG_CURRENT = "UI_" + SpigotPlugin.getPluginName();

    /**
     * This is an internal metadata tag that the player has.
     *
     * This will set the name of the previous menu in order to
     * backtrack for returning menus
     */
    public static final String TAG_PREVIOUS = "UI_PREVIOUS_" + SpigotPlugin.getPluginName();

    /**
     * Registered buttons to this menu (via reflection) to add in rendering
     */
    private final List<Button> registeredButtons = new ArrayList<>();

    //  -------------------------------------------------------------------------
    //  Properties of an interface
    //  -------------------------------------------------------------------------

    /**
     * The parent interface we entered from. This allows us to return to a previous menu
     * Can be {@code null} if no parent
     */
    private final UserInterface parent;

    /**
     * The return button to display if applicable
     */
    private final Button returnButton;

    /**
     * Amount of slots in the inventory
     */
    private int size = 9 * 3;

    /**
     * This is the title to display at the top of the interface
     */
    private String title = "&7Menu";

    /**
     * This is the description of the menu that can be displayed
     * at a certain slot
     */
    @Getter(value = AccessLevel.PROTECTED)
    private String[] info = null;

    /**
     * This is the player currently viewing the menu.
     * Isn't set till displayed to a player
     */
    private Player viewer;

    /**
     * See {@link #UserInterface(UserInterface)}
     *
     * Creates a {@link UserInterface} with no parent
     */
    protected UserInterface() {
        this(null);
    }

    /**
     * Base constructor to create a {@link UserInterface} with the size 9 * 3
     * with a parent menu.
     *
     * You should set the size and title of the {@link UserInterface} in
     * the constructor.
     *
     * Note: The viewer is still null here
     *
     * @param parent The parent {@link UserInterface}
     */
    protected UserInterface(final UserInterface parent) {
        this.parent = parent;
    }

    //  -------------------------------------------------------------------------
    //  Getting menus
    //  -------------------------------------------------------------------------

    /**
     * Get the currently active menu for the player
     *
     * @param player The player to get menu for
     * @return Found interface or {@code null} See {@link #getInterfaceViaTag(Player, String)}
     */
    public static UserInterface getInterface(final Player player) {
        return getInterfaceViaTag(player, TAG_CURRENT);
    }

    /**
     * Get the previous active menu for the player
     *
     * @param player The player to get menu for
     * @return Found interface or {@code null} See {@link #getInterfaceViaTag(Player, String)}
     */
    public static UserInterface getPrevious(final Player player) {
        return getInterfaceViaTag(player, TAG_PREVIOUS);
    }

    /**
     * Get a {@link UserInterface} from the metadata on a player
     *
     * @param player The player to check metadata
     * @param tag The name of the tag storing the interface
     * @return Found {@link UserInterface} otherwise {@code null}
     */
    public static UserInterface getInterfaceViaTag(final Player player, final String tag) {
        if (player.hasMetadata(tag)) {
            // Cast from tag
            final UserInterface userInterface = (UserInterface) player.getMetadata(tag).get(0).value();
            Valid.checkNotNull(userInterface, "Interface was missing from " + player.getName() + "'s metadata " + tag + "tag!");

            return userInterface;
        }

        return null;
    }

    //  -------------------------------------------------------------------------
    //  Button utils
    //  -------------------------------------------------------------------------

    /**
     * Scans the class for every {@link Button} instance and registers it
     */
    protected final void registerButtons() {
        // Don't double register stuff
        registeredButtons.clear();

        // Register buttons explicitly given
        {
            final List<Button> buttons = getButtonsToRegister();

            if (buttons != null)
                registeredButtons.addAll(buttons);
        }

        // Register buttons declared as fields
        {
            Class<?> lookup = getClass();

            // Scan every class and super until interface class
            do
                for (final Field f : lookup.getDeclaredFields())
                    registerButtonViaReflection(f);
            while (UserInterface.class.isAssignableFrom(lookup = lookup.getSuperclass()));
        }
    }

    /**
     * Registers a {@link Button} into this {@link UserInterface} if the
     * field is a {@link Button}
     *
     * @param field The field to register
     */
    private void registerButtonViaReflection(final Field field) {
        field.setAccessible(true);

        final Class<?> clazz = field.getType();

        // Is just a button instance
        if (Button.class.isAssignableFrom(clazz)) {
            // Get button
            final Button button = (Button) ReflectionUtil.getFieldContent(field, this);

            Valid.checkNotNull(button, "Invalid button for names field " + field.getName());
            registeredButtons.add(button);
        } else if (Button[].class.isAssignableFrom(clazz)) {
            // Array of buttons
            Valid.checkBoolean(Modifier.isFinal(field.getModifiers()),
                    "Button[] field must be final: " + field);
            final Button[] buttons = (Button[]) ReflectionUtil.getFieldContent(field, this);

            Valid.checkBoolean(buttons != null && buttons.length > 0, "Null " + field.getName() + "[] in " + this);
            registeredButtons.addAll(Arrays.asList(buttons));
        }
    }

    /**
     * @return A list of buttons to manually add instead of scanning
     */
    protected List<Button> getButtonsToRegister() {
        return null;
    }

    /**
     * Try to get a button with a specific icon from {@link ItemStack}
     *
     * @param icon {@link ItemStack} to find by
     * @return Found button otherwise null
     */
    public final Button getButton(final ItemStack icon) {
        if (icon != null) {
            for (final Button button : registeredButtons) {
                // Make sure valid button
                Valid.checkNotNull(button, "Menu button is null at " + getClass().getSimpleName());
                if (button.getItem() == null)
                    return null;

                if (icon.isSimilar(button.getItem()))
                    return button;
            }
        }

        return null;
    }

    /**
     * Return a new instance of this interface
     *
     * You must override this in certain cases
     *
     * @return the new instance, of null
     * @throws PluginException if new instance could not be made, for example when the menu is
     *            taking constructor params
     */
    public UserInterface newInstance() {
        try {
            return ReflectionUtil.instantiate(getClass());
        } catch (final Throwable t) {
            try {
                final Object parent = getClass().getMethod("getParent").invoke(getClass());

                if (parent != null)
                    return ReflectionUtil.instantiate(getClass(), parent);
            } catch (final Throwable ignored) {
            }

            t.printStackTrace();
        }

        throw new PluginException("Could not instatiate menu of " + getClass()
                + ", override 'newInstance' and ensure constructor is public!");
    }

    //  -------------------------------------------------------------------------
    //  Final getters and setters
    //  -------------------------------------------------------------------------

    /**
     * Returns the item at a certain slot
     *
     * @param slot the slow
     * @return the item, or null if no icon at the given slot (default)
     */
    public ItemStack getItemAt(final int slot) {
        return null;
    }

    /**
     * Get the info button position
     *
     * @return the slot which info buttons is located on
     */
    protected int getInfoButtonPosition() {
        return size - 9;
    }

    /**
     * Should we automatically add the return button to the bottom left corner?
     *
     * @return true if the return button should be added, true by default
     */
    protected boolean addReturnButton() {
        return true;
    }

    /**
     * Should we automatically add an info button {@link #getInfo()} at the
     * {@link #getInfoButtonPosition()} ?
     *
     * @return If to add button
     */
    protected boolean addInfoButton() {
        return true;
    }

    /**
     * Get the return button position
     *
     * @return the slot which return buttons is located on
     */
    protected int getReturnButtonPosition() {
        return size - 1;
    }

    /**
     * Calculates the center slot of this menu
     *
     * <p>
     * Credits to Gober at
     * https://www.spigotmc.org/threads/get-the-center-slot-of-a-menu.379586/
     *
     * @return the estimated center slot
     */
    protected final int getCenterSlot() {
        final int pos = size / 2;

        return size % 2 == 1 ? pos : pos - 5;
    }

    /**
     * The title of this menu
     *
     * @return the menu title
     */
    public final String getTitle() {
        return title;
    }

    /**
     * Sets the title of this inventory, this change is not reflected in client, you
     * must call {@link #restartMenu()} to take change
     *
     * @param title the new title
     */
    protected final void setTitle(final String title) {
        this.title = title;
    }

    /**
     * Return the parent menu or null
     *
     * @return
     */
    public final UserInterface getParent() {
        return parent;
    }

    /**
     * Get the size of this menu
     *
     * @return
     */
    public final Integer getSize() {
        return size;
    }

    /**
     * Sets the size of this menu (without updating the player container - if you
     * want to update it call {@link #restartMenu()})
     *
     * @param size
     */
    protected final void setSize(final Integer size) {
        this.size = size;
    }

    /**
     * Set the menu's description
     *
     * <p>
     * Used to create an info bottom in bottom left corner, see
     * {@link Button#makeInfo(String...)}
     *
     * @param info the info to set
     */
    protected final void setInfo(final String... info) {
        this.info = info;
    }

    /**
     * Get the viewer that this instance of this menu is associated with
     *
     * @return the viewer of this instance, or null
     */
    protected final Player getViewer() {
        return viewer;
    }

    /**
     * Sets the viewer for this instance of this menu
     *
     * @param viewer The new viewer of the menu. Only sets the player
     *               doesn't perform any magic
     */
    protected final void setViewer(final Player viewer) {
        this.viewer = viewer;
    }

    /**
     * Return the top opened inventory if viewer exists
     *
     * @return The open inventory instance
     */
    protected final Inventory getInventory() {
        Valid.checkNotNull(viewer, "Cannot get inventory when there is no viewer!");

        final Inventory topInventory = viewer.getOpenInventory().getTopInventory();
        Valid.checkNotNull(topInventory, "Top inventory is null!");

        return topInventory;
    }

    /**
     * Get the open inventory content to match the array length, cloning items
     * preventing ID mismatch in yaml files
     *
     * @param from The slot to start from
     * @param to The slot to end at
     * @return The array of found {@link ItemStack} can contain {@link org.bukkit.Material#AIR}
     */
    protected final ItemStack[] getContent(final int from, final int to) {
        final ItemStack[] content = getInventory().getContents();
        final ItemStack[] copy = new ItemStack[content.length];

        for (int i = from; i < copy.length; i++) {
            final ItemStack item = content[i];

            copy[i] = item != null ? item.clone() : null;
        }

        return Arrays.copyOfRange(copy, from, to);
    }

    //  -------------------------------------------------------------------------
    //  Interface events
    //  -------------------------------------------------------------------------
}
