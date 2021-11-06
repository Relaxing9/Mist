package com.illuzionzstudios.mist.random;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Random;

/**
 * A util class for randomly generating numbers with an upper and lower limit
 * Provides additional functionality on top of {@link java.util.Random}
 * <p>
 * Generates from lower (inclusive) to upper (inclusive)
 */
@NoArgsConstructor
public class RandomNumberGenerator {

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

    public RandomNumberGenerator(final double upper) {
        this(0, upper);
    }

    /**
     * Set lower and upper limits for generation
     */
    public RandomNumberGenerator(final double lower, final double upper) {
        this.lower = lower;
        this.upper = upper;
    }

    /**
     * Parse a string into a {@link RandomNumberGenerator}.
     * Syntax is "{lower}to{upper}". If just one number is provided, only generates
     * that single number.
     *
     * @param string String as "{lower}to{upper}"
     * @return {@link RandomNumberGenerator} with those bounds
     */
    public static RandomNumberGenerator parse(String string) {
        if (string == null) return new RandomNumberGenerator(0);

        // Remove whitespace
        string = string.replaceAll("\\s+", "");
        // Create tokens
        String[] tokens = string.split("to");

        // Else use first element as upper
        return tokens[1] != null ? new RandomNumberGenerator(Double.parseDouble(tokens[0]), Double.parseDouble(tokens[1]))
                : new RandomNumberGenerator(Double.parseDouble(tokens[0]), Double.parseDouble(tokens[0]));
    }

    /**
     * @return A randomly generated number in our range
     */
    public double generate() {
        // Give new serial for generation
        Random random = new Random();

        // Precise value to add on to generated value
        double precision = random.nextDouble();
        return (random.nextInt(((int) upper - (int) lower) + 1) + lower) + precision;
    }

}
