package org.kjkoster.wedo.transport.ble112;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.System.out;
import static java.lang.Thread.sleep;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.thingml.bglib.BDAddr;
import org.thingml.bglib.BGAPI;
import org.thingml.bglib.BGAPIDefaultListener;

import lombok.SneakyThrows;

/**
 * A central connection manager to establish and maintain Bluetooth Low Energy
 * connections. Hubs are passed to this connection manager, who will then be
 * able to provide connection ID's for further BGAPI calls.
 *
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
public class BLE112Connections extends BGAPIDefaultListener
        implements Closeable {
    /**
     * The minimum connection event interval. This value is measured in 1.25 ms
     * units and has a range of 7.5 ms to 4 seconds.
     */
    public static final int CONN_INTERVAL_MIN = 0x3c;

    /**
     * The maximum connection event interval. This value is measured in 1.25 ms
     * units and has a range of 7.5 ms to 4 seconds.
     */
    public static final int CONN_INTERVAL_MAX = 0x3c;

    /**
     * The connection's supervisor timeout. Shall be greater than the maximum
     * connection interval. This value is measured in 10 ms units and has a
     * range of 100 ms to 32 seconds.
     */
    public static final int CONN_TIMEOUT = 0x64;

    /**
     * Defines how many connection intervals the slave can skip. Setting this to
     * 0x00 prevents slave from skipping connection intervals on purpose. It has
     * a range of 0 to 500 events.
     */
    public static final int CONN_LATENCY = 0x00;

    private final BGAPI bgapi;
    private final Thread watchdog;

    /**
     * Our records of the connections that we maintain. This class works to
     * maintain a stable connection to each item on this map.
     */
    private final Map<BLE112Address, Integer> connections = new HashMap<>();

    /**
     * A set of connections that we should disconnect and the next opportunity.
     */
    private final Set<Integer> toDisconnect = new HashSet<>();

    /**
     * Set up a new connection manager.
     * 
     * @param bgapi
     *            The BGAPI to use.
     */
    @SneakyThrows
    public BLE112Connections(final BGAPI bgapi) {
        super();

        checkNotNull(bgapi, "null bgapi");
        this.bgapi = bgapi;

        // clear any lingering connections before we start connecting
        toDisconnect.add(0);
        toDisconnect.add(1);
        toDisconnect.add(2);

        bgapi.addListener(this);
        checkConnections();

        watchdog = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    for (;;) {
                        sleep(SECONDS.toMillis(2L));
                        checkConnections();
                    }
                } catch (InterruptedException e) {
                    return;// clean shutdown
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }, "BLE112 connections watchdog");
        watchdog.start();
    }

    /**
     * @see java.io.Closeable#close()
     */
    @Override
    @SneakyThrows
    public void close() throws IOException {
        toDisconnect.addAll(connections.values());
        connections.clear();

        toDisconnect.add(0);
        toDisconnect.add(1);
        toDisconnect.add(2);

        while (toDisconnect.size() > 0) {
            sleep(MILLISECONDS.toMillis(100L));
        }

        watchdog.interrupt();
        bgapi.removeListener(this);
        watchdog.join();
    }

    private void checkConnections() {
        if (toDisconnect.size() > 0) {
            closeAnyConnection();
        } else {
            openAnyNewConnection();
        }
    }

    private void closeAnyConnection() {
        final Integer connection = toDisconnect.iterator().next();
        toDisconnect.remove(connection);

        if (connection != null) {
            bgapi.send_connection_disconnect(connection);
        }
    }

    /**
     * Connect to an SBrick. We pick a random, unconnected brick to connect to.
     * That way we don't get stuck trying to connect to bricks that never
     * respond.
     */
    private void openAnyNewConnection() {
        final List<BLE112Address> unconnected = new ArrayList<>(
                connections.size());
        for (final Map.Entry<BLE112Address, Integer> connection : connections
                .entrySet()) {
            if (connection.getValue() == null) {
                unconnected.add(connection.getKey());
            }
        }

        if (unconnected.size() == 0) {
            return; // nothing to connect, bail out...
        }

        Collections.shuffle(unconnected);
        final BLE112Address connection = unconnected.get(0);
        out.printf("ble112: connecting to %s...\n", connection);
        bgapi.send_gap_connect_direct(connection.getBDAddr(),
                connection.getAddress_type(), CONN_INTERVAL_MIN,
                CONN_INTERVAL_MAX, CONN_TIMEOUT, CONN_LATENCY);
    }

    /**
     * @see org.thingml.bglib.BGAPIDefaultListener#receive_connection_status(int,
     *      int, org.thingml.bglib.BDAddr, int, int, int, int, int)
     */
    @Override
    public void receive_connection_status(final int connection, final int flags,
            final BDAddr address, final int address_type,
            final int conn_interval, final int timeout, final int latency,
            final int bonding) {
        if (flags != 0x00) {
            // connected, remember the connection ID
            connections.put(new BLE112Address(address, address_type),
                    connection);
            out.printf("ble112: connection %d to %s.\n", connection,
                    address.toString());
        } else {
            // disconnected, clear the connection ID
            connections.put(new BLE112Address(address, address_type), null);
            out.printf("ble112: disconnected from %s.\n", address.toString());
        }

        checkConnections();
    }

    /**
     * @see org.thingml.bglib.BGAPIDefaultListener#receive_connection_disconnected(int,
     *      int)
     */
    @Override
    public void receive_connection_disconnected(final int connection,
            final int reason) {
        for (final Map.Entry<BLE112Address, Integer> conn : connections
                .entrySet()) {
            final Integer connectionId = conn.getValue();
            if (connectionId != null && connection == connectionId) {
                conn.setValue(null);
                out.printf("ble112: disconnected from %s.\n",
                        conn.getKey().toString());

                break; // found the entry, break the loop
            }
        }

        checkConnections();
    }

    /**
     * @see org.thingml.bglib.BGAPIDefaultListener#receive_gap_connect_direct(int,
     *      int)
     */
    @Override
    public void receive_gap_connect_direct(final int result,
            final int connection_handle) {
        switch (result) {
        case 0x0000: /* ok */
            break;
        case 0x0209: /* connection limit exceeded */
            // force a reconnect
            toDisconnect.add(connection_handle);
            break;
        default:
            out.printf("ble112: connection error 0x%04x.\n", result);
        }
    }

    /**
     * Add a collection of hubs to keep a connection to.
     * 
     * @param ble112Address
     *            The address of the hub to maintain a connection with.
     */
    public void add(final BLE112Address ble112Address) {
        connections.putIfAbsent(ble112Address, null);

        checkConnections();
    }

    /**
     * Find the connection ID for a particular hub.
     * 
     * @param ble112Address
     *            The hub to check the connection ID for.
     * @return The connection Id. This ID can be used for further BGAPI calls.
     *         If there is no current connection to the specified hub, this
     *         method returns <code>null</code>.
     */
    public Integer getConnection(final BLE112Address ble112Address) {
        return connections.get(ble112Address);
    }
}
