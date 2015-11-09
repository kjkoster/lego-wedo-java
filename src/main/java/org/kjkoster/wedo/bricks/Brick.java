package org.kjkoster.wedo.bricks;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static java.lang.String.format;
import static org.kjkoster.wedo.bricks.Brick.Type.DISTANCE;
import static org.kjkoster.wedo.bricks.Brick.Type.TILT;

import org.kjkoster.wedo.usb.Handle;

/**
 * The representation of a single LEGO WeDo brick. To be precise, this is the
 * representation of a connector on a hub. Empty spots are represented as bricks
 * too, of a not connected type.
 * 
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
public class Brick {
    private final Handle handle;
    private final boolean isA;

    private final byte id;
    private final Type type;

    private final byte value;

    /**
     * Create a new brick representation.
     * 
     * @param handle
     *            The USB device handle of the device that this brick is
     *            connected to.
     * @param isA
     *            <code>true</code> if this is on connector A of the WeDo hub,
     *            or <code>false</code>if this comes from connector B.
     * @param type
     *            The type of brick.
     * @param id
     *            The value of the ID byte that was read from the WeDo hub.
     * @param value
     *            The value of the value byte that was read from the WeDo hub.
     */
    public Brick(final Handle handle, final boolean isA, final Type type,
            final byte id, final byte value) {
        this.handle = checkNotNull(handle);
        this.isA = isA;
        this.type = checkNotNull(type);
        this.id = id;
        this.value = value;
    }

    Handle getHandle() {
        return handle;
    }

    boolean isA() {
        return isA;
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

        return format("[%s brick %s: %s id: 0x%02x value: 0x%02x%s]", handle,
                (isA ? "A" : "B"), type, id, value, sensorData);
    }
}