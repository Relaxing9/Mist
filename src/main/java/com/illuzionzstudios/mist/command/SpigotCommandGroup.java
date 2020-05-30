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

import com.illuzionzstudios.mist.util.Valid;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;

/**
 * Contains a group of commands for execution. Contains the main command,
 * for instance "/customfishing", and the subs for that command, eg "/customfishing rewards"
 * allows us to group functionality for commands and interact with each other
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class SpigotCommandGroup {

    /**
     * The {@link SpigotSubCommand} that belong to this group
     */
    @Getter
    protected final HashSet<SpigotSubCommand> subCommands = new HashSet<>();

    /**
     * The main {@link SpigotCommand} which {@link SpigotSubCommand} are
     * executed through
     */
    protected SpigotCommand mainCommand;

    /**
     * Register this command group with the main label,
     * then add sub commands to it
     *
     * @param labels List of main label and aliases
     */
    public final void register(String... labels) {
        Valid.checkBoolean(!isRegistered(), "Main command was already registered as: " + mainCommand);

        // Set command
    }

    /**
     * Get the label for this command group, failing if not yet registered
     *
     * @return The string label
     */
    public final String getLabel() {
        Valid.checkBoolean(isRegistered(), "Main command has not yet been set!");

        return mainCommand.getMainLabel();
    }

    /**
     * Has the command group been registered yet?
     *
     * @return If main command is set
     */
    public final boolean isRegistered() {
        return mainCommand != null;
    }

    //  -------------------------------------------------------------------------
    //  Execution
    //  -------------------------------------------------------------------------

    /**
     * Handles our main command to detect sub commands and run functionality
     */
    private final class MainCommand extends SpigotCommand {

        /**
         * @param label Create the main command with the main label
         */
        public MainCommand(String label) {
            super(label);

            // Let everyone view info
            setPermission(null);
        }

        /**
         * Here we handle our main help, showing sub commands etc.
         * Also handle execution of sub commands
         */
        @Override
        protected void onCommand() {
            // Show our help
            // Assures arg[0] isn't null
            if (args.length == 0) {

            }

            final String subArg = args[0];

        }
    }

}
