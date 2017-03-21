package org.kjkoster.wedo.systems.sbrick;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.System.out;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.kjkoster.wedo.bricks.Hub;
import org.kjkoster.wedo.transport.ble112.ProtocolLogger;
import org.thingml.bglib.BDAddr;
import org.thingml.bglib.BGAPI;
import org.thingml.bglib.BGAPIDefaultListener;
import org.thingml.bglib.BGAPITransport;

/**
 * A class to represent the collection of SBricks and SBrick Pluses.
 *
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
public class SBricks extends BGAPIDefaultListener implements Closeable {
    static final int CONN_INTERVAL_MIN = 0x3c; // XXX magic...
    static final int CONN_INTERVAL_MAX = 0x3c; // XXX magic...
    static final int CONN_TIMEOUT = 0x64; // XXX magic...
    static final int CONN_LATENCY = 0x00; // XXX magic...
    static final int CONN_ADDR_TYPE = 1; // XXX check name and document

    private final List<Hub> hubs = new ArrayList<>();

    /**
     * The BGAPI interface.
     */
    private final BGAPI bgapi;

    /**
     * @param ble112Device
     *            The device that the BLE112 is represented as on your system.
     * @param verbose
     *            If <code>true</code>, log all BLE messages.
     * @param hubs
     *            The definition of all SBrick hubs. Unlike WeDo (for example)
     *            SBrick's protocol does not have facilities to detect what
     *            brick is connected on what port of the hub. Instead, we have
     *            to rely on that information being supplied.
     * @throws FileNotFoundException
     *             When the specified device could not be opened.
     */
    public SBricks(final File ble112Device, final boolean verbose,
            final Set<Hub> hubs) throws FileNotFoundException {
        super();

        checkNotNull(ble112Device, "null ble112 device");
        final BGAPITransport bgapiTransport = new BGAPITransport(
                new FileInputStream(ble112Device),
                new FileOutputStream(ble112Device));
        this.bgapi = new BGAPI(bgapiTransport);
        if (verbose) {
            bgapi.addListener(new ProtocolLogger());
        }
        bgapi.addListener(this);

        checkNotNull(hubs, "null hubs");
        this.hubs.addAll(hubs);
        if (this.hubs.size() > 0) {
            // we only connect to the first hub. The response messages will
            // trigger further connections.
            bgapi.send_gap_connect_direct(
                    BDAddr.fromString(this.hubs.get(0).getPath()),
                    CONN_ADDR_TYPE, CONN_INTERVAL_MIN, CONN_INTERVAL_MAX,
                    CONN_TIMEOUT, CONN_LATENCY);
        }
    }

    /**
     * @see java.io.Closeable#close()
     */
    @Override
    public void close() throws IOException {
        bgapi.removeListener(this);
        bgapi.disconnect();
    }

    /**
     * Read a map of all the bricks. Find any SBrick hubs and construct an
     * object for each of them. Hubs can be plugged in and out at any time, so
     * it is a surprise how many bricks we get every time.
     * <p>
     * While scanning we disable regular message handling, so as to not confuse
     * the connection handling. Note that scanning and regular operation do not
     * work well together. They are also not thread safe.
     * <p>
     * Best either scan or do regular operations, but not both.
     * 
     * @return All the hubs.
     */
    public Collection<Hub> readAll() {
        final SBrickScanner sbrickScanner = new SBrickScanner(bgapi);
        try {
            bgapi.removeListener(this);
            bgapi.addListener(sbrickScanner);

            return sbrickScanner.readAll();
        } finally {
            bgapi.removeListener(sbrickScanner);
            bgapi.addListener(this);
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
        bgapi.send_attclient_attribute_write(connection, 0x001a, data);
    }

    /**
     * @param parseByte
     */
    public void motor(byte parseByte) {
        throw new Error("NOT IMPLEMENTED..."); // TODO
    }

    /**
     * @param parseByte
     */
    public void motorB(byte parseByte) {
        throw new Error("NOT IMPLEMENTED..."); // TODO
    }

    /**
     * @param parseByte
     */
    public void light(byte parseByte) {
        throw new Error("NOT IMPLEMENTED..."); // TODO
    }

    /**
     * @param parseByte
     */
    public void lightA(byte parseByte) {
        throw new Error("NOT IMPLEMENTED..."); // TODO
    }

    /**
     * @param parseByte
     */
    public void lightB(byte parseByte) {
        throw new Error("NOT IMPLEMENTED..."); // TODO
    }

    /**
     * @param parseByte
     */
    public void all(byte parseByte) {
        throw new Error("NOT IMPLEMENTED..."); // TODO
    }

    /**
     * @param parseByte
     */
    public void allA(byte parseByte) {
        throw new Error("NOT IMPLEMENTED..."); // TODO
    }

    /**
     * @param parseByte
     */
    public void allB(byte parseByte) {
        throw new Error("NOT IMPLEMENTED..."); // TODO
    }
}
