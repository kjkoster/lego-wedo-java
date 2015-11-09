package org.kjkoster.wedo.bricks;

import static java.lang.String.format;

/**
 * The representation of a single sample from a distance sensor.
 * 
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
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
     * Find the byte value of the distance, as read from the LEGO WeDo hub.
     * 
     * @return The distance's byte value.
     */
    public int getValue() {
        return value;
    }

    /**
     * Return the distance in centimetres (approximately).
     * 
     * @return The distance in centimetres.
     */
    public int getCm() {
        return (int) ((20.0 / 145.0) * (value - 69));
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return format("[distance 0x%02x %dcm]", value, getCm());
    }
}
