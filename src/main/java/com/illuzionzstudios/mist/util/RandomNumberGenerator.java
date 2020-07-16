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

    /**
     * Parse a string into a {@link RandomNumberGenerator}.
     * Syntax is "{lower}-{upper}". If just one number is provided, it is used
     * as the upper bound with lower being 1.
     *
     * @param string String as "{lower}-{upper}"
     * @return {@link RandomNumberGenerator} with those bounds
     */
    public static RandomNumberGenerator parse(String string) {
        // Create tokens
        String[] tokens = string.split("-");

        // Else use first element as upper
        return tokens[1] != null ? new RandomNumberGenerator(Double.parseDouble(tokens[0]), Double.parseDouble(tokens[1]))
                : new RandomNumberGenerator(1, Double.parseDouble(tokens[0]));
    }

}
