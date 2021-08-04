package com.illuzionzstudios.mist.conversation;

import com.illuzionzstudios.mist.util.TextUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationPrefix;

/**
 * A simple conversation prefix with a static string
 */
@RequiredArgsConstructor
public final class SimplePrefix implements ConversationPrefix {

    /**
     * The conversation prefix
     */
    @Getter
    private final String prefix;

    @Override
    public String getPrefix(ConversationContext context) {
        return TextUtil.formatText(prefix);
    }
}

