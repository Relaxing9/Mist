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

import java.util.HashSet;

/**
 * Contains a group of commands for execution. Contains the main command,
 * for instance "/customfishing", and the subs for that command, eg "/customfishing rewards"
 * allows us to group functionality for commands and interact with each other
 */
public abstract class SpigotCommandGroup {

    /**
     * The {@link SpigotSubCommand} that belong to this group
     */
    protected final HashSet<SpigotSubCommand> subCommands = new HashSet<>();

}
