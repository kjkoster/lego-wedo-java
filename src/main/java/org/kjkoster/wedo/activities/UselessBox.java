package org.kjkoster.wedo.activities;

import static java.lang.System.exit;
import static java.lang.Thread.sleep;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.kjkoster.wedo.bricks.Tilt.Direction.RIGHT;

import java.util.Collection;

import org.kjkoster.wedo.bricks.Tilt;
import org.kjkoster.wedo.bricks.WeDoBricks;
import org.kjkoster.wedo.usb.Usb;

/**
 * A basic useless box with a motor and a tilt sensor.
 * 
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
public class UselessBox {
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
                waitForSwitchThrown();
                runOneCyle();
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

    private static void waitForSwitchThrown() throws InterruptedException {
        for (;;) {
            sleep(MILLISECONDS.toMillis(100L));

            final Collection<Tilt> tilts = weDoBricks.readTilts();
            if (tilts.size() > 0
                    && tilts.iterator().next().getDirection() == RIGHT) {
                return;
            }
        }
    }

    private static void runOneCyle() throws InterruptedException {
        weDoBricks.motor((byte) 0x2f);
        sleep(SECONDS.toMillis(1L));
        weDoBricks.motor((byte) 0x00);
    }
}
