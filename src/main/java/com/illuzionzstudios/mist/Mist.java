package com.illuzionzstudios.mist;

import com.illuzionzstudios.mist.scheduler.MinecraftScheduler;
import com.illuzionzstudios.mist.util.TextUtil;
import org.bukkit.ChatColor;
import org.bukkit.conversations.Conversable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Main library class to handle utils and other stuff
 * Will contain a lot of static utils for our convenience
 */
public final class Mist {

    /**
     * The name (with extension) for the main config file
     */
    public static final String SETTINGS_NAME = "config.yml";

    /**
     * Amount of ticks for a invocation to pause before warning
     */
    public static final long TIME_WARNING_THRESHOLD = 100;

    /**
     * Returns a long ------ smooth console line
     */
    public static final String SMOOTH_LINE = ChatColor.STRIKETHROUGH + "                                                               ";

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
