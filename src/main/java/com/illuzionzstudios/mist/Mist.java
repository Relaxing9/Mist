package com.illuzionzstudios.mist;

import com.illuzionzstudios.mist.scheduler.MinecraftScheduler;
import com.illuzionzstudios.mist.util.TextUtil;
import org.bukkit.conversations.Conversable;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Main library class to handle utils and other stuff
 * Will contain a lot of static utils for our convenience
 *
 * <p>
 * Mist is a library which aims to make developing standalone Spigot
 * plugins fast and easy. By standalone meaning not for developing libraries
 * but used as a library to make full plugins. It aims to be as lag-free/async
 * as possible to prevent lag and do it with as little code as possible. It can
 * be easily shaded in your plugin without bloating the size.
 * </p>
 */
public final class Mist {

    /**
     * The name (with extension) for the main config file
     */
    public static final String SETTINGS_NAME = "config.yml";

    /**
     * Amount of ticks for an invocation to pause before warning
     */
    public static final long TIME_WARNING_THRESHOLD = 100;

    /**
     * Sends the conversable a message later
     */
    public static void tellLaterConversing(final Conversable conversable, final String message, final int delayTicks) {
        MinecraftScheduler.get().synchronize(() -> tellConversing(conversable, message), delayTicks);
    }

    /**
     * Sends the conversable player a colorized message
     */
    public static void tellConversing(final Conversable conversable, final String message) {
        conversable.sendRawMessage(TextUtil.formatText(message).trim());
    }


    /**
     * Convert {@link Iterable} to {@link List}
     *
     * @param iterable The iterable to convert
     * @param <T>      Type of object
     * @return As a collection
     */
    public static <T> List<T> toList(Iterable<T> iterable) {
        return StreamSupport.stream(iterable.spliterator(), false)
                .collect(Collectors.toList());
    }

}
