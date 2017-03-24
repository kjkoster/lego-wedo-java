package org.kjkoster.wedo.systems.sbrick;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.System.out;
import static org.kjkoster.wedo.bricks.Brick.Type.MOTOR;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.kjkoster.wedo.bricks.Hub;
import org.kjkoster.wedo.transport.ble112.BLE112Connections;
import org.thingml.bglib.BGAPI;
import org.thingml.bglib.BGAPIDefaultListener;

/**
 * A class to represent the collection of SBricks and SBrick Pluses.
 *
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
public class SBricks extends BGAPIDefaultListener {
    private final List<Hub> hubs = new ArrayList<>();

    private final BGAPI bgapi;

    private final BLE112Connections ble112Connections;

    /**
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
     * Reset the BLE112 device.
     */
    public void reset() {
        bgapi.send_system_reset(0);
    }

    /**
     * @see org.thingml.bglib.BGAPIDefaultListener#receive_system_reset()
     */
    @Override
    public void receive_system_reset() {
        out.printf("BLE112 reset.\n");
    }

    /**
     * Run the motor on port A.
     * 
     * @param speed
     *            How fast to run the motor.
     */
    public void motorA(final byte speed) {
        final byte[] data = { 0x01, 0x00, 0x00, (byte) 0xfe };

        for (final Hub hub : hubs) {
            final Integer connection = ble112Connections
                    .getConnection(hub.getBLE112Address());
            if (connection != null && hub.getBrick('A').getType() == MOTOR) {
                bgapi.send_attclient_attribute_write(connection, 0x001a, data);
            }
        }
    }

    public void motor(byte parseByte) {
        throw new Error("NOT IMPLEMENTED..."); // TODO
    }

    public void motorB(byte parseByte) {
        throw new Error("NOT IMPLEMENTED..."); // TODO
    }

    public void light(byte parseByte) {
        throw new Error("NOT IMPLEMENTED..."); // TODO
    }

    public void lightA(byte parseByte) {
        throw new Error("NOT IMPLEMENTED..."); // TODO
    }

    public void lightB(byte parseByte) {
        throw new Error("NOT IMPLEMENTED..."); // TODO
    }

    public void all(byte parseByte) {
        throw new Error("NOT IMPLEMENTED..."); // TODO
    }

    public void allA(byte parseByte) {
        throw new Error("NOT IMPLEMENTED..."); // TODO
    }

    public void allB(byte parseByte) {
        throw new Error("NOT IMPLEMENTED..."); // TODO
    }

    public Collection<Hub> readAll() {
        throw new Error("NOT IMPLEMENTED..."); // TODO
    }
}
