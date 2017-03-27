package org.kjkoster.wedo.transport.usb;

import static com.codeminders.hidapi.ClassPathLibraryLoader.loadNativeHIDLibrary;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;
import static java.lang.System.err;
import static java.lang.System.out;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.codeminders.hidapi.HIDDevice;
import com.codeminders.hidapi.HIDDeviceInfo;
import com.codeminders.hidapi.HIDManager;

import lombok.SneakyThrows;

/**
 * Encapsulate all the USB functions for this library. This class is geared
 * heavily to supporting the LEGO WeDo functions. It is not a general purpose
 * USB API layer.
 * <p>
 * On Ubuntu I found a problem that rapidly opening and closing devices in a
 * tight loop would lead to a hard JVM crash. This class works around that bug
 * by keeping open devices cached until the whole USB class is closed.
 * 
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
public class Usb implements Closeable {
    private static final int VENDORID_LEGO = 0x0694;
    private static final int PRODUCTID_WEDOHUB = 0x0003;

    private static final int PACKETSIZE = 8;

    private static volatile boolean hidLibraryLoaded = false;

    private final boolean verbose;
    private final Map<String, HIDDevice> openDevices = new HashMap<>();

    /**
     * Initialise a new USB abstraction that filters on a given USB vendor and
     * product ID.
     * 
     * @param verbose
     *            Print a trace of all interaction with the USB port.
     */
    @SneakyThrows
    public Usb(final boolean verbose) {
        this.verbose = verbose;

        synchronized (Usb.class) {
            if (!hidLibraryLoaded) {
                if (verbose) {
                    out.println("  USB loading native HID library");
                }
                hidLibraryLoaded = loadNativeHIDLibrary();
                if (!hidLibraryLoaded) {
                    throw new IOException("unable to load native HID library");
                }
            }
        }

        // just to force it to load.
        HIDManager.getInstance();
    }

    /**
     * Read a packet from each device that matches our vendor ID and product ID
     * filter.
     * 
     * @return A map with a data entry for each USB device handle.
     */
    @SneakyThrows
    public Map<HubHandle, byte[]> readFromAll() {
        final Map<HubHandle, byte[]> packets = new HashMap<>();
        for (final HIDDeviceInfo hidDeviceInfo : HIDManager.getInstance()
                .listDevices()) {
            if (hidDeviceInfo.getVendor_id() == VENDORID_LEGO
                    && hidDeviceInfo.getProduct_id() == PRODUCTID_WEDOHUB) {
                read(hidDeviceInfo, packets);
            }
        }

        return packets;
    }

    private void read(final HIDDeviceInfo hidDeviceInfo,
            final Map<HubHandle, byte[]> packets) {
        try {
            final String productName = hidDeviceInfo.getProduct_string();
            if (productName == null) {
                // Typically a USB device permissions issue under Linux. If that
                // is the case, you may need udev rules.
                err.printf(
                        "unable to read product name from %s, permission issue?",
                        hidDeviceInfo.getPath());
                return;
            }
            final HubHandle hubHandle = new HubHandle(hidDeviceInfo.getPath(),
                    productName);

            final byte[] buffer = new byte[PACKETSIZE];
            final int bytesRead = open(hubHandle).readTimeout(buffer,
                    (int) MILLISECONDS.toMillis(100L));
            if (bytesRead != PACKETSIZE) {
                // there was a time-out, and we did not get a packet.
                err.printf(
                        "expected %d bytes but received %d reading %s, timeout?",
                        PACKETSIZE, bytesRead, hubHandle);
                return;
            }

            if (verbose) {
                out.printf(
                        "  USB read  %s: 0x%02x 0x%02x [value A: 0x%02x] [id A: 0x%02x] [value B: 0x%02x] [id B: 0x%02x] 0x%02x 0x%02x\n",
                        hubHandle, buffer[0], buffer[1], buffer[2], buffer[3],
                        buffer[4], buffer[5], buffer[6], buffer[7]);
            }

            packets.put(hubHandle, buffer);
        } catch (IOException e) {
            err.printf("unexpected exception reading from %s: %s",
                    hidDeviceInfo.getPath(), e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Write a packet of bytes to the USB device. If the write fails, an
     * exception is thrown.
     * 
     * @param hubHandle
     *            The USB device handle of the device to write to.
     * @param buffer
     *            The bytes to write.
     */
    @SneakyThrows
    public void write(final HubHandle hubHandle, final byte[] buffer) {
        checkNotNull(hubHandle);
        checkNotNull(buffer);
        checkArgument(buffer.length == 9);

        if (verbose) {
            out.printf(
                    "  USB write %s: 0x%02x 0x%02x [value A: 0x%02x] [value B: 0x%02x] 0x%02x 0x%02x 0x%02x 0x%02x 0x%02x\n",
                    hubHandle, buffer[0], buffer[1], buffer[2], buffer[3],
                    buffer[4], buffer[5], buffer[6], buffer[7], buffer[8]);
        }

        final int bytesWritten = open(hubHandle).write(buffer);
        if (bytesWritten != buffer.length) {
            throw new IOException(
                    format("expected to write %d bytes to %s, but wrote %d",
                            buffer.length, hubHandle, bytesWritten));
        }
    }

    private synchronized HIDDevice open(final HubHandle hubHandle)
            throws IOException {
        HIDDevice hidDevice = openDevices.get(hubHandle.getPath());
        if (hidDevice == null) {
            hidDevice = HIDManager.getInstance()
                    .openByPath(hubHandle.getPath());
            if (hidDevice == null) {
                err.printf(
                        "unable to open device %s, claimed by another application?",
                        hubHandle);
            }
            openDevices.put(hubHandle.getPath(), hidDevice);
        }
        return hidDevice;
    }

    /**
     * @see java.io.Closeable#close()
     */
    @Override
    @SneakyThrows
    public synchronized void close() {
        for (final HIDDevice hidDevice : openDevices.values()) {
            hidDevice.close();
        }
        HIDManager.getInstance().release();
    }
}
