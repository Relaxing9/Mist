package com.illuzionzstudios.mist.util

import com.illuzionzstudios.mist.compatibility.ServerVersion
import org.apache.commons.lang.StringEscapeUtils
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.function.Consumer
import java.util.regex.Pattern


/**
 * Util methods to help parsing text
 */
class TextUtil {

    companion object {

        /**
         * Returns a long ------ smooth console line
         */
        @JvmField
        val SMOOTH_LINE =
            ChatColor.STRIKETHROUGH.toString() + "                                                               "

        /**
         * Hexadecimal pattern
         */
        private val hexPattern = Pattern.compile("<#([A-Fa-f0-9]){6}>")
        fun formatText(text: List<String?>): List<String> {
            val formatted: MutableList<String> = ArrayList()
            text.forEach(Consumer { str: String? -> formatted.add(formatText(str)) })
            return formatted
        }
        /**
         * See [.formatText]
         *
         *
         * Capitalize the text and set colours
         */
        /**
         * Master method to format text. This will run formatting like
         * capitalizing, translating colour codes, etc.
         *
         * @param text [String] to format
         * @return The formatted [String]
         */
        @JvmOverloads
        fun formatText(text: String?, capitalize: Boolean = false): String {
            var text = text
            if (text == null || text == "") return ""
            if (capitalize) text = text.substring(0, 1).uppercase(Locale.getDefault()) + text.substring(1)

            // Parse unicode
            text = StringEscapeUtils.unescapeJava(text)

            // Parse hexadecimal as #FAFAFA<message>
            if (ServerVersion.atLeast(ServerVersion.V.v1_16)) {
                var matcher = hexPattern.matcher(text)
                while (matcher.find()) {
                    val hexColor =
                        net.md_5.bungee.api.ChatColor.of(matcher.group().substring(1, matcher.group().length - 1))
                    val before = text!!.substring(0, matcher.start())
                    val after = text.substring(matcher.end())
                    text = before + hexColor + after
                    matcher = hexPattern.matcher(text)
                }
            }
            return net.md_5.bungee.api.ChatColor.translateAlternateColorCodes('&', text)
        }

        /**
         * This will turn camelCase into PascalCase.
         * For instance vanillaRewards becomes Vanilla Rewards
         *
         * @param text camelCase text
         * @return PascalCase text
         */
        fun convertCamelCase(text: String): String {
            return formatText(
                text.replace(
                    String.format(
                        "%s|%s|%s",
                        "(?<=[A-Z])(?=[A-Z][a-z])",
                        "(?<=[^A-Z])(?=[A-Z])",
                        "(?<=[A-Za-z])(?=[^A-Za-z])"
                    ).toRegex(),
                    " "
                ), true
            )
        }

        /**
         * Util method to get amount of ' ' chars in [String] before the first non-space char
         */
        fun getOffset(s: String): Int {
            val chars = s.toCharArray()
            for (i in chars.indices) {
                if (chars[i] != ' ') {
                    return i
                }
            }
            return -1
        }

        fun replaceLast(string: String, toReplace: String, replacement: String): String {
            val index = string.lastIndexOf(toReplace)
            return if (index == -1) {
                string
            } else string.substring(0, index) + replacement + string.substring(index + toReplace.length)
        }

        fun getFormattedTime(millis: Long, verbose: Boolean): String {
            var millis = millis
            require(millis >= 0) { "Duration must be greater than zero!" }
            val daysLeft = TimeUnit.MILLISECONDS.toDays(millis)
            millis -= TimeUnit.DAYS.toMillis(daysLeft)
            val hoursLeft = TimeUnit.MILLISECONDS.toHours(millis)
            millis -= TimeUnit.HOURS.toMillis(hoursLeft)
            val minutesLeft = TimeUnit.MILLISECONDS.toMinutes(millis)
            millis -= TimeUnit.MINUTES.toMillis(minutesLeft)
            val secondsLeft = TimeUnit.MILLISECONDS.toSeconds(millis)
            val message = StringBuilder()
            if (daysLeft != 0L) {
                message.append(daysLeft)
                message.append(if (verbose) " days" else "d")
            }
            if (hoursLeft != 0L) {
                if (message.isNotEmpty()) {
                    message.append(if (verbose) ", " else " ")
                }
                message.append(hoursLeft)
                message.append(if (verbose) " hour" else "h")
                if (verbose && hoursLeft > 1) {
                    message.append("s")
                }
            }
            if (minutesLeft != 0L) {
                if (message.isNotEmpty()) {
                    message.append(if (verbose) ", " else " ")
                }
                message.append(minutesLeft)
                message.append(if (verbose) " minute" else "m")
                if (verbose && minutesLeft > 1) {
                    message.append("s")
                }
            }

            //Only display seconds if waittime is <1 hr
            if (secondsLeft != 0L && hoursLeft == 0L && daysLeft == 0L) {
                if (message.isNotEmpty()) {
                    message.append(if (verbose) ", " else " ")
                }
                message.append(secondsLeft)
                message.append(if (verbose) " second" else "s")
                if (verbose && secondsLeft > 1) {
                    message.append("s")
                }
            }

            //Only display seconds if waittime is <1 sec
            if (hoursLeft == 0L && minutesLeft == 0L && secondsLeft == 0L && millis > 0) {
                if (message.isNotEmpty()) {
                    message.append(if (verbose) ", " else " ")
                }
                message.append(millis)
                message.append(if (verbose) " millis" else "ms")
            }
            var formatted = message.toString()
            if (verbose) {
                formatted = replaceLast(formatted, ", ", " and ")
            }
            return formatted
        }

        /**
         * Makes the string centered with padding based on the max width. Default is max width for chat
         */
        @JvmOverloads
        fun getCenteredString(message: String?, maxWidth: Int = 154): String {
            var message = message
            if (message == null || message == "") return ""

            message = formatText(message)
            var messagePxSize = 0
            var previousCode = false
            var isBold = false
            for (c in message.toCharArray()) {
                if (c == '§') {
                    previousCode = true
                    continue
                } else if (previousCode) {
                    previousCode = false
                    if (c == 'l' || c == 'L') {
                        isBold = true
                        continue
                    } else isBold = false
                } else {
                    val dFI = DefaultFontInfo.getDefaultFontInfo(c)
                    messagePxSize += if (isBold) dFI.getBoldLength() else dFI.length
                    messagePxSize++
                }
            }
            val halvedMessageSize = messagePxSize / 2
            val toCompensate: Int = (maxWidth * 2) - halvedMessageSize
            val spaceLength = DefaultFontInfo.SPACE.length + 1
            var compensated = 0
            val sb = StringBuilder()
            while (compensated < toCompensate) {
                sb.append(" ")
                compensated += spaceLength
            }
            return sb.toString() + message
        }
    }

    /**
     * Length about certain characters
     */
    enum class DefaultFontInfo(val char: Char, val length: Int) {
        A('A', 5),
        a('a', 5),
        B('B', 5),
        b('b', 5),
        C('C', 5),
        c('c', 5),
        D('D', 5),
        d('d', 5),
        E('E', 5),
        e('e', 5),
        F('F', 5),
        f('f', 4),
        G('G', 5),
        g('g', 5),
        H('H', 5),
        h('h', 5),
        I('I', 3),
        i('i', 1),
        J('J', 5),
        j('j', 5),
        K('K', 5),
        k('k', 4),
        L('L', 5),
        l('l', 1),
        M('M', 5),
        m('m', 5),
        N('N', 5),
        n('n', 5),
        O('O', 5),
        o('o', 5),
        P('P', 5),
        p('p', 5),
        Q('Q', 5),
        q('q', 5),
        R('R', 5),
        r('r', 5),
        S('S', 5),
        s('s', 5),
        T('T', 5),
        t('t', 4),
        U('U', 5),
        u('u', 5),
        V('V', 5),
        v('v', 5),
        W('W', 5),
        w('w', 5),
        X('X', 5),
        x('x', 5),
        Y('Y', 5),
        y('y', 5),
        Z('Z', 5),
        z('z', 5),
        NUM_1('1', 5),
        NUM_2('2', 5),
        NUM_3('3', 5),
        NUM_4('4', 5),
        NUM_5('5', 5),
        NUM_6('6', 5),
        NUM_7('7', 5),
        NUM_8('8', 5),
        NUM_9('9', 5),
        NUM_0('0', 5),
        EXCLAMATION_POINT('!', 1),
        AT_SYMBOL('@', 6),
        NUM_SIGN('#', 5),
        DOLLAR_SIGN('$', 5),
        PERCENT('%', 5),
        UP_ARROW('^', 5),
        AMPERSAND('&', 5),
        ASTERISK('*', 5),
        LEFT_PARENTHESIS('(', 4),
        RIGHT_PERENTHESIS(')', 4),
        MINUS('-', 5),
        UNDERSCORE('_', 5),
        PLUS_SIGN('+', 5),
        EQUALS_SIGN('=', 5),
        LEFT_CURL_BRACE('{', 4),
        RIGHT_CURL_BRACE('}', 4),
        LEFT_BRACKET('[', 3),
        RIGHT_BRACKET(']', 3),
        COLON(':', 1),
        SEMI_COLON(';', 1),
        DOUBLE_QUOTE('"', 3),
        SINGLE_QUOTE('\'', 1),
        LEFT_ARROW('<', 4),
        RIGHT_ARROW('>', 4),
        QUESTION_MARK('?', 5),
        SLASH('/', 5),
        BACK_SLASH('\\', 5),
        LINE('|', 1),
        TILDE('~', 5),
        TICK('`', 2),
        PERIOD('.', 1),
        COMMA(',', 1),
        SPACE(' ', 3),
        DEFAULT('a', 4);

        fun getBoldLength(): Int {
            return if (this === SPACE) length else length + 1
        }

        companion object {
            fun getDefaultFontInfo(c: Char): DefaultFontInfo {
                for (dFI in values()) {
                    if (dFI.char == c) return dFI
                }
                return DEFAULT
            }
        }
    }
}