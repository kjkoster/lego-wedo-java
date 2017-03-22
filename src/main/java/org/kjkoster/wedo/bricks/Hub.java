package org.kjkoster.wedo.bricks;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

import java.util.List;

/**
 * A weak pointer to a LEGO hub of some system. Hubs may be plugged in or out at
 * any time. Due to the volatile nature of USB and BLE, we do not actually hand
 * out hubs as objects. Instead this library takes the approach to use abstract
 * hub handles that may or may not point to a valid device.
 * <p>
 * Note that this handle does not point to a USB hub, but rather to one of the
 * various types of LEGO hubs (WeDo, Sbrick etc).
 *
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
public class Hub {
    private final String path;
    private final String productName;
    private final List<Brick> bricks;

    /**
     * Create a new pointer to a hub.
     * 
     * @param path
     *            The brick's address on the transport that handles it.
     * @param productName
     *            The (reasonably) human readable representation of that hub.
     * @param bricks
     *            The bricks that are connected to this hub.
     */
    public Hub(final String path, final String productName,
            final Brick[] bricks) {
        checkNotNull(path, "null path");
        checkArgument(path.trim().equals(path), "path has whitespace");
        checkArgument(path.length() > 0, "path was empty");
        this.path = path;

        checkNotNull(productName, "null product name");
        checkArgument(productName.trim().equals(productName),
                "product name has whitespace");
        checkArgument(productName.length() > 0, "product name was empty");
        this.productName = productName;

        checkArgument(
                bricks.length == 2/* WeDo */ || bricks.length == 4 /* SBrick */,
                "unexpected number of bricks (expected 2 or 4, found %d)",
                bricks.length);
        this.bricks = unmodifiableList(asList(bricks));
    }

    /**
     * Get the unique path for this hub for use in the USB or Bluetooth
     * transport. This path may or may not be valid for reuse when a hub was
     * unplugged or reconnected.
     * 
     * @return The unique path for this hub.
     */
    public String getPath() {
        return path;
    }

    /**
     * Find the human readable product name of the hub.
     * 
     * @return The human readable product name.
     */
    public String getProductName() {
        return productName;
    }

    /**
     * Find the bricks attached to this hub. This list is immutable.
     * 
     * @return The immutable list of bricks attached to this hub.
     */
    public List<Brick> getBricks() {
        return bricks;
    }

    /**
     * Find the brick attached to a specific port on this hub.
     * 
     * @param port
     *            The port that we'd like to know the brick on.
     * @return The port's brick.
     */
    public Brick getBrick(final char port) {
        checkArgument(port >= 'A' && port < bricks.size(),
                "no port %c on hub %s", port, path);
        return bricks.get(port - 'A');
    }
}
