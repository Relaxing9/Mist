package com.illuzionzstudios.mist.util;

import com.illuzionzstudios.mist.compatibility.ServerVersion;
import com.illuzionzstudios.mist.config.locale.MistString;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang.StringEscapeUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Util methods to help parsing text
 */
public final class TextUtil {

    /**
     * Returns a long ------ smooth console line
     */
    public static final String SMOOTH_LINE = org.bukkit.ChatColor.STRIKETHROUGH + "                                                               ";

    /**
     * Hexadecimal pattern
     */
    private static final Pattern hexPattern = Pattern.compile("<#([A-Fa-f0-9]){6}>");

    /**
     * These are all the {@link Charset} we support for encoding/saving
     */
    private static final List<Charset> supportedCharsets = new ArrayList<>();

    static {
        supportedCharsets.add(StandardCharsets.UTF_8); // UTF-8 BOM: EF BB BF
        supportedCharsets.add(StandardCharsets.ISO_8859_1); // also starts with EF BB BF
        supportedCharsets.add(StandardCharsets.UTF_16LE); // FF FE
        supportedCharsets.add(StandardCharsets.UTF_16BE); // FE FF
        supportedCharsets.add(StandardCharsets.UTF_16);
        try {
            supportedCharsets.add(Charset.forName("windows-1253"));
            supportedCharsets.add(Charset.forName("ISO-8859-7"));
        } catch (Exception ignored) {
        } // UnsupportedCharsetException technically can be thrown, but can also be ignored

        supportedCharsets.add(StandardCharsets.US_ASCII);
    }

    /**
     * Master method to format text. This will run formatting like
     * capitalizing, translating colour codes, etc.
     *
     * @param text {@link String} to format
     * @return The formatted {@link String}
     */
    public static String formatText(String text) {
        return formatText(text, false);
    }

    public static List<String> formatText(List<String> text) {
        List<String> formatted = new ArrayList<>();
        text.forEach(str -> {
            formatted.add(formatText(str));
        });
        return formatted;
    }

    /**
     * See {@link #formatText(String)}
     * <p>
     * Capitalize the text and set colours
     */
    public static String formatText(String text, boolean capitalize) {
        if (text == null || text.equals(""))
            return "";
        if (capitalize)
            text = text.substring(0, 1).toUpperCase() + text.substring(1);

        // Parse unicode
        text = StringEscapeUtils.unescapeJava(text);

        // Parse hexadecimal as #FAFAFA<message>
        if (ServerVersion.atLeast(ServerVersion.V.v1_16)) {
            Matcher matcher = hexPattern.matcher(text);
            while (matcher.find()) {
                final ChatColor hexColor = ChatColor.of(matcher.group().substring(1, matcher.group().length() - 1));
                final String before = text.substring(0, matcher.start());
                final String after = text.substring(matcher.end());
                text = before + hexColor + after;
                matcher = hexPattern.matcher(text);
            }
        }

        return ChatColor.translateAlternateColorCodes('&', text);
    }

    /**
     * Converts list of objects into a string separated by \n char.
     * Takes the toString() form.
     * For instance list of objects "one" and "two" becomes
     * "one\ntwo"
     *
     * @param list List to convert
     * @return As singular string
     * @deprecated See {@link MistString#of#toString()}
     */
    @Deprecated
    public static String listToString(List<?> list) {
        StringBuilder builder = new StringBuilder();

        list.forEach(object -> {
            builder.append(object.toString());

            // Last element check
            if (!list.get(list.size() - 1).equals(object)) {
                builder.append("\n");
            }
        });

        return builder.toString();
    }

    /**
     * This will turn camelCase into PascalCase.
     * For instance vanillaRewards becomes Vanilla Rewards
     *
     * @param text camelCase text
     * @return PascalCase text
     */
    public static String convertCamelCase(String text) {
        return formatText(text.replaceAll(
                String.format("%s|%s|%s",
                        "(?<=[A-Z])(?=[A-Z][a-z])",
                        "(?<=[^A-Z])(?=[A-Z])",
                        "(?<=[A-Za-z])(?=[^A-Za-z])"
                ),
                " "
        ), true);
    }

    /**
     * Util method to get amount of ' ' chars in {@link String} before the first non-space char
     */
    public static int getOffset(String s) {
        char[] chars = s.toCharArray();
        for (int i = 0; i < chars.length; ++i) {
            if (chars[i] != ' ') {
                return i;
            }
        }
        return -1;
    }

    public static String replaceLast(String string, String toReplace, String replacement) {
        int index = string.lastIndexOf(toReplace);
        if (index == -1) {
            return string;
        }
        return string.substring(0, index) + replacement + string.substring(index + toReplace.length());
    }

    public static String getFormattedTime(long millis, boolean verbose) {
        if (millis < 0) {
            throw new IllegalArgumentException("Duration must be greater than zero!");
        }

        long daysLeft = TimeUnit.MILLISECONDS.toDays(millis);
        millis = millis - TimeUnit.DAYS.toMillis(daysLeft);
        long hoursLeft = TimeUnit.MILLISECONDS.toHours(millis);
        millis = millis - TimeUnit.HOURS.toMillis(hoursLeft);
        long minutesLeft = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis = millis - TimeUnit.MINUTES.toMillis(minutesLeft);
        long secondsLeft = TimeUnit.MILLISECONDS.toSeconds(millis);

        StringBuilder message = new StringBuilder();
        if (daysLeft != 0) {
            message.append(daysLeft);
            message.append((verbose ? " days" : "d"));
        }

        if (hoursLeft != 0) {
            if (message.length() != 0) {
                message.append((verbose ? ", " : " "));
            }
            message.append(hoursLeft);
            message.append((verbose ? " hour" : "h"));
            if (verbose && hoursLeft > 1) {
                message.append("s");
            }
        }

        if (minutesLeft != 0) {
            if (message.length() != 0) {
                message.append((verbose ? ", " : " "));
            }
            message.append(minutesLeft);
            message.append((verbose ? " minute" : "m"));
            if (verbose && minutesLeft > 1) {
                message.append("s");
            }
        }

        //Only display seconds if waittime is <1 hr
        if (secondsLeft != 0 && hoursLeft == 0 && daysLeft == 0) {
            if (message.length() != 0) {
                message.append((verbose ? ", " : " "));
            }
            message.append(secondsLeft);
            message.append((verbose ? " second" : "s"));
            if (verbose && secondsLeft > 1) {
                message.append("s");
            }
        }

        //Only display seconds if waittime is <1 sec
        if (hoursLeft == 0 && minutesLeft == 0 && secondsLeft == 0 && millis > 0) {
            if (message.length() != 0) {
                message.append((verbose ? ", " : " "));
            }
            message.append(millis);
            message.append((verbose ? " millis" : "ms"));
        }
        String formatted = message.toString();
        if (verbose) {
            formatted = TextUtil.replaceLast(formatted, ", ", " and ");
        }
        return formatted;
    }

}
