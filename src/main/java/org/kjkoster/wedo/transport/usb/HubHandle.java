package org.kjkoster.wedo.transport.usb;

import static com.google.common.base.Preconditions.checkNotNull;

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
public class HubHandle {
    private final String path;
    private final String productName;

    /**
     * Create a new pointer to a brick.
     * 
     * @param path
     *            The brick's address on the transport that handles it.
     * @param productName
     *            The (reasonably) human readable representation of that hub.
     */
    public HubHandle(final String path, final String productName) {
        this.path = checkNotNull(path);
        this.productName = checkNotNull(productName);
    }

    /**
     * Get the unique path for this hub. This path may or may not be valid for
     * reuse.
     * 
     * @return The unique path for this hub.
     */
    public String getPath() {
        return path;
    }

    /**
     * Find the human readable product name of the hub that this packet came
     * from.
     * 
     * @return The human readable product name.
     */
    public String getProductName() {
        return productName;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return path;
    }
}
