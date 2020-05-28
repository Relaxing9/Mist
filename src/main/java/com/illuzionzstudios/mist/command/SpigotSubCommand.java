/**
 * Copyright © 2020 Property of Illuzionz Studios, LLC
 * All rights reserved. No part of this publication may be reproduced, distributed, or
 * transmitted in any form or by any means, including photocopying, recording, or other
 * electronic or mechanical methods, without the prior written permission of the publisher,
 * except in the case of brief quotations embodied in critical reviews and certain other
 * noncommercial uses permitted by copyright law. Any licensing of this software overrides
 * this statement.
 */
package com.illuzionzstudios.mist.command;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * This command is apart of a command group. It acts as a sub label to
 * add extra functionality. For instance, "/customfishing rewards", rewards is the
 * sub label
 */
public abstract class SpigotSubCommand extends SpigotCommand {

    /**
     * The registered sub labels, or aliases this command has
     */
    private final ArrayList<String> subLabels = new ArrayList<>();

    /**
     * The latest sub label used when the sub command was run,
     * always updated on executing
     */
    @Setter(value = AccessLevel.PROTECTED)
    @Getter(value = AccessLevel.PROTECTED)
    private String subLabel;

    /**
     * Create a new {@link SpigotCommand} with certain labels
     *
     * @param label   The main label for this command
     * @param aliases Additional labels that correspond to this {@link SpigotCommand}
     */
    protected SpigotSubCommand(@NotNull String label, String... aliases) {
        super(label, aliases);
    }
}
