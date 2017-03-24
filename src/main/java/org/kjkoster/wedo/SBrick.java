package org.kjkoster.wedo;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.Byte.parseByte;
import static java.lang.Integer.parseInt;
import static java.lang.System.out;
import static org.apache.commons.cli.Option.builder;
import static org.kjkoster.wedo.bricks.Brick.Type.UNKNOWN;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
import org.kjkoster.wedo.bricks.Distance;
import org.kjkoster.wedo.bricks.Hub;
import org.kjkoster.wedo.bricks.Tilt;
import org.kjkoster.wedo.systems.sbrick.SBrickScanner;
import org.kjkoster.wedo.systems.sbrick.SBricks;
import org.kjkoster.wedo.transport.ble112.BLE112Connections;
import org.kjkoster.wedo.transport.ble112.ProtocolLogger;
import org.thingml.bglib.BGAPI;
import org.thingml.bglib.BGAPITransport;

/**
 * The Vengit SBrick and SBrick Plus command line tool's main entry point.
 * 
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
public class SBrick {
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
        final BGAPITransport bgapiTransport = new BGAPITransport(
                new FileInputStream(ble112Device),
                new FileOutputStream(ble112Device));
        final BGAPI bgapi = new BGAPI(bgapiTransport);
        if (verbose) {
            bgapi.addListener(new ProtocolLogger());
        }

        try {
            if (commandLine.hasOption(LIST)) {
                list(bgapi);
            } else {
                final Collection<Hub> hubs = new ArrayList<>();
                final Brick[] bricks = new Brick[4];
                bricks[0] = new Brick('A', Type.MOTOR);
                bricks[1] = new Brick('B', UNKNOWN);
                bricks[2] = new Brick('C', UNKNOWN);
                bricks[3] = new Brick('D', UNKNOWN);
                // XXX get this from the command line...
                hubs.add(new Hub("0:7:80:d0:52:bf", "SBrick plus", bricks));
                // hubs.add(
                // new Hub("00:77:80:2e:43:e4", "SBrick regular", bricks));

                final BLE112Connections ble112Connections = new BLE112Connections(
                        bgapi);
                final SBricks sBricks = new SBricks(bgapi, ble112Connections,
                        hubs);

                Thread.sleep(1000L);

                if (commandLine.hasOption(RESET)) {
                    sBricks.reset();
                } else if (commandLine.hasOption(SENSOR)) {
                    final String value = commandLine.getOptionValue(SENSOR);
                    sensor(sBricks, value == null ? -1 : parseInt(value), true,
                            true);
                } else if (commandLine.hasOption(DISTANCE)) {
                    final String value = commandLine.getOptionValue(DISTANCE);
                    sensor(sBricks, value == null ? -1 : parseInt(value), true,
                            false);
                } else if (commandLine.hasOption(TILT)) {
                    final String value = commandLine.getOptionValue(TILT);
                    sensor(sBricks, value == null ? -1 : parseInt(value), false,
                            true);
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
                } else if (commandLine.hasOption(LIGHT)) {
                    sBricks.light(parseByte(commandLine.getOptionValue(LIGHT)));
                } else if (commandLine.hasOption(LIGHT_A)) {
                    sBricks.lightA(
                            parseByte(commandLine.getOptionValue(LIGHT_A)));
                } else if (commandLine.hasOption(LIGHT_B)) {
                    sBricks.lightB(
                            parseByte(commandLine.getOptionValue(LIGHT_B)));
                } else if (commandLine.hasOption(ALL)) {
                    sBricks.all(parseByte(commandLine.getOptionValue(ALL)));
                } else if (commandLine.hasOption(ALL_A)) {
                    sBricks.allA(parseByte(commandLine.getOptionValue(ALL_A)));
                } else if (commandLine.hasOption(ALL_B)) {
                    sBricks.allB(parseByte(commandLine.getOptionValue(ALL_B)));
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
            bgapi.listeners.clear();
            bgapi.send_system_reset(0);
            bgapi.disconnect();
        }
    }

    private static Options setOptions() {
        final Options options = new Options();

        options.addOption(VERBOSE, "verbose output");
        options.addOption(BLE112DEVICE, true,
                "the file path to your BLE112 dongle. On Mac OS X, this is typically /dev/cu.usbmodem1");

        options.addOption(RESET, "reset all bricks");
        options.addOption(LIST, "list SBricks and SBrick Pluses");
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

    private static void list(final BGAPI bgapi) throws IOException {
        out.printf(
                "Scanning for Vengit SBricks and SBrick Pluses (this may take a few seconds)...\n\n");

        final SBrickScanner sBrickScanner = new SBrickScanner(bgapi);
        final Collection<Hub> hubs = sBrickScanner.readAll();
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

    private static void sensor(final SBricks sBricks, int repeat,
            final boolean showDistance, final boolean showTilt)
            throws InterruptedException {
        while (repeat == -1 || repeat > 0) {
            for (final Hub hub : sBricks.readAll()) {
                for (final Brick brick : hub.getBricks()) {
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
                    case UNKNOWN:
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
