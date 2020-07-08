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

import com.illuzionzstudios.mist.util.Valid;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationCanceller;
import org.bukkit.conversations.ConversationContext;

import java.util.Arrays;
import java.util.List;

/**
 * A simple conversation canceller
 * If the players message matches any word in the list, his conversation is cancelled
 */
public final class SimpleCanceller implements ConversationCanceller {

    /**
     * The words that trigger the conversation cancellation
     */
    private final List<String> cancelPhrases;

    /**
     * Create a new convo canceler based off the given strings
     * If the players message matches any word in the list, his conversation is cancelled
     *
     * @param cancelPhrases
     */
    public SimpleCanceller(String... cancelPhrases) {
        this(Arrays.asList(cancelPhrases));
    }

    /**
     * Create a new convo canceler from the given lists
     * If the players message matches any word in the list, his conversation is cancelled
     *
     * @param cancelPhrases
     */
    public SimpleCanceller(List<String> cancelPhrases) {
        Valid.checkBoolean(!cancelPhrases.isEmpty(), "Cancel phrases are empty for conversation cancel listener!");

        this.cancelPhrases = cancelPhrases;
    }

    @Override
    public void setConversation(Conversation conversation) {
    }

    /**
     * Listen to cancel phrases and exit if they equals
     */
    @Override
    public boolean cancelBasedOnInput(ConversationContext context, String input) {
        for (final String phrase : cancelPhrases)
            if (input.equalsIgnoreCase(phrase))
                return true;

        return false;
    }

    @Override
    public ConversationCanceller clone() {
        return new SimpleCanceller(cancelPhrases);
    }
}

