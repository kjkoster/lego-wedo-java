package org.kjkoster.wedo.bricks;

import lombok.Value;

/**
 * The representation of a single sample from a distance sensor.
 * 
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
@Value
public class Distance {
    private final int value;

    /**
     * Create a new distance sensor sample.
     * 
     * @param value
     *            The raw distance sensor value.
     */
    public Distance(final int value) {
        this.value = (value & 0xff);
    }

    /**
     * Return the distance in centimetres (approximately).
     * 
     * @return The distance in centimetres.
     */
    public int getCm() {
        return (int) ((20.0 / 145.0) * (value - 69));
    }
}
