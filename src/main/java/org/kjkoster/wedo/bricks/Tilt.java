package org.kjkoster.wedo.bricks;

/**
 * The representation of a single sample from a tilt sensor.
 * 
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
public class Tilt {
    private final byte value;

    /**
     * Create a new tilt sensor sample.
     * 
     * @param value
     *            The raw tilt sensor value.
     */
    public Tilt(final byte value) {
        this.value = value;
    }

    /**
     * Find the byte value of the tilt, as read from the LEGO WeDo hub.
     * 
     * @return The tilt's byte value.
     */
    public byte getValue() {
        return value;
    }

    /**
     * @return
     */
    public String getDirection() {
        // XXX
        return "XXX-dir";
    }

    /**
     * @return
     */
    public int getAngle() {
        // XXX
        return 40;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        int tilt = (value & 0xff);
        String d;
        if (tilt > 10 && tilt < 40) {
            d = "Tilt.BACK";
        } else if (tilt > 60 && tilt < 90) {
            d = "Tilt.RIGHT";
        } else if (tilt > 120 && tilt < 140) {
            d = "Tilt.NO_TILT";
        } else if (tilt > 170 && tilt < 190) {
            d = "Tilt.FORWARD";
        } else if (tilt > 220 && tilt < 240) {
            d = "Tilt.LEFT";
        } else {
            // XXX
            d = "Tilt.NO_TILTXXX";
        }
        return String.format("[tilt 0x%02x %d %s]", value, tilt, d);
    }
}
