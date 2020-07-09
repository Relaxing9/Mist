/**
 * Copyright © 2020 Property of Illuzionz Studios, LLC
 * All rights reserved. No part of this publication may be reproduced, distributed, or
 * transmitted in any form or by any means, including photocopying, recording, or other
 * electronic or mechanical methods, without the prior written permission of the publisher,
 * except in the case of brief quotations embodied in critical reviews and certain other
 * noncommercial uses permitted by copyright law. Any licensing of this software overrides
 * this statement.
 */
package com.illuzionzstudios.mist.conversation;

import com.illuzionzstudios.mist.Mist;
import com.illuzionzstudios.mist.compatibility.XSound;
import com.illuzionzstudios.mist.config.locale.Locale;
import com.illuzionzstudios.mist.plugin.SpigotPlugin;
import com.illuzionzstudios.mist.scheduler.MinecraftScheduler;
import com.illuzionzstudios.mist.ui.UserInterface;
import com.illuzionzstudios.mist.util.Valid;
import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.conversations.*;
import org.bukkit.entity.Player;

import java.awt.*;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * A simple way to communicate with the player
 * - their chat will be isolated and they chat messages processed and
 * the conversation input.
 */
public abstract class SimpleConversation implements ConversationAbandonedListener {

    /**
     * How often should we show the question in the prompt again, in seconds?
     */
    private static final int QUESTION_SHOW_THRESHOLD = 20;

    /**
     * The menu to return to, if any
     */
    private UserInterface menuToReturnTo;

    /**
     * Creates a simple conversation
     */
    protected SimpleConversation() {
        this(null);
    }

    /**
     * Creates a simple conversation that opens the
     * menu when finished
     *
     * @param menuToReturnTo
     */
    protected SimpleConversation(final UserInterface menuToReturnTo) {
        this.menuToReturnTo = menuToReturnTo;
    }

    /**
     * Start a conversation with the player, throwing error if {@link Player#isConversing()}
     *
     * @param player
     */
    public final void start(final Player player) {
        Valid.checkBoolean(!player.isConversing(), "Player " + player.getName() + " is already conversing!");

        // Do not allow open inventory since they cannot type anyways
        player.closeInventory();

        // Setup
        final CustomConversation conversation = new CustomConversation(player);

        final InactivityConversationCanceller inactivityCanceller = new InactivityConversationCanceller(SpigotPlugin.getInstance(), 45);
        inactivityCanceller.setConversation(conversation);

        conversation.getCancellers().add(inactivityCanceller);
        conversation.getCancellers().add(getCanceller());

        conversation.addConversationAbandonedListener(this);

        conversation.begin();
    }

    /**
     * Get the first prompt in this conversation for the player
     *
     * @return
     */
    protected abstract Prompt getFirstPrompt();

    /**
     * Listen for and handle existing the conversation
     */
    @Override
    public final void conversationAbandoned(final ConversationAbandonedEvent event) {
        final Conversable conversing = event.getContext().getForWhom();
        final Object source = event.getSource();

        if (source instanceof CustomConversation) {
            final SimplePrompt lastPrompt = ((CustomConversation) source).getLastSimplePrompt();

            if (lastPrompt != null)
                lastPrompt.onConversationEnd(this, event);
        }

        onConversationEnd(event);

        if (conversing instanceof Player) {
            final Player player = (Player) conversing;

            (event.gracefulExit() ? XSound.BLOCK_NOTE_BLOCK_PLING : XSound.BLOCK_NOTE_BLOCK_BASS).play(player, 1F, 1F);

            if (menuToReturnTo != null && reopenMenu())
                menuToReturnTo.newInstance().show(player);
        }
    }

    /**
     * Fired when the user quits this conversation (see {@link #getCanceller()}, or
     * simply quits the game)
     *
     * @param event
     */
    protected void onConversationEnd(final ConversationAbandonedEvent event) {
    }

    /**
     * Get conversation prefix before each message
     *
     * By default we use the plugins tell prefix
     *
     * TIP: You can use {@link SimplePrefix}
     *
     * @return
     */
    protected ConversationPrefix getPrefix() {
        return new SimplePrefix(Locale.General.PLUGIN_PREFIX + " ");
    }

    private final String addLastSpace(final String prefix) {
        return prefix.endsWith(" ") ? prefix : prefix + " ";
    }

    /**
     * Return the canceller that listens for certain words to exit the convo,
     * by default we use {@link SimpleCanceller} that listens to quit|cancel|exit
     *
     * @return
     */
    protected ConversationCanceller getCanceller() {
        return new SimpleCanceller("quit", "cancel", "exit");
    }

    /**
     * Return true if we should insert a prefix before each message, see {@link #getPrefix()}
     *
     * @return
     */
    protected boolean insertPrefix() {
        return true;
    }

    /**
     * If we detect the player has a menu opened should we reopen it?
     *
     * @return
     */
    protected boolean reopenMenu() {
        return true;
    }

    /**
     * Get the timeout in seconds before automatically exiting the convo
     *
     * @return
     */
    protected int getTimeout() {
        return 60;
    }

    /**
     * Sets the menu to return to after the end of this conversation
     *
     * @param menu
     */
    public void setMenuToReturnTo(final UserInterface menu) {
        this.menuToReturnTo = menu;
    }
    // ------------------------------------------------------------------------------------------------------------
    // Static access
    // ------------------------------------------------------------------------------------------------------------

    /**
     * Shortcut method for direct message send to the player
     *
     * @param conversable
     * @param message
     */
    protected static final void tell(final Conversable conversable, final String message) {
        Mist.tellConversing(conversable, message);
    }

    /**
     * Send a message to the conversable player later
     *
     * @param delayTicks
     * @param conversable
     * @param message
     */
    protected static final void tellLater(final Conversable conversable, final String message, final int delayTicks) {
        Mist.tellLaterConversing(conversable, message, delayTicks);
    }

    /**
     * Custom conversation class used for only showing the question once per 20 seconds interval
     *
     */
    private final class CustomConversation extends Conversation {

        /**
         * Holds the information about the last prompt, used to invoke onConversationEnd
         */
        @Getter(value = AccessLevel.PRIVATE)
        private SimplePrompt lastSimplePrompt;

        private CustomConversation(final Conversable forWhom) {
            super(SpigotPlugin.getInstance(), forWhom, SimpleConversation.this.getFirstPrompt());

            localEchoEnabled = false;

            if (insertPrefix() && SimpleConversation.this.getPrefix() != null)
                prefix = SimpleConversation.this.getPrefix();
        }

        @Override
        public void outputNextPrompt() {
            if (currentPrompt == null)
                abandon(new ConversationAbandonedEvent(this));
            else {
                // Edit start

                // Edit 1 - save the time when we showed the question to the player
                // so that we only show it once per the given threshold
                final String promptClass = currentPrompt.getClass().getSimpleName();

                final String question = currentPrompt.getPromptText(context);

                try {
                    final HashMap<String, Void /*dont have expiring set class*/> askedQuestions = (HashMap<String, Void>) context.getAllSessionData()
                            .getOrDefault("Asked_" + promptClass, new HashMap<>());

                    if (!askedQuestions.containsKey(question)) {
                        askedQuestions.put(question, null);

                        context.setSessionData("Asked_" + promptClass, askedQuestions);
                        context.getForWhom().sendRawMessage(prefix.getPrefix(context) + question);
                    }
                } catch (final NoSuchMethodError ex) {
                    // Unfortunatelly old MC version detected
                }

                // Edit 2 - Save last prompt if it is our class
                if (currentPrompt instanceof SimplePrompt)
                    lastSimplePrompt = ((SimplePrompt) currentPrompt).clone();

                // Edit end
                if (!currentPrompt.blocksForInput(context)) {
                    currentPrompt = currentPrompt.acceptInput(context, null);
                    outputNextPrompt();
                }
            }
        }
    }
}

