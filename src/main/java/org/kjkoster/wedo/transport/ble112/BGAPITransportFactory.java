package org.kjkoster.wedo.transport.ble112;

import static com.google.common.base.Preconditions.checkArgument;
import static gnu.io.SerialPort.DATABITS_8;
import static gnu.io.SerialPort.FLOWCONTROL_RTSCTS_IN;
import static gnu.io.SerialPort.FLOWCONTROL_RTSCTS_OUT;
import static gnu.io.SerialPort.PARITY_NONE;
import static gnu.io.SerialPort.STOPBITS_1;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.thingml.bglib.BGAPITransport;

import com.jamierf.rxtx.RXTXLoader;
import com.jamierf.rxtx.RXTXLoader.OperatingSystem;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
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
    private static boolean nativeLibraryLoaded = false;

    /**
     * Factory to generate BLE112 devices in a platform-independent manner.
     * 
     * @param ble112Device
     *            The file node for the BLE112 device.
     * @return The internal representation of the BLE112 device.
     */
    public static BGAPITransport newBGAPITransport(final File ble112Device) {
        switch (OperatingSystem.get()) {
        case MAC_OSX:
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
        if (!nativeLibraryLoaded) {
            RXTXLoader.load();
            nativeLibraryLoaded = true;
        }

        final SerialPort serialPort = openSerialPort(ble112Device);
        return new BGAPITransport(serialPort.getInputStream(),
                serialPort.getOutputStream());
    }

    @SneakyThrows
    private static SerialPort openSerialPort(final File ble112Device) {
        final CommPortIdentifier portIdentifier = CommPortIdentifier
                .getPortIdentifier(ble112Device.getAbsolutePath());
        if (portIdentifier.isCurrentlyOwned()) {
            throw new IOException(ble112Device + " is in use");
        }

        final CommPort commPort = portIdentifier.open("BLED112", 2000);
        checkArgument(commPort instanceof SerialPort, "%s is not a serial port",
                ble112Device);

        final SerialPort serialPort = (SerialPort) commPort;
        serialPort.setSerialPortParams(115200, DATABITS_8, STOPBITS_1,
                PARITY_NONE);
        serialPort.setFlowControlMode(
                FLOWCONTROL_RTSCTS_IN | FLOWCONTROL_RTSCTS_OUT);
        serialPort.setRTS(true);

        return serialPort;
    }
}
