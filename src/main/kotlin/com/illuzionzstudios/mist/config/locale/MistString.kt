package com.illuzionzstudios.mist.config.locale

import com.illuzionzstudios.mist.compatibility.ServerVersion
import com.illuzionzstudios.mist.compatibility.ServerVersion.V
import com.illuzionzstudios.mist.util.TextUtil
import com.illuzionzstudios.mist.util.TextUtil.formatText
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*
import java.util.function.Consumer
import java.util.regex.Matcher

/**
 * This represents a custom translatable string that can be used in the plugin where we
 * would usually use a [String]. It is based of plugin translation files and includes
 * utils for formatting and replacing parts of the string.
 *
 *
 * Contains full text engine for manipulating text
 */
class MistString(
    /**
     * The key of this string for the locale
     */
    private val key: String,
    /**
     * The raw contents of this string
     */
    private var value: String
) {
    companion object {
        /**
         * If can use the action bar
         */
        private var canActionBar = false

        /**
         * Construct a [MistString] from single string
         */
        fun of(string: String): MistString {
            // Faster than iterate list of 1 item
            return MistString(string)
        }

        /**
         * Construct a [MistString] from multi strings
         */
        fun of(vararg strings: String?): MistString {
            val builder = StringBuilder()
            for (i in 0 until strings.size) {
                builder.append(strings[i])

                // Can't compare values have to compare index
                if (i != strings.size - 1) builder.append("\n")
            }
            return MistString(builder.toString())
        }

        /**
         * Converts a list of strings to one mist string
         *
         * @param list The list of strings to convert
         * @return One [MistString]
         */
        fun of(list: List<String>): MistString {
            return of(*list.toTypedArray())
        }

        /**
         * Easily turn a list of strings into a list of [MistString]
         *
         * @param list The list to convert
         * @return The list of [MistString] with the original list's values
         */
        fun fromStringList(list: List<String>): List<MistString> {
            val strings = ArrayList<MistString>()
            list.forEach(Consumer { string: String -> strings.add(MistString(string)) })
            return strings
        }

        /**
         * Easily turn a list of [MistString] into a list of strings
         *
         * @param list The list to convert
         * @return The list of string with the original list's values
         */
        fun fromList(list: List<MistString>): List<String> {
            val strings = ArrayList<String>()
            list.forEach(Consumer { string: MistString -> strings.add(string.toString()) })
            return strings
        }

        init {
            try {
                Class.forName("net.md_5.bungee.api.ChatMessageType")
                Class.forName("net.md_5.bungee.api.chat.TextComponent")
                Player.Spigot::class.java.getDeclaredMethod(
                    "sendMessage",
                    ChatMessageType::class.java,
                    TextComponent::class.java
                )
                canActionBar = true
            } catch (ignored: Exception) {
            }
        }
    }

    /**
     * Default value of the string
     */
    private val def: String = ""

    /**
     * If it has been loaded from the locale
     */
    private var loaded = false

    /**
     * Used when we just want a string with content and not
     * to be localised
     *
     * @param def The value of the string
     */
    constructor(def: String) : this("", def)

    /**
     * @param other Create string from another
     */
    constructor(other: MistString) : this(other.key, other.value)

    /**
     * Concat another string to this string
     *
     * @param other String to add
     * @return String with other added on
     */
    fun concat(other: String): MistString {
        value += other
        return this
    }

    /**
     * See [.concat]
     */
    fun concat(other: MistString): MistString {
        concat(other.toString())
        return this
    }

    /**
     * Replace the provided placeholder with the provided object. <br></br>
     * Supports `{value}` placeholders
     *
     * @param placeholder the placeholder to replace
     * @param replacement the replacement object
     * @return the modified Message
     */
    fun toString(placeholder: String?, replacement: Any?): MistString {
        loadString()
        val place = Matcher.quoteReplacement(placeholder)
        value = value.replace(
            "\\{" + place + "}".toRegex(),
            if (replacement == null) "" else Matcher.quoteReplacement(replacement.toString())
        )
        return this
    }

    /**
     * Replace everything in the string according to this replacement map.
     *
     * @param replacements The map of replacements
     * @return the modified Message
     */
    fun toString(replacements: Map<String?, Any?>): MistString {
        replacements.forEach { (placeholder: String?, replacement: Any?) -> this.toString(placeholder, replacement) }
        return this
    }

    /**
     * After turning into final string we must reset the value as
     * otherwise things like concat will continue for every call
     */
    override fun toString(): String {
        val toSend = value
        // Reload value
        loaded = false
        loadString()
        return TextUtil.formatText(toSend)
    }

    /**
     * Returns a list of strings made from this string
     */
    fun toStringList(): List<String> {
        return listOf(*toString().split("\\r?\\n".toRegex()).toTypedArray())
    }

    /**
     * Loads string from locale into value. Must be used before replacing
     */
    fun loadString() {
        if (!loaded) {
            value = TextUtil.formatText(PluginLocale.Companion.getMessage(key, value))
            loaded = true
        }
    }

    /**
     * Format and send the held message to a player.
     * Detect if string is split and send multiple lines
     *
     * @param player player to send the message to
     */
    fun sendMessage(player: CommandSender?) {
        // Check for split lore
        val strings = toString().split("\\n".toRegex()).toTypedArray()
        player?.sendMessage(*strings)
    }

    /**
     * Format and send the held message to a player as a title message
     *
     * @param sender   command sender to send the message to
     * @param subtitle Subtitle to send
     */
    /**
     * Format and send the held message to a player as a title message
     *
     * @param sender command sender to send the message to
     */
    @JvmOverloads
    fun sendTitle(sender: CommandSender?, subtitle: MistString = MistString("")) {
        if (sender is Player) {
            if (ServerVersion.atLeast(V.v1_11)) {
                sender.sendTitle(toString(), subtitle.toString(), 10, 20, 10)
            } else {
                sender.sendTitle(toString(), subtitle.toString())
            }
        } else {
            sendMessage(sender)
        }
    }

    /**
     * Format and send the held message to a player as an actionbar message
     *
     * @param sender command sender to send the message to
     */
    fun sendActionBar(sender: CommandSender?) {
        if (sender !is Player) {
            sender?.sendMessage(toString())
        } else if (!canActionBar) {
            sendTitle(sender)
        } else {
            sender.spigot().sendMessage(
                ChatMessageType.ACTION_BAR,
                TextComponent(toString())
            )
        }
    }
}