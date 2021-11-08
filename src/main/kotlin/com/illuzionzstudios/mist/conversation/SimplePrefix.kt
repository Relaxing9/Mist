package com.illuzionzstudios.mist.conversation

import com.illuzionzstudios.mist.util.TextUtil
import com.illuzionzstudios.mist.util.TextUtil.formatText
import lombok.*
import org.bukkit.conversations.ConversationContext
import org.bukkit.conversations.ConversationPrefix

/**
 * A simple conversation prefix with a static string
 */
@RequiredArgsConstructor
class SimplePrefix : ConversationPrefix {
    /**
     * The conversation prefix
     */
    @Getter
    private val prefix: String? = null
    override fun getPrefix(context: ConversationContext): String {
        return TextUtil.formatText(prefix)
    }
}