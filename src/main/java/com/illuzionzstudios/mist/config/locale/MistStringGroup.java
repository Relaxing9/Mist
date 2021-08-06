package com.illuzionzstudios.mist.config.locale;

import com.illuzionzstudios.mist.plugin.SpigotPlugin;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a group of strings for a section. Eg strings to do with interface or the player.
 * The way this works is we create strings via this group and then load this group after
 * the locale is loaded. This way the {@link MistString}'s get loaded from locale without null
 * errors
 *
 * Should be loaded in {@link SpigotPlugin#onReloadablesStart()}
 */
@RequiredArgsConstructor
public class MistStringGroup {

    /**
     * Name of this group
     */
    @Getter
    public final String name;

    /**
     * List of strings in group
     */
    private final List<MistString> strings = new ArrayList<>();

    /**
     * Create a new string and add it to our group
     *
     * @param key Key from config
     * @param def Default value
     * @return Created {@link MistString}
     */
    public MistString create(final String key, final String def) {
        MistString string = new MistString(key, def);
        strings.add(string);
        return string;
    }

    /**
     * Load all strings into cache and locale
     */
    public final void load() {
        strings.forEach(MistString::loadString);
    }

}
