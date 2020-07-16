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
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Random;

/**
 * A util class for randomly generating numbers with an upper and lower limit
 * Provides additional functionality on top of {@link java.util.Random}
 *
 * Generates from lower (inclusive) to upper (inclusive)
 */
@NoArgsConstructor
public class RandomNumberGenerator {

    /**
     * Instanced random object to help us with random operations
     */
    private Random random;

    /**
     * The lower bound for a generated number
     */
    @Getter
    @Setter
    private double lower;

    /**
     * The upper bound for a generated number
     */
    @Getter
    @Setter
    private double upper;

    public RandomNumberGenerator(double upper) {
        this(0, upper);
    }

    /**
     * Set lower and upper limits for generation
     */
    public RandomNumberGenerator(double lower, double upper) {
        this.lower = lower;
        this.upper = upper;
    }

    /**
     * @return A randomly generated number in our range
     */
    public double generate() {
        // Give new serial for generation
        random = new Random();

        // Precise value to add on to generated value
        double precision = random.nextDouble();
        return (random.nextInt(((int) upper - (int) lower) + 1) + lower) + precision;
    }

}
