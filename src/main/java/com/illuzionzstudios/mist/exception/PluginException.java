package com.illuzionzstudios.mist.exception;

import lombok.NoArgsConstructor;

/**
 * Custom exception in our plugin which allows us
 * to easily debug problems
 */
@NoArgsConstructor
public class PluginException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Create a new exception and logs it
     *
     * @param t The error thrown
     */
    public PluginException(Throwable t) {
        super(t);
    }

    /**
     * Create a new exception and logs it
     *
     * @param message The cause of the error
     */
    public PluginException(String message) {
        super(message);
    }

    /**
     * Create a new exception and logs it
     *
     * @param message The cause of the error
     * @param t       The error thrown
     */
    public PluginException(Throwable t, String message) {
        super(message, t);
    }

}
