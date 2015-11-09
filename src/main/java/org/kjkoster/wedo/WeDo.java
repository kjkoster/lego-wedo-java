package org.kjkoster.wedo;

import static java.lang.Byte.parseByte;
import static java.lang.Integer.parseInt;
import static java.lang.System.out;
import static java.lang.Thread.setDefaultUncaughtExceptionHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.kjkoster.wedo.bricks.Brick;
import org.kjkoster.wedo.bricks.Distance;
import org.kjkoster.wedo.bricks.Tilt;
import org.kjkoster.wedo.bricks.WeDoBricks;
import org.kjkoster.wedo.usb.Handle;
import org.kjkoster.wedo.usb.Usb;

/**
 * The command line tool's main entry point.
 * 
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
public class WeDo {
    private static WeDoBricks weDoBricks = null;

    /**
     * The main application entry point.
     * 
     * @param args
     *            The command line arguments, as documented in
     *            <code>usage()</code>.
     */
    public static void main(String[] args) {
        setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                e.printStackTrace();
                System.exit(1);
            }
        });

        try {
            boolean verbose = false;
            final List<String> options = new ArrayList<>();
            for (final String arg : args) {
                options.add(arg);
            }
            if (options.size() > 0 && "-v".equals(options.get(0))) {
                verbose = true;
                options.remove(0);
            }
            if (options.size() == 0) {
                usage();
                System.exit(1);
            }

            weDoBricks = new WeDoBricks(new Usb(), verbose);

            final String command = options.remove(0);
            switch (command) {
            case "reset":
                weDoBricks.reset();
                break;
            case "list":
                list();
                break;

            case "sensor":
                sensor(options.size() == 0 ? -1 : parseInt(options.remove(0)),
                        true, true);
                break;
            case "distance":
                sensor(options.size() == 0 ? -1 : parseInt(options.remove(0)),
                        true, false);
                break;
            case "tilt":
                sensor(options.size() == 0 ? -1 : parseInt(options.remove(0)),
                        false, true);
                break;

            case "motor":
                weDoBricks.motor(parseByte(options.remove(0)));
                break;
            case "motorA":
                weDoBricks.motorA(parseByte(options.remove(0)));
                break;
            case "motorB":
                weDoBricks.motorB(parseByte(options.remove(0)));
                break;

            case "light":
                weDoBricks.light(parseByte(options.remove(0)));
                break;
            case "lightA":
                weDoBricks.lightA(parseByte(options.remove(0)));
                break;
            case "lightB":
                weDoBricks.lightB(parseByte(options.remove(0)));
                break;

            case "all":
                weDoBricks.all(parseByte(options.remove(0)));
                break;
            case "allA":
                weDoBricks.allA(parseByte(options.remove(0)));
                break;
            case "allB":
                weDoBricks.allB(parseByte(options.remove(0)));
                break;

            default:
                out.printf("unknown command '%s'\n", command);
                usage();
                System.exit(1);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            // It seems that under Mac OS X a thread is still stuck in the
            // hidapi USB library, so we force the JVM to exit.
            System.exit(0);
        }
    }

    private static void usage() {
        out.println("Usage:");
        out.println("    wedo [-v] reset               reset all bricks");
        out.println("    wedo [-v] list                list WeDo hubs and blocks");
        out.println("    wedo [-v] sensor [<n>]        read all sensors n times (default repeat forever)");
        out.println("    wedo [-v] distance [<n>]      read all distance sensors n times (default repeat forever)");
        out.println("    wedo [-v] tilt [<n>]          read all tilt sensors n times (default repeat forever)");
        out.println("    wedo [-v] motor <speed>       set all motors to speed (-127 to 127, 0 is off, negative for reverse)");
        out.println("    wedo [-v] motorA <speed>      set all motor A's to speed (-127 to 127, 0 is off, negative for reverse)");
        out.println("    wedo [-v] motorB <speed>      set all motor B's to speed (-127 to 127, 0 is off, negative for reverse)");
        out.println("    wedo [-v] light <intensity>   set all lights to intensity (0 to 127, 0 is off)");
        out.println("    wedo [-v] lightA <intensity>  set all block A lights to intensity (0 to 127, 0 is off)");
        out.println("    wedo [-v] lightB <intensity>  set all block B lights to intensity (0 to 127, 0 is off)");
        out.println("    wedo [-v] all <value>         set all blocks to value (-127 to 127)");
        out.println("    wedo [-v] allA <value>        set all block A's to speed (-127 to 127)");
        out.println("    wedo [-v] allB <value>        set all block B's to speed (-127 to 127)");
    }

    /**
     * Print information on the WeDo hubs attached to this computer, and the
     * devices connected to them. The list is unordered.
     * <p>
     * We have no reliable way of addressing specific blocks connected to a
     * specific hub. If you study the output of the verbose you will find that
     * WeDo hubs do not seem to have identifying information. Further, the order
     * in which the hubs are reported by the <code>listDevices()</code> method
     * is random (though tantalisingly stable at times).
     */
    private static void list() throws IOException {
        final Map<Handle, Brick[]> hubs = weDoBricks.readAll();
        if (hubs.size() == 0) {
            out.println("No LEGO WeDo hubs found.");
        } else {
            for (final Map.Entry<Handle, Brick[]> hub : hubs.entrySet()) {
                out.println(hub.getKey().getProductName());
                out.println("  brick A: "
                        + hub.getValue()[0].getType().toString().toLowerCase()
                                .replace("_", " "));
                out.println("  brick b: "
                        + hub.getValue()[1].getType().toString().toLowerCase()
                                .replace("_", " "));
            }
        }
    }

    private static void sensor(int repeat, final boolean showDistance,
            final boolean showTilt) throws IOException, InterruptedException {
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
                            out.printf("tilt %s angle %d (value %d)\n", tilt
                                    .getDirection().toLowerCase(), tilt
                                    .getAngle(), tilt.getValue());
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
