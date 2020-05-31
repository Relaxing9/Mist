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

import com.illuzionzstudios.mist.plugin.SpigotPlugin;
import com.illuzionzstudios.mist.util.Valid;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * This command is apart of a command group. It acts as a sub label to
 * add extra functionality. For instance, "/customfishing rewards", rewards is the
 * sub label
 *
 * Acts as a normal command that runs based of a {@link SpigotCommandGroup}
 */
public abstract class SpigotSubCommand extends SpigotCommand {

    /**
     * The registered sub labels, or aliases this command has
     */
    @Getter
    private final String[] subLabels;

    /**
     * The latest sub label used when the sub command was run,
     * always updated on executing
     */
    @Setter(value = AccessLevel.PROTECTED)
    @Getter(value = AccessLevel.PROTECTED)
    private String subLabel;

    /**
     * Create a new {@link SpigotCommand} with certain labels
     * Main command group found from {@link SpigotPlugin#getMainCommand()}
     *
     * @param aliases Additional labels that correspond to this {@link SpigotCommand}
     *                First label is the main label
     */
    protected SpigotSubCommand(@NotNull String... aliases) {
        this(getMainCommandGroup(), aliases);
    }

    /**
     * Create a new {@link SpigotCommand} with certain labels
     *
     * @param aliases Additional labels that correspond to this {@link SpigotCommand}
     *                First label is the main label
     */
    protected SpigotSubCommand(SpigotCommandGroup parent, @NotNull String... aliases) {
        super(parent.getLabel());

        // Set sub labels
        this.subLabels = aliases;

        // Set main label
        this.subLabel = subLabels[0];

        // If the default perm was not changed, improve it
        if (getRawPermission().equals(DEFAULT_PERMISSION_SYNTAX))
            if (SpigotPlugin.isMainCommand(this.getMainLabel()))
                setPermission(getRawPermission().replace("{label}", "{sublabel}")); // simply replace label with sublabel
            else
                setPermission(getRawPermission() + ".{sublabel}"); // append the sublabel at the end since this is not our main command
    }

    /**
     * @return Main {@link SpigotCommandGroup} for the plugin
     */
    private static SpigotCommandGroup getMainCommandGroup() {
        final SpigotCommandGroup main = SpigotPlugin.getInstance().getMainCommand();
        Valid.checkNotNull(main, SpigotPlugin.getPluginName() + " does not define a main command group!");

        return main;
    }

    /**
     * The command group automatically displays all sub commands in the /{label} help|? menu.
     * Shall we display the sub command in this menu?
     *
     * @return If to show in help
     */
    protected boolean showInHelp() {
        return true;
    }

    /**
     * Replace additional {sublabel} placeholder for this subcommand.
     * See {@link SpigotCommand#replacePlaceholders(String)}
     */
    @Override
    protected String replacePlaceholders(String message) {
        return super.replacePlaceholders(message).replace("{sublabel}", getSubLabel());
    }

    /**
     * Compare based on sub labels
     */
    @Override
    public final boolean equals(Object obj) {
        return obj instanceof SpigotCommand && Arrays.equals(((SpigotSubCommand) obj).subLabels, this.subLabels);
    }
}
