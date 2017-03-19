package org.kjkoster.wedo.activities;

import static java.lang.System.exit;
import static java.lang.System.nanoTime;
import static java.lang.Thread.sleep;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.kjkoster.wedo.bricks.Tilt.Direction.LEFT;
import static org.kjkoster.wedo.bricks.Tilt.Direction.RIGHT;

import java.util.Collection;

import org.kjkoster.wedo.bricks.Tilt;
import org.kjkoster.wedo.bricks.WeDoBricks;
import org.kjkoster.wedo.transport.usb.Usb;

/**
 * Using a tilt sensor and a motor we try to balance the tilt sensor.
 * 
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
public class Balancer {
    private static WeDoBricks weDoBricks = null;

    private static final int WOBBLES = 4;
    private static final long SLEEPTIME = MILLISECONDS.toMillis(100L);

    /**
     * The main entry point.
     * 
     * @param args
     *            Ignored.
     */
    public static void main(final String[] args) {
        try (final Usb usb = new Usb(false)) {
            weDoBricks = new WeDoBricks(usb, true);
            weDoBricks.reset();

            tiltLeft();

            final long startTime = nanoTime();
            for (int i = 0; i < WOBBLES; i++) {
                if ((i % 2) == 0) {
                    tiltRight();
                } else {
                    tiltLeft();
                }
            }
            final long endTime = nanoTime();

            final long halfWayTime = ((endTime - startTime) / WOBBLES) / 2L;
            balance(halfWayTime);
        } catch (Exception e) {
            e.printStackTrace();
            exit(1);
        } finally {
            try {
                sleep(1000L);
                weDoBricks.reset();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

            // It seems that under Mac OS X a thread is still stuck in the
            // hidapi USB library, so we force the JVM to exit.
            exit(0);
        }
    }

    private static void tiltLeft() throws InterruptedException {
        weDoBricks.motor((byte) -50);

        for (;;) {
            sleep(SLEEPTIME);

            final Collection<Tilt> tilts = weDoBricks.readTilts();
            if (tilts.size() > 0
                    && tilts.iterator().next().getDirection() == LEFT) {
                break;
            }
        }
        weDoBricks.motor((byte) 0);
    }

    private static void tiltRight() throws InterruptedException {
        weDoBricks.motor((byte) 50);

        for (;;) {
            sleep(SLEEPTIME);

            final Collection<Tilt> tilts = weDoBricks.readTilts();
            if (tilts.size() > 0
                    && tilts.iterator().next().getDirection() == RIGHT) {
                break;
            }
        }
        weDoBricks.motor((byte) 0);
    }

    private static void balance(final long halfWayTime)
            throws InterruptedException {
        weDoBricks.motor((byte) 50);
        sleep(NANOSECONDS.toMillis(halfWayTime));
        weDoBricks.motor((byte) 0);
    }
}
