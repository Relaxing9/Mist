/**
 * Copyright © 2020 Property of Illuzionz Studios, LLC
 * All rights reserved. No part of this publication may be reproduced, distributed, or
 * transmitted in any form or by any means, including photocopying, recording, or other
 * electronic or mechanical methods, without the prior written permission of the publisher,
 * except in the case of brief quotations embodied in critical reviews and certain other
 * noncommercial uses permitted by copyright law. Any licensing of this software overrides
 * this statement.
 */
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
     * @param t The error thrown
     */
    public PluginException(Throwable t, String message) {
        super(message, t);
    }

}
