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

import lombok.Getter;

/**
 * Simple class that contains two values at once
 */
public class Pair<K, V> {

    /**
     * First value
     */
    @Getter
    private K key;

    /**
     * Second value
     */
    @Getter
    private V value;

    /**
     * Construct pair from 2 values
     */
    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

}
