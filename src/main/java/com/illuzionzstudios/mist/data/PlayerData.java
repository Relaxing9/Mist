/**
 * Copyright © 2020 Property of Illuzionz Studios, LLC
 * All rights reserved. No part of this publication may be reproduced, distributed, or
 * transmitted in any form or by any means, including photocopying, recording, or other
 * electronic or mechanical methods, without the prior written permission of the publisher,
 * except in the case of brief quotations embodied in critical reviews and certain other
 * noncommercial uses permitted by copyright law. Any licensing of this software overrides
 * this statement.
 */
package com.illuzionzstudios.mist.data;

import com.illuzionzstudios.mist.data.player.AbstractPlayer;

/**
 * Basis of player data
 */
public interface PlayerData<P extends AbstractPlayer> {

    /**
     * Gets the player associated with the data
     */
    P getPlayer();

}
