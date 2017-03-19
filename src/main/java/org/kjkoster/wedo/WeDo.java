package org.kjkoster.wedo;

import static java.lang.Byte.parseByte;
import static java.lang.Integer.parseInt;
import static java.lang.System.out;
import static org.apache.commons.cli.Option.builder;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.kjkoster.wedo.bricks.Brick;
import org.kjkoster.wedo.bricks.Distance;
import org.kjkoster.wedo.bricks.Tilt;
import org.kjkoster.wedo.bricks.WeDoBricks;
import org.kjkoster.wedo.sbrick.SBrickScanner;
import org.kjkoster.wedo.transport.usb.Handle;
import org.kjkoster.wedo.transport.usb.Usb;

/**
 * The command line tool's main entry point.
 * 
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
public class WeDo {
    private static final String VERBOSE = "v";
    private static final String BLE112DEVICE = "ble112";

    private static final String RESET = "reset";
    private static final String LIST = "list";

    private static final String TILT = "tilt";
    private static final String DISTANCE = "distance";
    private static final String SENSOR = "sensor";

    private static final String ALL = "all";
    private static final String ALL_A = "allA";
    private static final String ALL_B = "allB";

    private static final String LIGHT = "light";
    private static final String LIGHT_A = "lightA";
    private static final String LIGHT_B = "lightB";

    private static final String MOTOR = "motor";
    private static final String MOTOR_A = "motorA";
    private static final String MOTOR_B = "motorB";

    private static WeDoBricks weDoBricks = null;

    /**
     * The main application entry point.
     * 
     * @param args
     *            The command line arguments, as documented in
     *            <code>usage()</code>.
     * @throws ParseException
     *             When the command line could not be parsed.
     */
    public static void main(final String[] args) throws ParseException {
        final Options options = setOptions();

        final CommandLine commandLine = parseCommandLine(options, args);

        final boolean verbose = commandLine.hasOption(VERBOSE);
        final File ble112Device = commandLine.hasOption(BLE112DEVICE)
                ? new File(commandLine.getOptionValue(BLE112DEVICE)) : null;

        try (final Usb usb = new Usb(verbose)) {
            weDoBricks = new WeDoBricks(usb, verbose);

            if (commandLine.hasOption(RESET)) {
                weDoBricks.reset();
            } else if (commandLine.hasOption(LIST)) {
                list(ble112Device, verbose);
            } else if (commandLine.hasOption(SENSOR)) {
                final String value = commandLine.getOptionValue(SENSOR);
                sensor(value == null ? -1 : parseInt(value), true, true);
            } else if (commandLine.hasOption(DISTANCE)) {
                final String value = commandLine.getOptionValue(DISTANCE);
                sensor(value == null ? -1 : parseInt(value), true, false);
            } else if (commandLine.hasOption(TILT)) {
                final String value = commandLine.getOptionValue(TILT);
                sensor(value == null ? -1 : parseInt(value), false, true);
            } else if (commandLine.hasOption(MOTOR)) {
                weDoBricks.motor(parseByte(commandLine.getOptionValue(MOTOR)));
            } else if (commandLine.hasOption(MOTOR_A)) {
                weDoBricks
                        .motorA(parseByte(commandLine.getOptionValue(MOTOR_A)));
            } else if (commandLine.hasOption(MOTOR_B)) {
                weDoBricks
                        .motorB(parseByte(commandLine.getOptionValue(MOTOR_B)));
            } else if (commandLine.hasOption(LIGHT)) {
                weDoBricks.light(parseByte(commandLine.getOptionValue(LIGHT)));
            } else if (commandLine.hasOption(LIGHT_A)) {
                weDoBricks
                        .lightA(parseByte(commandLine.getOptionValue(LIGHT_A)));
            } else if (commandLine.hasOption(LIGHT_B)) {
                weDoBricks
                        .lightB(parseByte(commandLine.getOptionValue(LIGHT_B)));
            } else if (commandLine.hasOption(ALL)) {
                weDoBricks.all(parseByte(commandLine.getOptionValue(ALL)));
            } else if (commandLine.hasOption(ALL_A)) {
                weDoBricks.allA(parseByte(commandLine.getOptionValue(ALL_A)));
            } else if (commandLine.hasOption(ALL_B)) {
                weDoBricks.allB(parseByte(commandLine.getOptionValue(ALL_B)));
            } else {
                final HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("wedo", options);
                System.exit(1);
            }
        } catch (

        Throwable e) {
            e.printStackTrace();
        } finally {
            // It seems that under Mac OS X a thread is still stuck in the
            // hidapi USB library, so we force the JVM to exit.
            System.exit(0);
        }
    }

    private static Options setOptions() {
        final Options options = new Options();

        options.addOption(VERBOSE, "verbose output");
        options.addOption(BLE112DEVICE, true,
                "the file path to your BLE112 dongle. On Mac OS X, this is typically /dev/cu.usbmodem1");

        options.addOption(RESET, "reset all bricks");
        options.addOption(LIST, "list WeDo hubs, SBricks and SBrick Pluses");
        options.addOption(builder(SENSOR).optionalArg(true)
                .desc("read all sensors [<n>] times (default repeat forever)")
                .build());
        options.addOption(builder(DISTANCE).optionalArg(true)
                .desc("read all distance sensors [<n>] times (default repeat forever)")
                .build());
        options.addOption(builder(TILT).optionalArg(true)
                .desc("read all tilt sensors [<n>] times (default repeat forever)")
                .build());
        options.addOption(MOTOR, true,
                "set all motors to speed (-127 to 127, 0 is off, negative for reverse)");
        options.addOption(MOTOR_A, true,
                "set all motor A's to speed (-127 to 127, 0 is off, negative for reverse)");
        options.addOption(MOTOR_B, true,
                "set all motor B's to speed (-127 to 127, 0 is off, negative for reverse)");
        options.addOption(LIGHT, true,
                "set all lights to intensity (0 to 127, 0 is off)");
        options.addOption(LIGHT_A, true,
                "set all brick A lights to intensity (0 to 127, 0 is off)");
        options.addOption(LIGHT_B, true,
                "set all brick B lights to intensity (0 to 127, 0 is off)");
        options.addOption(ALL, true, "set all bricks to value (-127 to 127)");
        options.addOption(ALL_A, true,
                "set all brick A's to speed (-127 to 127)");
        options.addOption(ALL_B, true,
                "set all brick B's to speed (-127 to 127)");

        return options;
    }

    private static CommandLine parseCommandLine(final Options options,
            final String[] arguments) throws ParseException {
        final CommandLineParser commandLineParser = new DefaultParser();
        return commandLineParser.parse(options, arguments);
    }

    /**
     * Print information on the WeDo hubs attached to this computer, and the
     * devices connected to them. The list is unordered.
     * <p>
     * We have no reliable way of addressing specific bricks connected to a
     * specific hub. If you study the output of the verbose you will find that
     * WeDo hubs do not seem to have identifying information. Further, the order
     * in which the hubs are reported by the <code>listDevices()</code> method
     * is random (though tantalisingly stable at times).
     */
    private static void list(final File ble112Device, final boolean verbose)
            throws IOException {
        out.printf("Scanning for LEGO WeDo hubs...\n\n");

        Map<Handle, Brick[]> hubs = weDoBricks.readAll();
        if (hubs.size() == 0) {
            out.println("No LEGO WeDo hubs found.");
        } else {
            for (final Map.Entry<Handle, Brick[]> hub : hubs.entrySet()) {
                // we don't show the USB address, it changes a lot.
                out.println(hub.getKey().getProductName());
                for (final Brick brick : hub.getValue()) {
                    listBrick(brick);
                }
            }
        }

        if (ble112Device == null) {
            out.printf(
                    "\nSpecify -%s on the command line to look for Vengit SBricks too.\n",
                    BLE112DEVICE);
        } else {
            out.printf(
                    "\nScanning for Vengit SBricks and SBrick Pluses (this may take a few seconds)...\n\n");

            try (final SBrickScanner sBrickScanner = new SBrickScanner(
                    ble112Device, verbose)) {
                hubs = sBrickScanner.readAll();
                if (hubs.size() == 0) {
                    out.println("No Vengit SBricks or SBrick Pluses found.");
                } else {
                    for (final Map.Entry<Handle, Brick[]> hub : hubs
                            .entrySet()) {
                        out.println(hub.getKey().getProductName() + ", at "
                                + hub.getKey().getPath());
                        for (final Brick brick : hub.getValue()) {
                            listBrick(brick);
                        }
                    }
                }
            }
        }
    }

    private static void listBrick(final Brick brick) {
        String sensorData = "";
        switch (brick.getType()) {
        case DISTANCE:
            sensorData = ": " + brick.getDistance().getCm() + " cm";
            break;
        case TILT:
            sensorData = ": " + brick.getTilt().getDirection().toString()
                    .toLowerCase().replace("_", " ");
            break;
        default:
        }
        out.println("  brick " + brick.getPort() + ": "
                + brick.getType().toString().toLowerCase().replace("_", " ")
                + sensorData);
    }

    private static void sensor(int repeat, final boolean showDistance,
            final boolean showTilt) throws InterruptedException {
        while (repeat == -1 || repeat > 0) {
            final Map<Handle, Brick[]> hubs = weDoBricks.readAll();
            for (final Map.Entry<Handle, Brick[]> hub : hubs.entrySet()) {
                for (final Brick brick : hub.getValue()) {
                    switch (brick.getType()) {
                    case DISTANCE:
                        if (showDistance) {
                            final Distance distance = brick.getDistance();
                            out.printf("distance %d cm (value %d)\n",
                                    distance.getCm(), distance.getValue());
                        }
                        break;
                    case TILT:
                        if (showTilt) {
                            final Tilt tilt = brick.getTilt();
                            out.printf("tilt %s (value %d)\n",
                                    tilt.getDirection().toString().toLowerCase()
                                            .replace("_", " "),
                                    tilt.getValue());
                        }
                        break;
                    case MOTOR:
                    case LIGHT:
                    case NOT_CONNECTED:
                        // known not to be sensors
                        break;
                    default:
                        out.printf("unknown sensor type %s\n", brick.getType()
                                .toString().toLowerCase().replace("_", " "));
                        break;
                    }
                }
            }

            if (repeat == -1 || --repeat > 0) {
                Thread.sleep(1000L);
            }
        }
    }
}
