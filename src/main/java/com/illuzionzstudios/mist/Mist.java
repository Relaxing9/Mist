/**
 * Copyright © 2020 Property of Illuzionz Studios, LLC
 * All rights reserved. No part of this publication may be reproduced, distributed, or
 * transmitted in any form or by any means, including photocopying, recording, or other
 * electronic or mechanical methods, without the prior written permission of the publisher,
 * except in the case of brief quotations embodied in critical reviews and certain other
 * noncommercial uses permitted by copyright law. Any licensing of this software overrides
 * this statement.
 */
package com.illuzionzstudios.mist;

import com.illuzionzstudios.mist.scheduler.MinecraftScheduler;
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

    //  -------------------------------------------------------------------------
    //  Static final settings across the plugin
    //  Stored in internal classes relating to their category
    //  -------------------------------------------------------------------------

    /**
     * Core options relating to the plugin
     */
    public static final class Core {

        /**
         * Core revision version, meaning each major refactor or change,
         * we increment this number
         */
        public static final int CORE_VERSION = 1;

        /**
         * The actual version of this core. Each new release, set a new version
         */
        public static final String VERSION = "1.0a";

    }

    /**
     * Options relating to file parsing
     */
    public static final class File {

        /**
         * The name (with extension) for the main config file
         */
        public static final String SETTINGS_NAME = "config.yml";

    }

    /**
     * Options relating to the ticker
     */
    public static final class Scheduler {

        /**
         * Amount of ticks for a invocation to pause before warning
         */
        public static final long TIME_WARNING_THRESHOLD = 100;

    }

    /**
     * Static strings for aesthetics
     */
    public static final class Aesthetics {

        /**
         * Returns a long ------ smooth console line
         */
        public static final String SMOOTH_LINE = ChatColor.STRIKETHROUGH + "                                                               ";

    }

    //  -------------------------------------------------------------------------
    //  Static util methods
    //  -------------------------------------------------------------------------

    /**
     * Simply turn a message into a colored version with {@link ChatColor#COLOR_CHAR}
     *
     * @param message The original method without colors
     * @return The newly colored message
     */
    public static String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    /**
     * Sends the conversable a message later
     *
     * @param delayTicks
     * @param conversable
     * @param message
     */
    public static void tellLaterConversing(final Conversable conversable, final String message, final int delayTicks) {
        MinecraftScheduler.get().synchronize(() -> tellConversing(conversable, message), delayTicks);
    }

    /**
     * Sends the conversable player a colorized message
     *
     * @param conversable
     * @param message
     */
    public static void tellConversing(final Conversable conversable, final String message) {
        conversable.sendRawMessage(Mist.colorize(message).trim());
    }


    /**
     * Convert {@link Iterable} to {@link List}
     *
     * @param iterable The iterable to convert
     * @param <T> Type of object
     * @return As a collection
     */
    public static <T> List<T> toList(Iterable<T> iterable) {
        return StreamSupport.stream(iterable.spliterator(), false)
                        .collect(Collectors.toList());
    }

}
