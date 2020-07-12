/**
 * Copyright © 2020 Property of Illuzionz Studios, LLC
 * All rights reserved. No part of this publication may be reproduced, distributed, or
 * transmitted in any form or by any means, including photocopying, recording, or other
 * electronic or mechanical methods, without the prior written permission of the publisher,
 * except in the case of brief quotations embodied in critical reviews and certain other
 * noncommercial uses permitted by copyright law. Any licensing of this software overrides
 * this statement.
 */
package com.illuzionzstudios.mist.ui.type;

import com.illuzionzstudios.mist.compatibility.XMaterial;
import com.illuzionzstudios.mist.compatibility.XSound;
import com.illuzionzstudios.mist.config.Configurable;
import com.illuzionzstudios.mist.config.locale.Locale;
import com.illuzionzstudios.mist.config.locale.Message;
import com.illuzionzstudios.mist.conversation.SimpleConversation;
import com.illuzionzstudios.mist.conversation.SimplePrompt;
import com.illuzionzstudios.mist.conversation.type.SimpleDecimalPrompt;
import com.illuzionzstudios.mist.ui.UserInterface;
import com.illuzionzstudios.mist.ui.button.Button;
import com.illuzionzstudios.mist.ui.render.ItemCreator;
import com.illuzionzstudios.mist.util.ReflectionUtil;
import com.illuzionzstudios.mist.util.TextUtil;
import com.illuzionzstudios.mist.util.Valid;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Material;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

/**
 * Menu to view all options to configure for a {@link Object}
 * Abstracted to an item to configure those fields
 * <p>
 * A maximum of 54 options per object
 * <p>
 *
 * @param <T> Type of item to configure for
 *            All public non-final fields will appear for configuration
 *            <p>
 *            Supported Types:
 *            {@link Integer}
 *            {@link Double}
 *            {@link Float}
 *            {@link Boolean}
 *            {@link String}
 *            {@link XSound}
 *            {@link XMaterial}
 *            {@link Message}
 *            {@link ItemStack}
 *            And {@link java.util.Collection} of aforementioned
 *            <p>
 *            Else a blank menu appears with no options to configure
 */
public class ConfigureOptionsUI<T> extends UserInterface {

    /**
     * List of option buttons
     */
    private final List<Button> options;
    /**
     * The object to configure
     */
    public T object;

    /**
     * @param object The object instance to configure
     */
    public ConfigureOptionsUI(T object) {
        this(null, object);
    }

    /**
     * @param parent Parent {@link UserInterface}
     * @param object The object instance to configure
     */
    public ConfigureOptionsUI(UserInterface parent, T object) {
        super(parent, false);
        setTitle("&8Configure Options");

        this.options = new ArrayList<>();
        this.object = object;

        // Set all options
        prepareOptions();
    }

    /**
     * Called to scan object for classes and set buttons to
     * open further configuring fields
     */
    private void prepareOptions() {
        // Make sure fresh list
        options.clear();

        // Scan each field and set button based on type
        // Type of object
        Class<?> lookup = object.getClass();

        // Now scan fields to set buttons
        for (final Field f : lookup.getDeclaredFields()) {
            // Make sure field is annotated
            if (f.getAnnotationsByType(Configurable.class).length == 0) continue;

            // Get raw field description
            // Describes what the field is
            String valueDescriptionKey = f.getAnnotation(Configurable.class).description();

            // Get the type of field for checking
            Class<?> type = f.getType();

            // The prompt to accept value
            Prompt valuePrompt = null;
            // Value object to display
            // Auto calls .toString() method if not a string
            Object value = ReflectionUtil.getFieldContent(f, object);
            Button.ButtonListener listener = Button.ButtonListener.ofNull();

            // Message to indicate to input a value
            String enterValueMessage = Locale.Config.ENTER_VALUE;

            // Open appropriate menu
            if (int.class.isAssignableFrom(type)) {
                valuePrompt = new SimpleDecimalPrompt(enterValueMessage, d -> {
                    preSetValue(object, f, d);

                    try {
                        f.setInt(object, (int) Math.round(d));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }

                    onSetValue(object, f, d);
                });
            } else if (float.class.isAssignableFrom(type)) {
                valuePrompt = new SimpleDecimalPrompt(enterValueMessage, d -> {
                    preSetValue(object, f, d);

                    try {
                        f.setFloat(object, d.floatValue());
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }

                    onSetValue(object, f, d);
                });
            } else if (double.class.isAssignableFrom(type)) {
                valuePrompt = new SimpleDecimalPrompt(enterValueMessage, d -> {
                    preSetValue(object, f, d);

                    try {
                        f.setDouble(object, d);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }

                    onSetValue(object, f, d);
                });
            } else if (boolean.class.isAssignableFrom(type)) {
                valuePrompt = new SimplePrompt() {
                    @Override
                    protected String getPrompt(ConversationContext conversationContext) {
                        return enterValueMessage;
                    }

                    @Nullable
                    @Override
                    protected Prompt acceptValidatedInput(@NotNull ConversationContext conversationContext, @NotNull String s) {
                        preSetValue(object, f, s);

                        try {
                            f.setBoolean(object, Valid.parseBoolean(s));
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }

                        onSetValue(object, f, s);
                        return null;
                    }
                };
            } else if (String.class.isAssignableFrom(type)) {
                valuePrompt = new SimplePrompt() {
                    @Override
                    protected String getPrompt(ConversationContext conversationContext) {
                        return enterValueMessage;
                    }

                    @Nullable
                    @Override
                    protected Prompt acceptValidatedInput(@NotNull ConversationContext conversationContext, @NotNull String s) {
                        preSetValue(object, f, s);

                        try {
                            f.set(object, s);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }

                        onSetValue(object, f, s);
                        return null;
                    }
                };
            } else if (XSound.class.isAssignableFrom(type)) {
                valuePrompt = new SimplePrompt() {
                    @Override
                    protected String getPrompt(ConversationContext conversationContext) {
                        return enterValueMessage;
                    }

                    @Nullable
                    @Override
                    protected Prompt acceptValidatedInput(@NotNull ConversationContext conversationContext, @NotNull String s) {
                        preSetValue(object, f, s);

                        try {
                            f.set(object, XSound.matchXSound(s).get());
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }

                        onSetValue(object, f, s);
                        return null;
                    }

                    @Override
                    protected boolean isInputValid(final ConversationContext context, final String input) {
                        return XSound.matchXSound(input).isPresent();
                    }

                    @Override
                    protected String getFailedValidationText(final ConversationContext context, final String invalidInput) {
                        return "&cPlease enter a valid sound";
                    }
                };
            } else if (XMaterial.class.isAssignableFrom(type)) {
                valuePrompt = new SimplePrompt() {
                    @Override
                    protected String getPrompt(ConversationContext conversationContext) {
                        return enterValueMessage;
                    }

                    @Nullable
                    @Override
                    protected Prompt acceptValidatedInput(@NotNull ConversationContext conversationContext, @NotNull String s) {
                        preSetValue(object, f, s);

                        try {
                            f.set(object, XMaterial.matchXMaterial(s).get());
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }

                        onSetValue(object, f, s);
                        return null;
                    }

                    @Override
                    protected boolean isInputValid(final ConversationContext context, final String input) {
                        return XMaterial.matchXMaterial(input).isPresent();
                    }

                    @Override
                    protected String getFailedValidationText(final ConversationContext context, final String invalidInput) {
                        return "&cPlease enter a valid material";
                    }
                };
            } else if (Message.class.isAssignableFrom(type)) {
                valuePrompt = new SimplePrompt() {
                    @Override
                    protected String getPrompt(ConversationContext conversationContext) {
                        return enterValueMessage;
                    }

                    @Nullable
                    @Override
                    protected Prompt acceptValidatedInput(@NotNull ConversationContext conversationContext, @NotNull String s) {
                        preSetValue(object, f, s);

                        try {
                            f.set(object, new Message(s));
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }

                        onSetValue(object, f, s);
                        return null;
                    }
                };
                // Collections of types
            } else if (List.class.isAssignableFrom(type)) {
                // Get the class type of the list. Messy reflection stuff
                Class<?> listType = (Class<?>) ((ParameterizedType) f.getGenericType()).getActualTypeArguments()[0];

                // For now only strings and item stacks since that's all we're dealing with
                // TODO: Add more when abstracting to library
                if (String.class.isAssignableFrom(listType)) {
                    value = TextUtil.listToString((List<?>) ReflectionUtil.getFieldContent(f, object));
                    listener = (player, ui, clickType, event) -> {
                        new ConfigureListUI(this, object, f).show(getViewer());
                    };
                } else if (ItemStack.class.isAssignableFrom(listType)) {
                    // TODO: Show sneak peek of items
                    value = "Click to view";
                    listener = (player, ui, clickType, event) -> {
                        new ConfigureItemsUI(this, object, f).show(getViewer());
                    };
                }
            }

            // Construct listener from prompt
            // Set listener for configuring UI based on type
            // Set final to pass to implementation
            final Prompt finalValuePrompt = valuePrompt;
            if (finalValuePrompt != null) {
                listener = (player, ui, clickType, event) -> {
                    new SimpleConversation() {
                        @Override
                        protected Prompt getFirstPrompt() {
                            return finalValuePrompt;
                        }
                    }.start(player);
                };
            }

            // Finally construct button
            Button button = Button.of(ItemCreator.builder()
                            .material(XMaterial.PAPER)
                            .name(new Message(Locale.Interface.OPTIONS_OPTION_NAME)
                                    .processPlaceholder("valueName", TextUtil.convertCamelCase(f.getName())).getMessage())
                            .lore(new Message(Locale.Interface.OPTIONS_OPTION_LORE)
                                    .processPlaceholder("value", value)
                                    .processPlaceholder("description", WordUtils.wrap(Locale.getMessage(valueDescriptionKey, valueDescriptionKey).getMessage(), 30)).getMessage())
                            .build(),
                    listener);
            options.add(button);
        }

        // TODO: Temp until fully abstracted to auto resize
        setSize(18);
    }

    /**
     * Map options to each slot
     */
    public ItemStack getItemAt(final int slot) {
        if (slot >= options.size())
            return ItemCreator.builder().name(" ").material(XMaterial.BLACK_STAINED_GLASS_PANE).build().makeUIItem();
        return options.get(slot) != null ? options.get(slot).getItem() : null;
    }

    /**
     * Called before the value is set for the field
     * To be implemented to provide functionality
     *
     * @param object The object that the value was set on
     * @param field The field being set
     * @param value The value set in {@link Object} form
     */
    protected void preSetValue(T object, Field field, Object value) {
    }

    /**
     * Called when the value is set for the field
     * To be implemented to provide functionality
     *
     * @param object The object that the value was set on
     * @param field The field being set
     * @param value The value set in {@link Object} form
     */
    protected void onSetValue(T object, Field field, Object value) {
    }

    /**
     * @return List of each button for each option
     */
    @Override
    protected List<Button> getButtonsToRegister() {
        return options;
    }

    /**
     * For when returning to update values
     */
    public UserInterface newInstance() {
        return new ConfigureOptionsUI<>(getParent(), object);
    }

    /**
     * Internal class for separate UI's.
     * For instance, dragging item stacks, string lists
     */
    private final class ConfigureItemsUI extends UserInterface {

        /**
         * The object to configure
         */
        public final T object;

        /**
         * Field we are setting
         * We know it's a {@link List} of {@link ItemStack}
         */
        public final Field field;

        /**
         * Items displayed in the menu
         */
        private final List<Button> items;

        /**
         * Actual list of items displayed
         */
        private final List<ItemStack> itemStacks;

        /**
         * @param parent Parent {@link UserInterface}
         * @param object The object instance to configure
         */
        public ConfigureItemsUI(UserInterface parent, T object, Field field) {
            super(parent, true);
            setTitle("&8Configure Items");
            setSize(54);

            this.items = new ArrayList<>();
            this.object = object;
            this.field = field;

            // Assign items
            this.itemStacks = (ArrayList<ItemStack>) ReflectionUtil.getFieldContent(field, object);
            loadItems();
        }

        /**
         * Load the items from the list into the interface
         */
        private void loadItems() {
            this.itemStacks.forEach(itemStack -> {
                // Finally construct button
                Button button = Button.of(ItemCreator.builder().item(itemStack)
                                .build(),
                        (player, ui, clickType, event) -> {
                        });
                items.add(button);
            });
        }

        protected void onInterfaceClick(final Player player, final int slot, final ItemStack clicked, final InventoryClickEvent event) {
            // Allow to place items in interface
        }

        protected void onItemPlace(final Player player, final int slot, final ItemStack placed, final InventoryClickEvent event) {
            // Allow to place items in interface
        }

        @Override
        protected void onButtonClick(final Player player, final int slot, final InventoryAction action,
                                     final ClickType click, final Button button, final InventoryClickEvent event) {
            // Allow to freely drag items
            button.getListener().onClickInInterface(player, this, click, event);
        }

        /**
         * Save all item contents into reward
         */
        protected void onInterfaceClose(final Player player, final Inventory inventory) {
            List<ItemStack> contents = new ArrayList<>();

            for (int i = 0; i < inventory.getSize(); i++) {
                // Don't save return button
                if (i == getReturnButtonPosition()) continue;

                ItemStack stack = inventory.getItem(i);

                if (stack == null || stack.getType() == Material.AIR) continue;

                contents.add(stack);
            }

            try {
                field.set(object, contents);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        /**
         * Map options to each slot
         */
        public ItemStack getItemAt(final int slot) {
            if (slot >= items.size())
                return null;
            return items.get(slot) != null ? items.get(slot).getItem() : null;
        }

        /**
         * @return List of each button for each item
         */
        @Override
        protected List<Button> getButtonsToRegister() {
            return items;
        }

    }

    /**
     * Allows us to configure a string list
     */
    private final class ConfigureListUI extends UserInterface {

        /**
         * The object to configure
         */
        public final T object;

        /**
         * Field we are setting
         * We know it's a {@link List} of {@link ItemStack}
         */
        public final Field field;

        /**
         * Display current values in the list
         */
        public final Button currentValues;

        /**
         * Clear all values from the list
         */
        public final Button clearValues;

        /**
         * Append a value to the end of the list
         */
        public final Button addValue;

        /**
         * @param parent Parent {@link UserInterface}
         * @param object The object instance to configure
         */
        public ConfigureListUI(UserInterface parent, T object, Field field) {
            super(parent, true);
            setTitle("&8Configure List");
            setSize(27);

            this.object = object;
            this.field = field;

            // Set here as we need to get field content
            currentValues = Button.makeIcon(ItemCreator.builder()
                    .material(XMaterial.PAPER)
                    .name(Locale.Interface.LIST_VALUES_NAME)
                    .lores((List<String>) ReflectionUtil.getFieldContent(field, object))
                    .build());

            clearValues = Button.of(ItemCreator.builder()
                            .material(XMaterial.RED_DYE)
                            .name(Locale.Interface.LIST_CLEAR_NAME)
                            .lore(Locale.Interface.LIST_CLEAR_LORE)
                            .build(),
                    (player, ui, clickType, event) -> {
                        // Clear list
                        try {
                            List<?> list = (List<?>) ReflectionUtil.getFieldContent(field, object);
                            list.clear();
                            field.set(object, list);

                            // Return as we cleared values
                            getParent().show(player);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    });

            addValue = Button.of(ItemCreator.builder()
                            .material(XMaterial.LIME_DYE)
                            .name(Locale.Interface.LIST_ADD_NAME)
                            .lore(Locale.Interface.LIST_ADD_LORE)
                            .build(),
                    (player, ui, clickType, event) -> {
                        // Attempt to add value
                        new SimpleConversation() {
                            @Override
                            protected Prompt getFirstPrompt() {
                                return new SimplePrompt() {
                                    @Override
                                    protected String getPrompt(ConversationContext conversationContext) {
                                        return Locale.Config.ENTER_VALUE;
                                    }

                                    @Nullable
                                    @Override
                                    protected Prompt acceptValidatedInput(@NotNull ConversationContext conversationContext, @NotNull String s) {
                                        try {
                                            List<String> list = (List<String>) ReflectionUtil.getFieldContent(field, object);

                                            // Weird things where contains one blank object so clear then add
                                            if (list.size() == 1 && list.get(0).trim().equalsIgnoreCase("")) {
                                                list.clear();
                                            }

                                            list.add(s);
                                            field.set(object, list);
                                        } catch (IllegalAccessException e) {
                                            e.printStackTrace();
                                        }

                                        // We have to recreate menu with updated object
                                        new ConfigureListUI(getParent(), object, field).show(getViewer());
                                        return null;
                                    }
                                };
                            }
                        }.start(player);
                    });
        }

        @Override
        public ItemStack getItemAt(final int slot) {
            if (slot == 11) {
                return currentValues.getItem();
            } else if (slot == 13) {
                return clearValues.getItem();
            } else if (slot == 15) {
                return addValue.getItem();
            }

            // Else placeholder item
            return ItemCreator.builder().name(" ").material(XMaterial.BLACK_STAINED_GLASS_PANE).build().makeUIItem();
        }

    }

}
