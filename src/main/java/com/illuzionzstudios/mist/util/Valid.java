package com.illuzionzstudios.mist.util;

import com.illuzionzstudios.mist.exception.PluginException;
import lombok.experimental.UtilityClass;

import java.util.regex.Pattern;

/**
 * Util class to check if things are valid
 */
@UtilityClass
public class Valid {

    /**
     * Matching valid integers
     */
    private final Pattern PATTERN_INTEGER = Pattern.compile("-?\\d+");

    /**
     * Matching valid whole numbers
     */
    private final Pattern PATTERN_DECIMAL = Pattern.compile("-?\\d+.\\d+");


    /**
     * Throws an error if the given object is null
     *
     * @param toCheck Object to check if is null
     */
    public void checkNotNull(Object toCheck) {
        if (toCheck == null)
            throw new PluginException();
    }

    /**
     * Throws an error with a custom message if the given object is null
     *
     * @param toCheck Object to check if is null
     * @param falseMessage Message explaining why it may have been null
     */
    public void checkNotNull(Object toCheck, String falseMessage) {
        if (toCheck == null)
            throw new PluginException(falseMessage);
    }

    /**
     * Throws an error if the given expression is false
     *
     * @param expression Boolean expression to check
     */
    public void checkBoolean(boolean expression) {
        if (!expression)
            throw new PluginException();
    }

    /**
     * Throws an error with a custom message if the given expression is false
     *
     * @param expression Boolean expression to check
     * @param falseMessage Message explaining why it may have been false
     */
    public void checkBoolean(boolean expression, String falseMessage) {
        if (!expression)
            throw new PluginException(falseMessage);
    }

    // ------------------------------------------------------------------------------------------------------------
    // Checking for true without throwing errors
    // ------------------------------------------------------------------------------------------------------------

    /**
     * Returns true if the given string is a valid integer
     *
     * @param raw
     * @return
     */
    public boolean isInteger(String raw) {
        return PATTERN_INTEGER.matcher(raw).find();
    }

    /**
     * Returns true if the given string is a valid whole number
     *
     * @param raw
     * @return
     */
    public boolean isDecimal(String raw) {
        return PATTERN_DECIMAL.matcher(raw).find();
    }

    /**
     * Parse a string into a boolean
     *
     * @param check The string to check
     * @return True if parsed correctly otherwise false
     */
    public boolean parseBoolean(String check) {
        return Pattern.compile("true|yes|1").matcher(check).find();
    }

}
