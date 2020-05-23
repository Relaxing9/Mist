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

import lombok.Getter;

/**
 * Represents a silent exception thrown then handling commands,
 * this will only send the command sender a message
 */
public class CommandException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * The messages to send to the command sender
     */
    @Getter
    private final String[] messages;

    /**
     * Create a new command exception with messages for the command sender
     *
     * @param messages Messages to send command sender
     */
    public CommandException(String... messages) {
        super("");

        this.messages = messages;
    }
}
