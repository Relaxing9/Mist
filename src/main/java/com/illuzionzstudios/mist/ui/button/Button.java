/**
 * Copyright © 2020 Property of Illuzionz Studios, LLC
 * All rights reserved. No part of this publication may be reproduced, distributed, or
 * transmitted in any form or by any means, including photocopying, recording, or other
 * electronic or mechanical methods, without the prior written permission of the publisher,
 * except in the case of brief quotations embodied in critical reviews and certain other
 * noncommercial uses permitted by copyright law. Any licensing of this software overrides
 * this statement.
 */
package com.illuzionzstudios.mist.ui.button;

import com.illuzionzstudios.mist.compatibility.UMaterial;
import com.illuzionzstudios.mist.ui.UserInterface;
import com.illuzionzstudios.mist.ui.render.ItemCreator;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

/**
 * This represents a button in a {@link com.illuzionzstudios.mist.ui.UserInterface}
 * that can be interacted with an perform actions
 */
public abstract class Button {

    /**
     * The material representing info button, see {@link #makeInfo(String...)}
     */
    @Setter
    private static UMaterial infoButtonMaterial = UMaterial.NETHER_STAR;

    /**
     * @return The implemented {@link ButtonListener} to give functionality
     */
    public abstract ButtonListener getListener();

    /**
     * @return The {@link ItemStack} that represents this button as an icon
     */
    public abstract ItemStack getItem();

    //  -------------------------------------------------------------------------
    //  Pre construct buttons
    //  -------------------------------------------------------------------------

    /**
     * Construct a button that uses name and lore to display lines of text
     *
     * @param description Lines to display
     * @return The dummy icon
     */
    public static IconButton makeInfo(final String... description) {
        // Get all except first
        final List<String> lore = Arrays.asList(description).subList(1, Arrays.asList(description).size());

        // Format lines
        for (int i = 0; i < lore.size(); i++) {
            lore.set(i, "&7" + lore.get(i));
        }

        return makeIcon(ItemCreator.of(infoButtonMaterial).name(description[0]).hideTags(true).lores(lore));
    }

    /**
     * Construct an icon from a {@link ItemCreator.ItemCreatorBuilder}
     *
     * @param builder Builder of item
     * @return The now appropriate item
     */
    public static IconButton makeIcon(final ItemCreator.ItemCreatorBuilder builder) {
        return makeIcon(builder.build());
    }

    /**
     * Construct an icon from a {@link ItemCreator}
     *
     * @param creator Creator of an item
     * @return The now appropriate item
     */
    public static IconButton makeIcon(final ItemCreator creator) {
        return new IconButton(creator.makeUIItem());
    }

    /**
     * Represents a "blank" button. This means it doesn't do anything
     * when clicked but just renders an item stack
     */
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class IconButton extends Button {

        /**
         * The {@link ItemStack} to render
         */
        @Getter
        private final ItemStack item;

        /**
         * Null listener
         */
        @Override
        public ButtonListener getListener() {
            return ButtonListener.ofNull();
        }
    }

    /**
     * Represents a listener for a {@link Button} to
     * provide functionality
     */
    public interface ButtonListener {

        /**
         * Invoked when the button is clicked on
         *
         * @param player The {@link Player} who clicked
         * @param ui The instance of {@link UserInterface} that was clicked on
         * @param type How the button was clicked on
         */
        void onClickInInterface(Player player, UserInterface ui, ClickType type);

        /**
         * @return Simply returns a listener without functionlity
         */
        static ButtonListener ofNull() {
            return (i, j, k) -> {};
        }
    }

}
