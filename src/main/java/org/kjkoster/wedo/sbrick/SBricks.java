package org.kjkoster.wedo.sbrick;

import java.util.Map;

import org.kjkoster.wedo.bricks.Brick;
import org.kjkoster.wedo.usb.Handle;

/**
 * A class to represent the collection of SBricks and SBrick Pluses.
 *
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
public class SBricks {
    private final SBrickScanner sBrickScanner;

    /**
     * Construct a new SBrick collection.
     * 
     * @param sBrickScanner
     *            The SBrick scanner to use.
     */
    public SBricks(final SBrickScanner sBrickScanner) {
        super();

        this.sBrickScanner = sBrickScanner;
    }

    /**
     * Read a map of all the bricks. We scan for SBricks. Unfortunately SBricks
     * do not support detection of the bricks plugged into them, so we get an
     * empty list back. SBricks can be switched on and off at any time, so it is
     * a surprise how many bricks we get every time.
     * 
     * @return All the bricks, neatly laid out in a map.
     */
    public Map<Handle, Brick[]> readAll() {
        return sBrickScanner.readAll();
    }
}
