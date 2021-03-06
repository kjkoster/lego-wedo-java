package org.kjkoster.wedo.bricks;

import static java.lang.String.format;
import static org.kjkoster.wedo.bricks.Tilt.Direction.BACKWARD;
import static org.kjkoster.wedo.bricks.Tilt.Direction.FORWARD;
import static org.kjkoster.wedo.bricks.Tilt.Direction.LEFT;
import static org.kjkoster.wedo.bricks.Tilt.Direction.NO_TILT;
import static org.kjkoster.wedo.bricks.Tilt.Direction.RIGHT;

import lombok.Value;

/**
 * The representation of a single sample from a tilt sensor. These directions
 * are taken with the wire of the sensor facing towards you.
 * <p>
 * The tilt sensor does not tell us a whole lot when it is not slightly tilted.
 * I chose to lump all of those under <code>NO_TILT</code> rather than calling
 * it flat. The same value comes out when the sensor is placed on its side or
 * straight up.
 * 
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
@Value
public class Tilt {
    private final byte value;

    /**
     * The tilt directions.
     */
    public enum Direction {
        /**
         * backwards tilt.
         */
        BACKWARD,

        /**
         * Forward tilt.
         */
        FORWARD,

        /**
         * Left tilt.
         */
        LEFT,

        /**
         * Right tilt.
         */
        RIGHT,

        /**
         * Flat, upright or on its side, no way to tell.
         */
        NO_TILT
    }

    /**
     * Find the tilt direction, if one can be determined.
     * 
     * @return The tilt direction.
     */
    public Direction getDirection() {
        final int tilt = (value & 0xff);
        if (tilt < 4) {
            throw new IllegalStateException(format(
                    "motor interference on tilt sensor (value 0x%02x)", value));
        }
        if (tilt > 10 && tilt < 40) {
            return BACKWARD;
        }
        if (tilt > 60 && tilt < 90) {
            return RIGHT;
        }
        if (tilt > 117 && tilt < 140) {
            return NO_TILT;
        }
        if (tilt > 151 && tilt < 190) {
            return FORWARD;
        }
        if (tilt > 203 && tilt < 240) {
            return LEFT;
        }
        throw new IllegalArgumentException(
                format("unknown tilt value 0x%02x", value));
    }
}
