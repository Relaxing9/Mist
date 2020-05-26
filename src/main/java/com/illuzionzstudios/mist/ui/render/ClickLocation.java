/**
 * Copyright © 2020 Property of Illuzionz Studios, LLC
 * All rights reserved. No part of this publication may be reproduced, distributed, or
 * transmitted in any form or by any means, including photocopying, recording, or other
 * electronic or mechanical methods, without the prior written permission of the publisher,
 * except in the case of brief quotations embodied in critical reviews and certain other
 * noncommercial uses permitted by copyright law. Any licensing of this software overrides
 * this statement.
 */
package com.illuzionzstudios.mist.ui.render;

/**
 * Places the player can click when having an open inventory
 */
public enum ClickLocation {

    /**
     * Clicked in our custom {@link com.illuzionzstudios.mist.ui.UserInterface}
     */
    INTERFACE,

    /**
     * Clicked in the bottom player inventory
     */
    PLAYER_INVENTORY,

    /**
     * Didn't click in a inventory at all, could produce null
     */
    OUTSIDE,

}
