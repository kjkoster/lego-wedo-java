package org.kjkoster.wedo.usb;

import static com.codeminders.hidapi.ClassPathLibraryLoader.loadNativeHIDLibrary;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.logging.Level.FINE;
import static java.util.logging.Level.WARNING;
import static java.util.logging.Logger.getLogger;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.codeminders.hidapi.HIDDevice;
import com.codeminders.hidapi.HIDDeviceInfo;
import com.codeminders.hidapi.HIDManager;

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

    private static Logger log = getLogger(Usb.class.getName());

    private static volatile boolean hidLibraryLoaded = false;

    private final Map<String, HIDDevice> openDevices = new HashMap<>();

    /**
     * Initialise a new USB abstraction that filters on a given USB vendor and
     * product ID.
     * 
     * @throws IOException
     *             When there was a problem loading the USB subsystem or the
     *             native libraries.
     */
    public Usb() throws IOException {
        synchronized (Usb.class) {
            if (!hidLibraryLoaded) {
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
     * @param bytesToRead
     *            The number of bytes to read.
     * @return A map with a data entry for each USB device handle.
     * @throws IOException
     *             When there was a problem reading from the USB subsystem.
     */
    public Map<Handle, byte[]> readFromAll(final int bytesToRead)
            throws IOException {
        checkArgument(bytesToRead > 0);

        final Map<Handle, byte[]> packets = new HashMap<>();
        for (final HIDDeviceInfo hidDeviceInfo : HIDManager.getInstance()
                .listDevices()) {
            if (hidDeviceInfo.getVendor_id() == VENDORID_LEGO
                    && hidDeviceInfo.getProduct_id() == PRODUCTID_WEDOHUB) {
                readFromHandle(new Handle(hidDeviceInfo.getPath(),
                        hidDeviceInfo.getProduct_string()), bytesToRead,
                        packets);
            }
        }

        return packets;
    }

    private void readFromHandle(final Handle handle, final int bytesToRead,
            final Map<Handle, byte[]> packets) {
        try {
            final byte[] buffer = new byte[bytesToRead];
            final int bytesRead = open(handle).readTimeout(buffer,
                    (int) MILLISECONDS.toMillis(100L));
            if (bytesRead != bytesToRead) {
                // there was a time-out, and we did not get a packet.
                log.warning(format(
                        "Expected %d bytes but received %d reading %s, timeout?",
                        bytesToRead, bytesRead, handle));
                return;
            }

            if (log.isLoggable(FINE)) {
                String logline = format("read %d bytes from %s:", bytesRead,
                        handle);
                for (int i = 0; i < bytesToRead; i++) {
                    logline += format(" 0x%02x", buffer[i]);
                }
                log.fine(logline);
            }

            packets.put(handle, buffer);
        } catch (IOException e) {
            log.log(WARNING,
                    format("unexpected exception reading from %s: %s", handle,
                            e.getMessage()), e);
        }
    }

    /**
     * Write a packet of bytes to the USB device. If the write fails, an
     * exception is thrown.
     * 
     * @param handle
     *            The USB device handle of the device to write to.
     * @param buffer
     *            The bytes to write.
     * @throws IOException
     *             When the write failed.
     */
    public void write(final Handle handle, final byte[] buffer)
            throws IOException {
        checkNotNull(handle);
        checkNotNull(buffer);
        checkArgument(buffer.length > 0);

        if (log.isLoggable(FINE)) {
            String logline = format("writing %d bytes to %s:", buffer.length,
                    handle);
            for (int i = 0; i < buffer.length; i++) {
                logline += format(" 0x%02x", buffer[i]);
            }
            log.fine(logline);
        }

        final int bytesWritten = open(handle).write(buffer);
        if (bytesWritten != buffer.length) {
            throw new IOException(format(
                    "expected to write %d bytes to %s, but wrote %d",
                    buffer.length, handle, bytesWritten));
        }
    }

    private synchronized HIDDevice open(final Handle handle) throws IOException {
        HIDDevice hidDevice = openDevices.get(handle.getPath());
        if (hidDevice == null) {
            hidDevice = HIDManager.getInstance().openByPath(handle.getPath());
            if (hidDevice == null) {
                log.warning(format(
                        "unable to open device %s, claimed by another application?",
                        handle));
            }
            openDevices.put(handle.getPath(), hidDevice);
        }
        return hidDevice;
    }

    /**
     * @see java.io.Closeable#close()
     */
    @Override
    public synchronized void close() throws IOException {
        for (final HIDDevice hidDevice : openDevices.values()) {
            hidDevice.close();
        }
        HIDManager.getInstance().release();
    }
}
