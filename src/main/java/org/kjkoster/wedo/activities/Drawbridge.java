package org.kjkoster.wedo.activities;

import static com.google.common.base.Preconditions.checkState;
import static java.lang.System.exit;
import static java.lang.System.out;
import static java.lang.Thread.sleep;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.kjkoster.wedo.bricks.Tilt.Direction.BACKWARD;
import static org.kjkoster.wedo.bricks.Tilt.Direction.FORWARD;
import static org.kjkoster.wedo.bricks.Tilt.Direction.NO_TILT;

import java.util.Collection;

import org.kjkoster.wedo.bricks.Tilt;
import org.kjkoster.wedo.bricks.Tilt.Direction;
import org.kjkoster.wedo.bricks.WeDoBricks;
import org.kjkoster.wedo.usb.Usb;

/**
 * The drawbridge is interesting because of the limitations of the tilt sensor
 * and the WeDo hub. Using only a rather brief tilt sensor we have to decide
 * what angle the drawbridge is at.
 * <p>
 * For the solution below, we make the assumption that the tilt sensor only
 * changes a little bit between two readings. We run the motor slowly and use a
 * worm wheel to slow the bridge motion. This gives us time to read the tilt
 * sensor and reason about the angle it must be at.
 * 
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
public class Drawbridge {
    private static final byte DOWN = 0x40;
    private static final byte UP = (byte) (-DOWN & 0xff);
    private static final byte STOP = 0x00;

    private static WeDoBricks weDoBricks;

    /**
     * The main drawbridge program.
     * 
     * @param args
     *            Ignored.
     */
    public static void main(final String[] args) {
        try (final Usb usb = new Usb(false)) {
            weDoBricks = new WeDoBricks(usb, false);
            weDoBricks.reset();

            findFlatPoint();

            for (;;) {
                sleep(SECONDS.toMillis(5L));
                out.println("Opening the bridge...");
                weDoBricks.motor(UP);
                sleep(SECONDS.toMillis(2L));
                runUntilFlat();

                sleep(SECONDS.toMillis(2L));
                out.println("Closing the bridge...");
                weDoBricks.motor(DOWN);
                sleep(SECONDS.toMillis(2L));
                runUntilFlat();
            }
        } catch (Exception e) {
            e.printStackTrace();
            exit(1);
        } finally {
            weDoBricks.reset();

            // It seems that under Mac OS X a thread is still stuck in the
            // hidapi USB library, so we force the JVM to exit.
            exit(0);
        }
    }

    /**
     * Find the bridge's flat point. If the tilt sensor says the bridge is at a
     * forward or backward angle that is easy, we just run the motor until the
     * tilt sensor stops reading an angle.
     * <p>
     * If the tilt sensor says it cannot read the angle, we are in trouble. The
     * bridge may either be flat or upright at that point. We run the motor for
     * a brief time and see what happens to the tilt sensor reading.
     */
    private static void findFlatPoint() throws InterruptedException {
        switch (readTilt()) {
        case BACKWARD:
            out.println("Bridge is still open, closing it first...");

            weDoBricks.motor(DOWN);
            runUntilFlat();
            break;
        case FORWARD:
            out.println("Bridge is hanging in the water, pulling it up...");

            weDoBricks.motor(UP);
            runUntilFlat();
            break;
        case NO_TILT:
            out.println("Bridge is either flat or upright, trying to see which it is...");
            weDoBricks.motor(UP);
            sleep(SECONDS.toMillis(1L));
            if (readTilt() == NO_TILT) {
                out.println("Bridge was tilted backwards, reversing...");
                weDoBricks.motor(DOWN);
                sleep(SECONDS.toMillis(4L)); // carry it back over upright
            } else {
                out.println("Bridge was flat, reversing...");
                weDoBricks.motor(DOWN);
            }
            runUntilFlat();
            break;
        default:
            throw new IllegalArgumentException("unsupported tilt direction");
        }
    }

    private static Direction readTilt() {
        final Collection<Tilt> tilts = weDoBricks.readTilts();
        checkState(tilts.size() == 1);
        return tilts.iterator().next().getDirection();
    }

    private static void runUntilFlat() throws InterruptedException {
        Direction direction = readTilt();
        while (direction == BACKWARD || direction == FORWARD) {
            sleep(MILLISECONDS.toMillis(100L));
            direction = readTilt();
        }
        weDoBricks.motor(STOP);
    }
}
