package org.kjkoster.wedo.bricks;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static java.lang.String.format;
import static org.kjkoster.wedo.bricks.Brick.Type.DISTANCE;
import static org.kjkoster.wedo.bricks.Brick.Type.TILT;

/**
 * The representation of a single LEGO brick. To be precise, this is the
 * representation of a connector on a hub. Empty spots are represented as bricks
 * too, of a &quot;not connected&quot; type.
 * 
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
public class Brick {
    private final char port;

    private final Type type;

    private final byte value;

    /**
     * Create a new brick representation.
     * 
     * @param port
     *            The capital letter designating the port on the hub that this
     *            brick is connected to.
     * @param type
     *            The type of brick.
     */
    public Brick(final char port, final Type type) {
        this(port, type, (byte) 0x00);
    }

    /**
     * Create a new brick representation.
     * 
     * @param port
     *            The capital letter designating the port on the hub that this
     *            brick is connected to.
     * @param type
     *            The type of brick.
     * @param value
     *            The value of the value byte that was read from the WeDo hub.
     */
    public Brick(final char port, final Type type, final byte value) {
        super();

        checkArgument(port >= 'A' && port <= 'D', "invalid port %c", port);
        this.port = port;

        checkNotNull(type);
        this.type = type;

        this.value = value;
    }

    /**
     * Find what port this brick is connected to.
     * 
     * @return The port that this brick is connected to, as a capital letter.
     */
    public char getPort() {
        return port;
    }

    /**
     * The WeDo brick types that we know of, plus a few internal ones to make
     * the code simpler.
     */
    public enum Type {
        /**
         * No brick is connected at this connector.
         */
        NOT_CONNECTED,

        /**
         * A motor is connected at this connector.
         */
        MOTOR,

        /**
         * A light is connected at this connector.
         */
        LIGHT,

        /**
         * A distance sensor is connected at this connector.
         */
        DISTANCE,

        /**
         * A tilt sensor is connected at this connector.
         */
        TILT,

        /**
         * Something unknown is connected at this connector.
         */
        UNKNOWN
    }

    /**
     * Find the type of the brick that is connected at this place.
     * 
     * @return The brick's type.
     */
    public Type getType() {
        return type;
    }

    /**
     * Get a distance measurement. May only be called when the type is a
     * distance sensor.
     * 
     * @return The measured distance.
     */
    public Distance getDistance() {
        checkState(type == DISTANCE);
        return new Distance(value);
    }

    /**
     * Get a tilt measurement. May only be called when the type is a tilt
     * sensor.
     * 
     * @return The measured tilt.
     */
    public Tilt getTilt() {
        checkState(type == TILT);
        return new Tilt(value);
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        final String sensorData;
        switch (type) {
        case DISTANCE:
            sensorData = " " + getDistance();
            break;
        case TILT:
            sensorData = " " + getTilt();
            break;
        default:
            sensorData = "";
        }

        return format("[port %c: %s value: 0x%02x%s]", port, type, value,
                sensorData);
    }
}
