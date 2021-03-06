package org.kjkoster.wedo.systems.sbrick;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.kjkoster.wedo.bricks.Brick.FIRST_PORT;
import static org.kjkoster.wedo.bricks.Brick.MAX_PORT;
import static org.kjkoster.wedo.bricks.Brick.Type.LIGHT;
import static org.kjkoster.wedo.bricks.Brick.Type.MOTOR;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.kjkoster.wedo.bricks.ActuatorValueMemory;
import org.kjkoster.wedo.bricks.Brick;
import org.kjkoster.wedo.bricks.Brick.Type;
import org.kjkoster.wedo.bricks.Hub;
import org.kjkoster.wedo.transport.ble112.BLE112Connections;
import org.thingml.bglib.BGAPI;
import org.thingml.bglib.BGAPIDefaultListener;

import lombok.NonNull;

/**
 * A class to represent the collection of SBricks and SBrick Pluses.
 *
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
public class SBricks extends BGAPIDefaultListener implements AutoCloseable {
    private static final int HANDLE_QUICKDRIVE = 0x001e;

    private final List<Hub> hubs = new ArrayList<>();

    private final BGAPI bgapi;

    private final BLE112Connections ble112Connections;

    private final ActuatorValueMemory actuatorValueMemory = new ActuatorValueMemory(
            4);

    /**
     * Create a new SBrick API that can be used to access all SBricks.
     * 
     * @param bgapi
     *            The BGAPI interface that we can use to send commands.
     * @param hubs
     *            The definition of all SBrick hubs. Unlike WeDo (for example)
     *            SBrick's protocol does not have facilities to detect what
     *            brick is connected on what port of the hub. Instead, we have
     *            to rely on that information being supplied.
     * @throws FileNotFoundException
     *             When the specified device could not be opened.
     */
    public SBricks(final BGAPI bgapi, final Collection<Hub> hubs)
            throws FileNotFoundException {
        this(bgapi, new BLE112Connections(bgapi), hubs);
    }

    /**
     * Create a new SBrick API that can be used to access all SBricks. This is
     * the detailed constructor that is not usually used. Instead, consider
     * using one of the other convenience constructors.
     * 
     * @param bgapi
     *            The BGAPI interface that we can use to send commands.
     * @param ble112Connections
     *            The BLE112 connection manager that maintains connections for
     *            us.
     * @param hubs
     *            The definition of all SBrick hubs. Unlike WeDo (for example)
     *            SBrick's protocol does not have facilities to detect what
     *            brick is connected on what port of the hub. Instead, we have
     *            to rely on that information being supplied.
     * @throws FileNotFoundException
     *             When the specified device could not be opened.
     */
    public SBricks(final BGAPI bgapi, final BLE112Connections ble112Connections,
            final Collection<Hub> hubs) throws FileNotFoundException {
        super();

        checkNotNull(bgapi, "null bgapi");
        this.bgapi = bgapi;
        bgapi.addListener(this);

        checkNotNull(bgapi, "null ble112Connections");
        this.ble112Connections = ble112Connections;

        checkNotNull(hubs, "null hubs");
        this.hubs.addAll(hubs);

        for (final Hub hub : hubs) {
            ble112Connections.add(hub.getBLE112Address());
        }
    }

    /**
     * @see java.lang.AutoCloseable#close()
     */
    @Override
    public void close() throws Exception {
        ble112Connections.close();
        bgapi.disconnect();
    }

    /**
     * Reset the BLE112 device.
     */
    public void reset() {
        actuator(null, null, (byte) 0x00);
    }

    /**
     * Run the motor on all ports.
     * 
     * @param speed
     *            How fast to run the motor.
     */
    public void motor(final byte speed) {
        actuator(null, MOTOR, speed);
    }

    /**
     * Run the motor on port A.
     * 
     * @param speed
     *            How fast to run the motor.
     */
    public void motorA(final byte speed) {
        actuator('A', MOTOR, speed);
    }

    /**
     * Run the motor on port B.
     * 
     * @param speed
     *            How fast to run the motor.
     */
    public void motorB(final byte speed) {
        actuator('B', MOTOR, speed);
    }

    /**
     * Run the motor on port C.
     * 
     * @param speed
     *            How fast to run the motor.
     */
    public void motorC(final byte speed) {
        actuator('C', MOTOR, speed);
    }

    /**
     * Run the motor on port D.
     * 
     * @param speed
     *            How fast to run the motor.
     */
    public void motorD(final byte speed) {
        actuator('D', MOTOR, speed);
    }

    /**
     * Light the light on all ports.
     * 
     * @param intensity
     *            How bright to light up.
     */
    public void light(final byte intensity) {
        actuator(null, LIGHT, intensity);
    }

    /**
     * Light the light on port A.
     * 
     * @param intensity
     *            How bright to light up.
     */
    public void lightA(final byte intensity) {
        actuator('A', LIGHT, intensity);
    }

    /**
     * Light the light on port B.
     * 
     * @param intensity
     *            How bright to light up.
     */
    public void lightB(final byte intensity) {
        actuator('B', LIGHT, intensity);
    }

    /**
     * Light the light on port C.
     * 
     * @param intensity
     *            How bright to light up.
     */
    public void lightC(final byte intensity) {
        actuator('C', LIGHT, intensity);
    }

    /**
     * Light the light on port D.
     * 
     * @param intensity
     *            How bright to light up.
     */
    public void lightD(final byte intensity) {
        actuator('D', LIGHT, intensity);
    }

    /**
     * Run all actuators on all ports.
     * 
     * @param value
     *            How hard to drive the actuators.
     */
    public void all(final byte value) {
        actuator(null, null, value);
    }

    /**
     * Run all actuators on port A.
     * 
     * @param value
     *            How hard to drive the actuators.
     */
    public void allA(final byte value) {
        actuator('A', null, value);
    }

    /**
     * Run all actuators on port B.
     * 
     * @param value
     *            How hard to drive the actuators.
     */
    public void allB(final byte value) {
        actuator('B', null, value);
    }

    /**
     * Run all actuators on port C.
     * 
     * @param value
     *            How hard to drive the actuators.
     */
    public void allC(final byte value) {
        actuator('C', null, value);
    }

    /**
     * Run all actuators on port D.
     * 
     * @param value
     *            How hard to drive the actuators.
     */
    public void allD(final byte value) {
        actuator('D', null, value);
    }

    /**
     * Set the specified actuator to the provided speed or light intensity. The
     * port and type are used as filter to identify what brick to address.
     * 
     * @param port
     *            The port to select, or <code>null</code> to select any port.
     * @param type
     *            The brick type to select, or <code>null</code> to select any
     *            brick type.
     * @param value
     *            The speed or intensity value to set the brick to.
     */
    private void actuator(final Character port, final Type type,
            final byte value) {
        checkArgument(port == null || (port >= FIRST_PORT && port <= MAX_PORT),
                "invalid port %s", port);

        for (final Hub hub : hubs) {
            final Integer connection = ble112Connections
                    .getConnection(hub.getBLE112Address());
            if (connection != null) {
                actuator(connection, hub, port, type, value);
            }
        }
    }

    private void actuator(final int connection, @NonNull final Hub hub,
            final Character port, final Type type, final byte value) {
        for (final Brick brick : hub.getBricks()) {
            if ((port == null || port.equals(brick.getPort()))
                    && (type == null || type.equals(brick.getType()))) {
                actuatorValueMemory.write(hub, brick.getPort(), value);
            }
        }

        final byte[] data = new byte[5];
        data[0] = actuatorValueMemory.read(hub, 'A');
        data[1] = actuatorValueMemory.read(hub, 'B');
        data[2] = actuatorValueMemory.read(hub, 'C');
        data[3] = actuatorValueMemory.read(hub, 'D');
        data[4] = (byte) 0x00; // XXX We do not support the SBrick led yet

        bgapi.send_attclient_attribute_write(connection, HANDLE_QUICKDRIVE,
                data);
    }
}
