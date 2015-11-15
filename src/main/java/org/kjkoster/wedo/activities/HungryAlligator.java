package org.kjkoster.wedo.activities;

import static java.lang.System.exit;
import static java.lang.Thread.sleep;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.Collection;

import org.kjkoster.wedo.bricks.Distance;
import org.kjkoster.wedo.bricks.WeDoBricks;
import org.kjkoster.wedo.usb.Usb;

/**
 * LEGO WeDo's hungry alligator.
 * 
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
public class HungryAlligator {
    private static WeDoBricks weDoBricks = null;

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

            for (;;) {
                openJawSlowly();
                waitForBait();
                slamShut();
            }
        } catch (Throwable e) {
            e.printStackTrace();
            exit(1);
        } finally {
            weDoBricks.reset();

            // It seems that under Mac OS X a thread is still stuck in the
            // hidapi USB library, so we force the JVM to exit.
            exit(0);
        }
    }

    private static void openJawSlowly() throws InterruptedException {
        weDoBricks.motor((byte) 30);
        sleep(SECONDS.toMillis(3L));
        weDoBricks.motor((byte) 0x00);
    }

    private static void waitForBait() throws InterruptedException {
        for (;;) {
            sleep(MILLISECONDS.toMillis(100L));

            final Collection<Distance> distances = weDoBricks.readDistances();
            if (distances.size() > 0 && distances.iterator().next().getCm() < 1) {
                return;
            }
        }
    }

    private static void slamShut() throws InterruptedException {
        weDoBricks.motor((byte) -127);
        sleep(MILLISECONDS.toMillis(400L));
        weDoBricks.motor((byte) 0x00);
        sleep(MILLISECONDS.toMillis(400L));
    }
}
