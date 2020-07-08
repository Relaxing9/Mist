/**
 * Copyright © 2020 Property of Illuzionz Studios, LLC
 * All rights reserved. No part of this publication may be reproduced, distributed, or
 * transmitted in any form or by any means, including photocopying, recording, or other
 * electronic or mechanical methods, without the prior written permission of the publisher,
 * except in the case of brief quotations embodied in critical reviews and certain other
 * noncommercial uses permitted by copyright law. Any licensing of this software overrides
 * this statement.
 */
package com.illuzionzstudios.mist.conversation;

import com.illuzionzstudios.mist.util.TextUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationPrefix;

/**
 * A simple conversation prefix with a static string
 */
@RequiredArgsConstructor
public final class SimplePrefix implements ConversationPrefix {

    /**
     * The conversation prefix
     */
    @Getter
    private final String prefix;

    @Override
    public String getPrefix(ConversationContext context) {
        return TextUtil.formatText(prefix);
    }
}

