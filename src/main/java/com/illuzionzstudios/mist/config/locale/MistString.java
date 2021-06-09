package com.illuzionzstudios.mist.config.locale;

import com.illuzionzstudios.mist.config.YamlConfig;
import com.illuzionzstudios.mist.util.TextUtil;

import java.util.regex.Matcher;

/**
 * This represents a custom translatable string that can be used in the plugin where we
 * would usually use a {@link String}. It is based of plugin translation files and includes
 * utils for formatting and replacing parts of the string.
 */
public class MistString {

    /**
     * The key of this string for the locale
     */
    private final String key;
    
    /**
     * The raw contents of this string
     */
    private String value;
    
    public MistString(final String value) {
        this("", value);
    }

    public MistString(final String key, final String value) {
        this.key = key;
        this.value = value;
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
        final String place = Matcher.quoteReplacement(placeholder);
        this.value = value.replaceAll("\\{" + place + "}", replacement == null ? "" : Matcher.quoteReplacement(replacement.toString()));
        return this;
    }

    /**
     * Chain replace placeholders.
     * See {@link #toString(String, Object)}
     */
    public MistString toString(final String placeholder1, final Object replacement1, final String placeholder2, final Object replacement2) {
        return this.toString(placeholder1, replacement1).toString(placeholder2, replacement2);
    }
    
    @Override
    public String toString() {
        // Get the message from locale cache otherwise search
        return TextUtil.formatText(PluginLocale.getMessage(key, value));
    }

}
