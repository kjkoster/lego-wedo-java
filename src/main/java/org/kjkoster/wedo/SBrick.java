package org.kjkoster.wedo;

import static com.fazecast.jSerialComm.SerialPort.TIMEOUT_READ_SEMI_BLOCKING;
import static com.fazecast.jSerialComm.SerialPort.getCommPort;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.Byte.parseByte;
import static java.lang.System.out;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.kjkoster.wedo.bricks.Brick.FIRST_PORT;
import static org.kjkoster.wedo.bricks.Brick.Type.NOT_CONNECTED;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.kjkoster.wedo.bricks.Brick;
import org.kjkoster.wedo.bricks.Brick.Type;
import org.kjkoster.wedo.bricks.Hub;
import org.kjkoster.wedo.systems.sbrick.SBrickScanner;
import org.kjkoster.wedo.systems.sbrick.SBricks;
import org.kjkoster.wedo.transport.ble112.BLE112Connections;
import org.kjkoster.wedo.transport.ble112.ProtocolLogger;
import org.thingml.bglib.BGAPI;
import org.thingml.bglib.BGAPITransport;

import com.fazecast.jSerialComm.SerialPort;

import lombok.Cleanup;

/**
 * The Vengit SBrick and SBrick Plus command line tool's main entry point.
 * 
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
public class SBrick {
    private static final String VERBOSE = "v";
    private static final String BLE112DEVICE = "ble112";
    private static final String HUB = "hub";

    private static final String RESET = "reset";
    private static final String LIST = "list";

    private static final String ALL = "all";
    private static final String ALL_A = "allA";
    private static final String ALL_B = "allB";
    private static final String ALL_C = "allC";
    private static final String ALL_D = "allD";

    private static final String LIGHT = "light";
    private static final String LIGHT_A = "lightA";
    private static final String LIGHT_B = "lightB";
    private static final String LIGHT_C = "lightC";
    private static final String LIGHT_D = "lightD";

    private static final String MOTOR = "motor";
    private static final String MOTOR_A = "motorA";
    private static final String MOTOR_B = "motorB";
    private static final String MOTOR_C = "motorC";
    private static final String MOTOR_D = "motorD";

    /**
     * The main application entry point.
     * 
     * @param args
     *            The command line arguments, as documented in
     *            <code>usage()</code>.
     * @throws ParseException
     *             When the command line could not be parsed.
     * @throws FileNotFoundException
     *             When the specified BLE112 device could not be opened.
     */
    public static void main(final String[] args)
            throws ParseException, FileNotFoundException {
        final Options options = setOptions();

        final CommandLine commandLine = parseCommandLine(options, args);

        final boolean verbose = commandLine.hasOption(VERBOSE);
        final File ble112Device = commandLine.hasOption(BLE112DEVICE)
                ? new File(commandLine.getOptionValue(BLE112DEVICE)) : null;

        checkNotNull(ble112Device, "null ble112 device");
        final SerialPort ble112Port = getCommPort(
                ble112Device.getAbsolutePath());
        ble112Port.setBaudRate(115200);

        BGAPI bgapi = null;

        try {
            ble112Port.openPort();
            ble112Port.setComPortTimeouts(TIMEOUT_READ_SEMI_BLOCKING,
                    (int) MILLISECONDS.toMillis(100L) /* read timeout */,
                    (int) MILLISECONDS.toMillis(0L) /* write timeout */);
            final BGAPITransport bgapiTransport = new BGAPITransport(
                    ble112Port.getInputStream(), ble112Port.getOutputStream());
            bgapi = new BGAPI(bgapiTransport);
            if (verbose) {
                bgapi.addListener(new ProtocolLogger());
            }

            if (commandLine.hasOption(LIST)) {
                list(verbose, bgapi);
            } else {
                final Collection<Hub> hubs = new ArrayList<>();
                hubs.add(parseBrick(commandLine.getOptionValue(HUB)));

                @Cleanup
                final BLE112Connections ble112Connections = new BLE112Connections(
                        bgapi);
                final SBricks sBricks = new SBricks(bgapi, ble112Connections,
                        hubs);

                Thread.sleep(1000L);

                if (commandLine.hasOption(RESET)) {
                    sBricks.reset();
                } else if (commandLine.hasOption(MOTOR)) {
                    sBricks.motor(parseByte(commandLine.getOptionValue(MOTOR)));
                } else if (commandLine.hasOption(MOTOR_A)) {
                    Thread.sleep(1000L);
                    sBricks.motorA(
                            parseByte(commandLine.getOptionValue(MOTOR_A)));
                    Thread.sleep(1000L);
                } else if (commandLine.hasOption(MOTOR_B)) {
                    sBricks.motorB(
                            parseByte(commandLine.getOptionValue(MOTOR_B)));
                } else if (commandLine.hasOption(MOTOR_C)) {
                    sBricks.motorC(
                            parseByte(commandLine.getOptionValue(MOTOR_C)));
                } else if (commandLine.hasOption(MOTOR_D)) {
                    sBricks.motorD(
                            parseByte(commandLine.getOptionValue(MOTOR_D)));
                } else if (commandLine.hasOption(LIGHT)) {
                    sBricks.light(parseByte(commandLine.getOptionValue(LIGHT)));
                } else if (commandLine.hasOption(LIGHT_A)) {
                    sBricks.lightA(
                            parseByte(commandLine.getOptionValue(LIGHT_A)));
                } else if (commandLine.hasOption(LIGHT_B)) {
                    sBricks.lightB(
                            parseByte(commandLine.getOptionValue(LIGHT_B)));
                } else if (commandLine.hasOption(LIGHT_C)) {
                    sBricks.lightC(
                            parseByte(commandLine.getOptionValue(LIGHT_C)));
                } else if (commandLine.hasOption(LIGHT_D)) {
                    sBricks.lightD(
                            parseByte(commandLine.getOptionValue(LIGHT_D)));
                } else if (commandLine.hasOption(ALL)) {
                    sBricks.all(parseByte(commandLine.getOptionValue(ALL)));
                } else if (commandLine.hasOption(ALL_A)) {
                    sBricks.allA(parseByte(commandLine.getOptionValue(ALL_A)));
                } else if (commandLine.hasOption(ALL_B)) {
                    sBricks.allB(parseByte(commandLine.getOptionValue(ALL_B)));
                } else if (commandLine.hasOption(ALL_C)) {
                    sBricks.allC(parseByte(commandLine.getOptionValue(ALL_C)));
                } else if (commandLine.hasOption(ALL_D)) {
                    sBricks.allD(parseByte(commandLine.getOptionValue(ALL_D)));
                } else {
                    final HelpFormatter formatter = new HelpFormatter();
                    formatter.printHelp("sbrick", options);
                    System.exit(1);
                }

                Thread.sleep(1000L);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            if (bgapi != null) {
                bgapi.listeners.clear();
                bgapi.send_system_reset(0);
                bgapi.disconnect();
            }

            ble112Port.closePort();
        }
    }

    static Hub parseBrick(final String hubSpec) {
        if (hubSpec == null || hubSpec.isEmpty()) {
            throw new IllegalArgumentException("missing required option -hub");
        }

        final String[] parts = hubSpec.split(",");
        final Brick[] bricks = new Brick[4];
        for (int i = 0; i < 4; i++) {
            final char port = (char) (FIRST_PORT + i);
            final int partsI = i + 1;
            if (partsI >= parts.length || parts[partsI].length() == 0) {
                bricks[i] = new Brick(port, NOT_CONNECTED);
            } else {
                bricks[i] = new Brick(port,
                        Type.valueOf(parts[partsI].toUpperCase()));
            }
        }

        return new Hub(parts[0], "SBrick", bricks);
    }

    private static Options setOptions() {
        final Options options = new Options();

        options.addOption(VERBOSE, "verbose output");
        options.addOption(BLE112DEVICE, true,
                "the file path to your BLE112 dongle. On Mac OS X, this is typically /dev/cu.usbmodem1");
        options.addOption(HUB, true,
                "specify the hub's MAC address and port assignment. E.g. -hub 00:77:80:2e:43:e4,MOTOR,,LIGHT");

        options.addOption(RESET, "reset all bricks");
        options.addOption(LIST, "list SBricks and SBrick Pluses");
        options.addOption(MOTOR, true,
                "set all motors to speed (-127 to 127, 0 is off, negative for reverse)");
        options.addOption(MOTOR_A, true,
                "set all motor A's to speed (-127 to 127, 0 is off, negative for reverse)");
        options.addOption(MOTOR_B, true,
                "set all motor B's to speed (-127 to 127, 0 is off, negative for reverse)");
        options.addOption(MOTOR_C, true,
                "set all motor C's to speed (-127 to 127, 0 is off, negative for reverse)");
        options.addOption(MOTOR_D, true,
                "set all motor D's to speed (-127 to 127, 0 is off, negative for reverse)");
        options.addOption(LIGHT, true,
                "set all lights to intensity (0 to 127, 0 is off)");
        options.addOption(LIGHT_A, true,
                "set all brick A lights to intensity (0 to 127, 0 is off)");
        options.addOption(LIGHT_B, true,
                "set all brick B lights to intensity (0 to 127, 0 is off)");
        options.addOption(LIGHT_C, true,
                "set all brick C lights to intensity (0 to 127, 0 is off)");
        options.addOption(LIGHT_D, true,
                "set all brick D lights to intensity (0 to 127, 0 is off)");
        options.addOption(ALL, true, "set all bricks to value (-127 to 127)");
        options.addOption(ALL_A, true,
                "set all brick A's to speed (-127 to 127)");
        options.addOption(ALL_B, true,
                "set all brick B's to speed (-127 to 127)");
        options.addOption(ALL_C, true,
                "set all brick C's to speed (-127 to 127)");
        options.addOption(ALL_D, true,
                "set all brick D's to speed (-127 to 127)");

        return options;
    }

    private static CommandLine parseCommandLine(final Options options,
            final String[] arguments) throws ParseException {
        final CommandLineParser commandLineParser = new DefaultParser();
        return commandLineParser.parse(options, arguments);
    }

    private static void list(final boolean verbose, final BGAPI bgapi)
            throws IOException {
        if (verbose) {
            out.printf("Found %d serial ports:\n",
                    SerialPort.getCommPorts().length);
            for (final SerialPort serialPort : SerialPort.getCommPorts()) {
                out.printf("  /dev/%s (%s)\n", serialPort.getSystemPortName(),
                        serialPort.getDescriptivePortName());
            }
            out.printf("\n");
        }

        out.printf(
                "Scanning for Vengit SBricks and SBrick Pluses (this may take a few seconds)...\n\n");

        final SBrickScanner sBrickScanner = new SBrickScanner(bgapi);
        final Collection<Hub> hubs = sBrickScanner.scan();
        if (hubs.size() == 0) {
            out.printf("No SBricks or SBrick Pluses found.\n");
        } else {
            for (final Hub hub : hubs) {
                out.printf("%s %s\n", hub.getPath(), hub.getProductName());
                for (final Brick brick : hub.getBricks()) {
                    listBrick(brick);
                }
            }
        }
    }

    private static void listBrick(final Brick brick) {
        out.println("  brick " + brick.getPort() + ": "
                + brick.getType().toString().toLowerCase().replace("_", " "));
    }
}
