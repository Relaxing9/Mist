/**
 * Copyright © 2020 Property of Illuzionz Studios, LLC
 * All rights reserved. No part of this publication may be reproduced, distributed, or
 * transmitted in any form or by any means, including photocopying, recording, or other
 * electronic or mechanical methods, without the prior written permission of the publisher,
 * except in the case of brief quotations embodied in critical reviews and certain other
 * noncommercial uses permitted by copyright law. Any licensing of this software overrides
 * this statement.
 */
package com.illuzionzstudios.mist.util;

import com.illuzionzstudios.mist.exception.PluginException;
import lombok.experimental.UtilityClass;

/**
 * Util class to check if things are valid
 */
@UtilityClass
public class Valid {

    /**
     * Throws an error if the given expression is false
     *
     * @param expression
     */
    public void checkBoolean(boolean expression) {
        if (!expression)
            throw new PluginException();
    }

    /**
     * Throws an error with a custom message if the given expression is false
     *
     * @param expression
     * @param falseMessage
     */
    public void checkBoolean(boolean expression, String falseMessage) {
        if (!expression)
            throw new PluginException(falseMessage);
    }

}
