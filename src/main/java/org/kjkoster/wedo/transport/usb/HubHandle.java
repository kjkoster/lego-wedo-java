package org.kjkoster.wedo.transport.usb;

import static com.google.common.base.Preconditions.checkNotNull;

import lombok.Value;

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
@Value
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
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return path;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof HubHandle)) {
            return false;
        }
        return path.equals(((HubHandle) obj).path);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return path.hashCode();
    }
}
