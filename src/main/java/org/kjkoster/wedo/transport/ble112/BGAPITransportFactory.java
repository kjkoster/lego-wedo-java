package org.kjkoster.wedo.transport.ble112;

import static com.fazecast.jSerialComm.SerialPort.TIMEOUT_READ_SEMI_BLOCKING;
import static java.lang.System.getProperty;
import static java.lang.System.out;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.thingml.bglib.BGAPITransport;

import com.fazecast.jSerialComm.SerialPort;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

/**
 * A factory that generates BGAPI transports in a platform-dependent manner.
 * This class encapsulates serial port handling on the various platforms.
 * <p>
 * On MAC OS X, the BLE112 device can simply be opened using regular file
 * streams.
 * <p>
 * On other platforms we need to set serial line parameters before we can use
 * the port.
 *
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
@UtilityClass
public class BGAPITransportFactory {
    private static final String osName = getProperty("os.name").toLowerCase();

    /**
     * Factory to generate BLE112 devices in a platform-independent manner.
     * 
     * @param ble112Device
     *            The file node for the BLE112 device.
     * @return The internal representation of the BLE112 device.
     */
    public static BGAPITransport newBGAPITransport(final File ble112Device) {
        out.printf("using os.name %s\n", osName); // XXX
        switch (osName) {
        case "macosx":
            return newBGAPITransport_MacOSX(ble112Device);
        default:
            return newBGAPITransport_others(ble112Device);
        }
    }

    @SneakyThrows
    private static BGAPITransport newBGAPITransport_MacOSX(
            final File ble112Device) {
        return new BGAPITransport(new FileInputStream(ble112Device),
                new FileOutputStream(ble112Device));
    }

    @SneakyThrows
    private static BGAPITransport newBGAPITransport_others(
            final File ble112Device) {
        out.printf("Detected %d serial ports:\n",
                SerialPort.getCommPorts().length); // XXX
        for (final SerialPort serialPort : SerialPort.getCommPorts()) {
            out.printf("  port: %s, /dev/%s, baudrate: %d, timeout: %d\n",
                    serialPort.getDescriptivePortName(),
                    serialPort.getSystemPortName(), serialPort.getBaudRate(), serialPort.getReadTimeout()); // XXX
        }

        final SerialPort ble112Port = SerialPort
                .getCommPort(ble112Device.getAbsolutePath());
        ble112Port.openPort();
        ble112Port.setComPortTimeouts(TIMEOUT_READ_SEMI_BLOCKING, 100,
                0 /* XXX magic */);
        return new BGAPITransport(ble112Port.getInputStream(),
                ble112Port.getOutputStream());

        // XXX close()?
    }
}
