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

import com.illuzionzstudios.mist.plugin.SpigotPlugin;
import com.illuzionzstudios.mist.ui.button.Button;
import com.illuzionzstudios.mist.util.ReflectionUtil;
import com.illuzionzstudios.mist.util.Valid;
import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.entity.Player;

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

}
