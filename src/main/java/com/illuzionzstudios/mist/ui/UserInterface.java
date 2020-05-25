/**
 * Copyright © 2020 Property of Illuzionz Studios, LLC
 * All rights reserved. No part of this publication may be reproduced, distributed, or
 * transmitted in any form or by any means, including photocopying, recording, or other
 * electronic or mechanical methods, without the prior written permission of the publisher,
 * except in the case of brief quotations embodied in critical reviews and certain other
 * noncommercial uses permitted by copyright law. Any licensing of this software overrides
 * this statement.
 */
package com.illuzionzstudios.mist.ui;

import com.illuzionzstudios.mist.plugin.SpigotPlugin;

/**
 * Our main menu (or interface) for all inventory interaction. We provide
 * a lot of functionality for adding buttons, storing items, and being
 * able to navigate through the menu easily. We can also have a parent menu
 * that can be returned to.
 */
public abstract class UserInterface {

    //  -------------------------------------------------------------------------
    //  Static variables
    //  -------------------------------------------------------------------------

    /**
     * This is an internal metadata tag that the player has.
     *
     * This will set the name of the current menu in order to keep
     * track of what menu is currently open
     */
    public static final String TAG_CURRENT = "UI_" + SpigotPlugin.getPluginName();

    /**
     * This is an internal metadata tag that the player has.
     *
     * This will set the name of the previous menu in order to
     * backtrack for returning menus
     */
    public static final String TAG_PREVIOUS = "UI_PREVIOUS_" + SpigotPlugin.getPluginName();

}
