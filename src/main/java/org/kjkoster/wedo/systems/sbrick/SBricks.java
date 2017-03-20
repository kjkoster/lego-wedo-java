package org.kjkoster.wedo.systems.sbrick;

import static java.lang.System.out;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

import org.kjkoster.wedo.bricks.Brick;
import org.kjkoster.wedo.transport.ble112.ProtocolLogger;
import org.kjkoster.wedo.transport.usb.HubHandle;
import org.thingml.bglib.BGAPI;
import org.thingml.bglib.BGAPIDefaultListener;
import org.thingml.bglib.BGAPITransport;

/**
 * A class to represent the collection of SBricks and SBrick Pluses.
 *
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
public class SBricks extends BGAPIDefaultListener implements Closeable {
    private final File ble112Device;
    /**
     * The BGAPI interface.
     */
    private final BGAPI bgapi;

    /**
     * @param ble112Device
     *            The device that the BLE112 is represented as on your system.
     * @param verbose
     *            If <code>true</code>, log all BLE messages.
     * @throws FileNotFoundException
     *             When the specified device could not be opened.
     */
    public SBricks(final File ble112Device, final boolean verbose)
            throws FileNotFoundException {
        super();

        this.ble112Device = ble112Device;
        
        final BGAPITransport bgapiTransport = new BGAPITransport(
                new FileInputStream(ble112Device),
                new FileOutputStream(ble112Device));
        this.bgapi = new BGAPI(bgapiTransport);
        if (verbose) {
            bgapi.addListener(new ProtocolLogger());
        }
        bgapi.addListener(this);

        if (verbose) {
            bgapi.send_system_get_info();
        }
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_system_boot(int, int, int,
     *      int, int, int, int)
     */
    @Override
    public void receive_system_boot(int major, int minor, int patch, int build,
            int ll_version, int protocol_version, int hw) {
        receive_system_get_info(major, minor, patch, build, ll_version,
                protocol_version, hw);
    }

    /**
     * @see org.thingml.bglib.BGAPIListener#receive_system_get_info(int, int,
     *      int, int, int, int, int)
     */
    @Override
    public void receive_system_get_info(int major, int minor, int patch,
            int build, int ll_version, int protocol_version, int hw) {
        out.printf(
                "%s: version %d.%d.%d-%d, ll version: %d, protocol: %d, hardware: %d.\n",
                ble112Device, major, minor, patch, build, ll_version,
                protocol_version, hw);
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
     * @return All the bricks, neatly laid out in a map.
     */
    public Map<HubHandle, Brick[]> readAll() {
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
        throw new Error("NOT IMPLEMENTED..."); // TODO
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
