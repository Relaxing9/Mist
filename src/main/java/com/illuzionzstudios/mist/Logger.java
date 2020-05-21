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

import com.illuzionzstudios.mist.plugin.SpigotPlugin;

import java.util.logging.Level;

/**
 * {@link com.illuzionzstudios.mist.plugin.SpigotPlugin} plugin logger to handle logging
 */
public final class Logger {

    //  -------------------------------------------------------------------------
    //  Main logging functions
    //  -------------------------------------------------------------------------

    /**
     * Debug an object as output
     *
     * @param message Prints the string version of on object as debug
     */
    public static void debug(Object message) {
        log(Level.WARNING, "[DEBUG] " + message);
    }

    /**
     * Print debug messages to the console. Warns as their
     * easier to see
     *
     * @param message Message to send
     * @param parameters Formatting parameters
     */
    public static void debug(String message, Object... parameters) {
        log(Level.WARNING, "[DEBUG] " + message, parameters);
    }

    /**
     * Send a warning to the console
     *
     * @param message Message to send
     * @param parameters Formatting parameters
     */
    public static void warn(String message, Object... parameters) {
        log(Level.WARNING, "[WARN] " + message, parameters);
    }

    /**
     * Log a error to the console
     *
     * @param message Message to send
     * @param parameters Formatting parameters
     */
    public static void severe(String message, Object... parameters) {
        log(Level.SEVERE, "[SEVERE] " + message, parameters);
    }

    /**
     * Basic method to report information to the console
     *
     * @param message Message to send
     * @param parameters Formatting parameters
     */
    public static void info(String message, Object... parameters) {
        log(Level.INFO, message, parameters);
    }

    /**
     * Base method to log output to the console at a certain logging level
     *
     * @param level Logging level
     * @param message The object/message to log
     * @param parameters Formatting parameters
     */
    private static void log(Level level, Object message, Object... parameters) {
        // Make sure has instance
        if (!SpigotPlugin.hasInstance()) {
            System.err.println("Fatal error trying to log messages");
            return;
        }

        SpigotPlugin.getInstance().getLogger().log(level, String.format(message.toString(), parameters));
    }

    //  -------------------------------------------------------------------------
    //  Error handling
    //  -------------------------------------------------------------------------

    /**
     * Nicely display an error to console
     *
     * @param throwable The error to display
     * @param errorMessage The error message/cause for this error
     */
    public static void displayError(final Throwable throwable, String errorMessage) {
        severe(" ");
        severe("An error occurred for " + SpigotPlugin.getPluginName() + " v" + SpigotPlugin.getPluginVersion());
        severe(" ");
        severe(errorMessage);
        severe(" ");
        severe("Report the following error:");
        throwable.printStackTrace();
    }

}
