package com.illuzionzstudios.mist.util;

import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang.StringEscapeUtils;

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

/**
 * Util methods to help parsing text
 */
public final class TextUtil {

    /**
     * These are all the {@link Charset} we support for encoding/saving
     */
    protected static final List<Charset> supportedCharsets = new ArrayList<>();

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

    /**
     * See {@link #formatText(String)}
     *
     * Capitalize the text and set colours
     */
    public static String formatText(String text, boolean capitalize) {
        if (text == null || text.equals(""))
            return "";
        if (capitalize)
            text = text.substring(0, 1).toUpperCase() + text.substring(1);

        // Parse unicode
        text = StringEscapeUtils.unescapeJava(text);

        return ChatColor.translateAlternateColorCodes('&', text);
    }

    /**
     * Converts list of objects into a string separated by \n char.
     * Takes the toString() form.
     * For instance list of objects "one" and "two" becomes
     * "one \ntwo"
     *
     * @param list List to convert
     * @return As singular string
     */
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
     * Convert a string to an invisible colored string that's lore-safe <br />
     * (Safe to use as lore) <br />
     * Note: Do not use semi-colons in this string, or they will be lost when decoding!
     *
     * @param s string to convert
     * @return encoded string
     */
    public static String convertToInvisibleLoreString(String s) {
        if (s == null || s.equals(""))
            return "";
        StringBuilder hidden = new StringBuilder();
        for (char c : s.toCharArray())
            hidden.append(ChatColor.COLOR_CHAR).append(';').append(ChatColor.COLOR_CHAR).append(c);
        return hidden.toString();
    }

    /**
     * Convert a string to an invisible colored string <br />
     * (Not safe to use as lore) <br />
     * Note: Do not use semi-colons in this string, or they will be lost when decoding!
     *
     * @param s string to convert
     * @return encoded string
     */
    public static String convertToInvisibleString(String s) {
        if (s == null || s.equals(""))
            return "";
        StringBuilder hidden = new StringBuilder();
        for (char c : s.toCharArray()) hidden.append(ChatColor.COLOR_CHAR).append(c);
        return hidden.toString();
    }

    /**
     * Removes color markers used to encode strings as invisible text
     *
     * @param s encoded string
     * @return string with color markers removed
     */
    public static String convertFromInvisibleString(String s) {
        if (s == null || s.equals("")) {
            return "";
        }
        return s.replaceAll(ChatColor.COLOR_CHAR + ";" + ChatColor.COLOR_CHAR + "|" + ChatColor.COLOR_CHAR, "");
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

    public static Charset detectCharset(File f, Charset def) {
        byte[] buffer = new byte[2048];
        int read = -1;
        // read the first 2kb of the file and test the file's encoding
        try (FileInputStream input = new FileInputStream(f)) {
            read = input.read(buffer);
        } catch (Exception ex) {
            return null;
        }
        return read != -1 ? detectCharset(buffer, read, def) : def;
    }

    public static Charset detectCharset(BufferedInputStream reader, Charset def) {
        byte[] buffer = new byte[2048];
        int read;
        try {
            reader.mark(2048);
            read = reader.read(buffer);
            reader.reset();
        } catch (Exception ex) {
            return null;
        }
        return read != -1 ? detectCharset(buffer, read, def) : def;
    }

    public static Charset detectCharset(byte[] data, int len, Charset def) {
        // check the file header
        if (len > 4) {
            if (data[0] == (byte) 0xFF && data[1] == (byte) 0xFE) {
                return StandardCharsets.UTF_16LE;
                // FF FE 00 00 is UTF-32LE
            } else if (data[0] == (byte) 0xFE && data[1] == (byte) 0xFF) {
                return StandardCharsets.UTF_16BE;
                // 00 00 FE FF is UTF-32BE
            } else if (data[0] == (byte) 0xEF && data[1] == (byte) 0xBB && data[2] == (byte) 0xBF) { // UTF-8 with BOM, same sig as ISO-8859-1
                return StandardCharsets.UTF_8;
            }
        }

        // iterate through sets to test, and return the first that is ok
        for (Charset charset : supportedCharsets) {
            if (charset != null && isCharset(data, charset)) {
                return charset;
            }
        }
        return def;
    }

    public static boolean isCharset(byte[] data, Charset charset) {
        try {
            CharsetDecoder decoder = charset.newDecoder();
            decoder.reset();
            decoder.decode(ByteBuffer.wrap(data));
            return true;
        } catch (CharacterCodingException e) {
        }
        return false;
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
