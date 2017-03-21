package org.kjkoster.wedo.systems.sbrick;

import static java.lang.Integer.parseInt;
import static java.lang.String.format;
import static java.lang.System.err;
import static java.lang.System.exit;
import static java.lang.System.out;
import static java.util.concurrent.TimeUnit.SECONDS;
import static lombok.Lombok.sneakyThrow;
import static org.kjkoster.wedo.bricks.Brick.Type.UNKNOWN;
import static org.kjkoster.wedo.systems.sbrick.SBricks.CONN_INTERVAL_MAX;
import static org.kjkoster.wedo.systems.sbrick.SBricks.CONN_INTERVAL_MIN;
import static org.kjkoster.wedo.systems.sbrick.SBricks.CONN_LATENCY;
import static org.kjkoster.wedo.systems.sbrick.SBricks.CONN_TIMEOUT;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;

import org.kjkoster.wedo.bricks.Brick;
import org.kjkoster.wedo.bricks.Hub;
import org.kjkoster.wedo.transport.ble112.BLE112Address;
import org.thingml.bglib.BDAddr;
import org.thingml.bglib.BGAPI;
import org.thingml.bglib.BGAPIDefaultListener;

/**
 * A scanner that searches for SBrick and SBrick Plus BLE hubs. Scanning,
 * connecting and then interrogating the SBricks is rather involved due to the
 * asynchronous nature of BGAPI. We can only send commands when previous
 * commands gave a response. This class uses the responses to trigger further
 * actions. This gives a rather brittle process that is hard to read, but it
 * seems to work quite well for most circumstances.
 *
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
class SBrickScanner extends BGAPIDefaultListener {
    private static final int HANDLE_VENDOR = 0x10;
    private static final int HANDLE_VERSION = 0x0a;
    private static final int HANDLE_NAME = 0x03;

    /**
     * The BGAPI interface.
     */
    private final BGAPI bgapi;

    /**
     * All peripherals that responded to a scan request.
     */
    private final Queue<BLE112Address> ble112Addresses = new LinkedList<>();

    /**
     * A flag indicating that we are done interrogating. This flag is switched
     * to <code>true</code> when the last SBrick has been added to the found
     * bricks.
     */
    private volatile boolean done = false;

    /**
     * The address of the peripheral that we are currently connected to. Used
     * during the interrogation process to remember the address across messages.
     */
    private String connectedAddress = "";

    /**
     * The firmware version of the peripheral that we are currently connected
     * to. Used during the interrogation process to remember the firmware
     * version across messages.
     */
    private String version = "";

    /**
     * The complete and supported SBricks that we found so far.
     */
    private final Collection<Hub> foundHubs = new ArrayList<>();

    /**
     * Start a new scanner to look for SBricks.
     * 
     * @param bgapi
     *            The BLE112 API to use.
     */
    SBrickScanner(final BGAPI bgapi) {
        super();

        this.bgapi = bgapi;
    }

    private void sleep() {
        try {
            Thread.sleep(SECONDS.toMillis(2L));
        } catch (InterruptedException e) {
            throw sneakyThrow(e);
        }
    }

    /**
     * Read a map of all the bricks. We scan for SBricks. Unfortunately SBricks
     * do not support detection of the bricks plugged into them, so we get an
     * empty list back. SBricks can be switched on and off at any time, so it is
     * a surprise how many bricks we get every time.
     * 
     * @return All the bricks, neatly laid out in a map.
     */
    Collection<Hub> readAll() {
        // XXX trigger a version report from the BLE112 device.

        bgapi.send_gap_set_scan_parameters(10, 250, 1 /* XXX magic numbers */);
        bgapi.send_gap_discover(1 /* gap_discover_generic */);

        try {
            sleep();
        } finally {
            // the response to <code>send_gap_end_procedure()</code> triggers
            // the connection and interrogation process. See
            // <code>receive_gap_end_procedure</code>, below.
            bgapi.send_gap_end_procedure();
        }

        // wait for the addresses to all be interrogated
        while (!done) {
            sleep();
        }

        return foundHubs;
    }

    private void connectNextAddress() {
        if (ble112Addresses.isEmpty()) {
            done = true;
        } else {
            final BLE112Address ble112Address = ble112Addresses.remove();
            connectedAddress = ble112Address.toString();
            bgapi.send_gap_connect_direct(ble112Address.getBDAddr(),
                    ble112Address.getAddress_type(), CONN_INTERVAL_MIN,
                    CONN_INTERVAL_MAX, CONN_TIMEOUT, CONN_LATENCY);
        }
    }

    /**
     * Add the scan result to the candidate peripherals to interrogate later on.
     * We only add a peripheral if we don't already have it on the list. We will
     * receive multiple scan results for the same address.
     * 
     * @see org.thingml.bglib.BGAPIDefaultListener#receive_gap_scan_response(int,
     *      int, org.thingml.bglib.BDAddr, int, int, byte[])
     */
    @Override
    public void receive_gap_scan_response(int rssi, int packet_type,
            BDAddr sender, int address_type, int bond, byte[] data) {
        final BLE112Address ble112Address = new BLE112Address(sender,
                address_type);
        if (!ble112Addresses.contains(ble112Address)) {
            ble112Addresses.add(ble112Address);
        }
    }

    /**
     * @see org.thingml.bglib.BGAPIDefaultListener#receive_gap_end_procedure(int)
     */
    @Override
    public void receive_gap_end_procedure(int result) {
        connectNextAddress();
    }

    /**
     * @see org.thingml.bglib.BGAPIDefaultListener#receive_connection_status(int,
     *      int, org.thingml.bglib.BDAddr, int, int, int, int, int)
     */
    @Override
    public void receive_connection_status(int connection, int flags,
            BDAddr address, int address_type, int conn_interval, int timeout,
            int latency, int bonding) {
        if (flags != 0x00) {
            // connected, kick off the interrogation
            bgapi.send_attclient_read_by_handle(connection, HANDLE_VENDOR);
        } else {
            // disconnected, so move to the next item
            connectNextAddress();
        }
    }

    /**
     * @see org.thingml.bglib.BGAPIDefaultListener#receive_connection_disconnected(int,
     *      int)
     */
    @Override
    public void receive_connection_disconnected(int connection, int reason) {
        connectNextAddress();
    }

    /**
     * @see org.thingml.bglib.BGAPIDefaultListener#receive_attclient_attribute_value(int,
     *      int, int, byte[])
     */
    @Override
    public void receive_attclient_attribute_value(int connection, int atthandle,
            int type, byte[] value) {
        switch (atthandle) {
        case HANDLE_VENDOR:
            if (!"Vengit Ltd.".equals(new String(value))) {
                // not an SBrick
                bgapi.send_connection_disconnect(connection);
            }
            bgapi.send_attclient_read_by_handle(connection, HANDLE_VERSION);
            break;

        case HANDLE_VERSION:
            version = new String(value);
            final int major = parseInt(version.split("\\.")[0]);
            final int minor = parseInt(version.split("\\.")[1]);
            if (major <= 4 && minor <= 2) {
                // pre-4.3 firmwares are not supported, ignore it until it has a
                // newer firmware
                out.printf(
                        "Found an SBrick that has an older, unsupported firmware version. Use the\n"
                                + "official SBrick app to update its firmware first and then re-run \"wedo -list\".\n\n");
                bgapi.send_connection_disconnect(connection);
            }
            bgapi.send_attclient_read_by_handle(connection, HANDLE_NAME);
            break;

        case HANDLE_NAME:
            final Brick[] bricks = new Brick[4];
            for (int i = 0; i < 4; i++) {
                bricks[i] = new Brick((char) ('A' + i), UNKNOWN,
                        (byte) 0xff, (byte) 0xff);
            }
            foundHubs.add(new Hub(connectedAddress,
                    format("%s, V%s", new String(value), version), bricks));
            bgapi.send_connection_disconnect(connection);
            break;

        default:
            bgapi.send_connection_disconnect(connection);
        }
    }

    /**
     * This callback is called when a peripheral is not an SBrick and it gives
     * an error when we query a handle it does not support.
     * 
     * @see org.thingml.bglib.BGAPIDefaultListener#receive_attclient_procedure_completed(int,
     *      int, int)
     */
    @Override
    public void receive_attclient_procedure_completed(int connection,
            int result, int chrhandle) {
        bgapi.send_connection_disconnect(connection);
    }

    /**
     * We get this one occasionally when the program was interrupted at halfway
     * an operation.
     * 
     * @see org.thingml.bglib.BGAPIDefaultListener#receive_hardware_adc_read(int)
     */
    @Override
    public void receive_hardware_adc_read(int result) {
        bgapi.send_system_reset(0);
        sleep();
        err.println("BLE112 device reported error 0x04x.");
        exit(1);
    }
}
