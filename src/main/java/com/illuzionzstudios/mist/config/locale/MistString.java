package com.illuzionzstudios.mist.config.locale;

import com.illuzionzstudios.mist.compatibility.ServerVersion;
import com.illuzionzstudios.mist.util.TextUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * This represents a custom translatable string that can be used in the plugin where we
 * would usually use a {@link String}. It is based of plugin translation files and includes
 * utils for formatting and replacing parts of the string.
 * <p>
 * <p>
 * Reset value once has been queried
 */
public class MistString {

    /**
     * If can use the action bar
     */
    private static boolean canActionBar = false;

    static {
        try {
            Class.forName("net.md_5.bungee.api.ChatMessageType");
            Class.forName("net.md_5.bungee.api.chat.TextComponent");
            Player.Spigot.class.getDeclaredMethod("sendMessage", net.md_5.bungee.api.ChatMessageType.class, net.md_5.bungee.api.chat.TextComponent.class);
            canActionBar = true;
        } catch (Exception ignored) {
        }
    }

    /**
     * The key of this string for the locale
     */
    private final String key;

    /**
     * Default value of the string
     */
    private final String def;

    /**
     * The raw contents of this string
     */
    private String value;

    /**
     * If it has been loaded from the locale
     */
    private boolean loaded;

    /**
     * Used when we just want a string with content and not
     * to be localised
     *
     * @param def The value of the string
     */
    public MistString(final String def) {
        this("", def);
    }

    public MistString(final String key, final String def) {
        this.key = key;
        this.def = this.value = def;
    }

    /**
     * @param other Create string from another
     */
    public MistString(final MistString other) {
        this(other.key, other.def);
    }

    /**
     * Converts a list of strings to one mist string
     *
     * @param list The list of strings to convert
     * @return One {@link MistString}
     */
    public static MistString fromList(final List<String> list) {
        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            builder.append(list.get(i));

            // Can't compare values have to compare index
            if (i != list.size() - 1)
                builder.append("\n");
        }

        return new MistString(builder.toString());
    }

    /**
     * Easily turn a list of strings into a list of {@link MistString}
     *
     * @param list The list to convert
     * @return The list of {@link MistString} with the original list's values
     */
    public static List<MistString> fromStringList(final List<String> list) {
        ArrayList<MistString> strings = new ArrayList<>();
        list.forEach(string -> strings.add(new MistString(string)));
        return strings;
    }

    /**
     * Easily turn a list of {@link MistString} into a list of strings
     *
     * @param list The list to convert
     * @return The list of string with the original list's values
     */
    public static List<String> fromMistList(final List<MistString> list) {
        ArrayList<String> strings = new ArrayList<>();
        list.forEach(string -> strings.add(string.toString()));
        return strings;
    }

    /**
     * Concat another string to this string
     *
     * @param other String to add
     * @return String with other added on
     */
    public MistString concat(final String other) {
        this.value = this.value.concat(other);
        return this;
    }

    /**
     * See {@link #concat(String)}
     */
    public MistString concat(final MistString other) {
        concat(other.toString());
        return this;
    }

    /**
     * Replace the provided placeholder with the provided object. <br />
     * Supports {@code {value}} placeholders
     *
     * @param placeholder the placeholder to replace
     * @param replacement the replacement object
     * @return the modified Message
     */
    public MistString toString(final String placeholder, final Object replacement) {
        loadString();
        final String place = Matcher.quoteReplacement(placeholder);
        this.value = value.replaceAll("\\{" + place + "}", replacement == null ? "" : Matcher.quoteReplacement(replacement.toString()));
        return this;
    }

    /**
     * Replace everything in the string according to this replacement map.
     *
     * @param replacements The map of replacements
     * @return the modified Message
     */
    public MistString toString(final Map<String, Object> replacements) {
        replacements.forEach(this::toString);
        return this;
    }

    /**
     * Chain replace placeholders.
     * See {@link #toString(String, Object)}
     */
    @Deprecated
    public MistString toString(final String placeholder1, final Object replacement1, final String placeholder2, final Object replacement2) {
        return this.toString(placeholder1, replacement1).toString(placeholder2, replacement2);
    }

    /**
     * After turning into final string we must reset the value as
     * otherwise things like concat will continue for every call
     */
    @Override
    public String toString() {
        String toSend = value;
        // Reload value
        this.loaded = false;
        loadString();
        return TextUtil.formatText(toSend);
    }

    /**
     * Loads string from locale into value. Must be used before replacing
     */
    public void loadString() {
        if (!loaded) {
            this.value = TextUtil.formatText(PluginLocale.getMessage(key, def));
            loaded = true;
        }
    }

    /**
     * Format and send the held message to a player.
     * Detect if string is split and send multiple lines
     *
     * @param player player to send the message to
     */
    public void sendMessage(final CommandSender player) {
        // Check for split lore
        String[] strings = toString().split("\\n");
        player.sendMessage(strings);
    }

    /**
     * Format and send the held message to a player as a title message
     *
     * @param sender command sender to send the message to
     */
    public void sendTitle(final CommandSender sender) {
        this.sendTitle(sender, new MistString(""));
    }

    /**
     * Format and send the held message to a player as a title message
     *
     * @param sender   command sender to send the message to
     * @param subtitle Subtitle to send
     */
    public void sendTitle(final CommandSender sender, final MistString subtitle) {
        if (sender instanceof Player) {
            if (ServerVersion.atLeast(ServerVersion.V.v1_11)) {
                ((Player) sender).sendTitle(toString(), subtitle.toString(), 10, 20, 10);
            } else {
                ((Player) sender).sendTitle(toString(), subtitle.toString());
            }
        } else {
            sendMessage(sender);
        }
    }

    /**
     * Format and send the held message to a player as an actionbar message
     *
     * @param sender command sender to send the message to
     */
    public void sendActionBar(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(toString());
        } else if (!canActionBar) {
            sendTitle(sender);
        } else {
            ((Player) sender).spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                    new net.md_5.bungee.api.chat.TextComponent(toString()));
        }
    }

}
