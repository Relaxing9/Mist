package com.illuzionzstudios.mist.util;

import com.illuzionzstudios.mist.compatibility.ServerVersion;
import lombok.experimental.UtilityClass;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang.StringEscapeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Util methods to help parsing text
 */
@UtilityClass
public class TextUtil {

    /**
     * Returns a long ------ smooth console line
     */
    public final String SMOOTH_LINE = org.bukkit.ChatColor.STRIKETHROUGH + "                                                               ";

    /**
     * Hexadecimal pattern
     */
    private final Pattern hexPattern = Pattern.compile("<#([A-Fa-f0-9]){6}>");

    /**
     * Master method to format text. This will run formatting like
     * capitalizing, translating colour codes, etc.
     *
     * @param text {@link String} to format
     * @return The formatted {@link String}
     */
    public String formatText(String text) {
        return formatText(text, false);
    }

    public List<String> formatText(List<String> text) {
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
    public String formatText(String text, boolean capitalize) {
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
     * This will turn camelCase into PascalCase.
     * For instance vanillaRewards becomes Vanilla Rewards
     *
     * @param text camelCase text
     * @return PascalCase text
     */
    public String convertCamelCase(String text) {
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
    public int getOffset(String s) {
        char[] chars = s.toCharArray();
        for (int i = 0; i < chars.length; ++i) {
            if (chars[i] != ' ') {
                return i;
            }
        }
        return -1;
    }

    public String replaceLast(String string, String toReplace, String replacement) {
        int index = string.lastIndexOf(toReplace);
        if (index == -1) {
            return string;
        }
        return string.substring(0, index) + replacement + string.substring(index + toReplace.length());
    }

    public String getFormattedTime(long millis, boolean verbose) {
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
