package org.kjkoster.wedo.usb;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A weak pointer to a USB device. USB devices may be plugged in or out at any
 * time. Due to the volatile nature of USB, we do not actually hand out USB
 * devices as objects. Instead this library takes the approach to use abstract
 * USB handles that may or may not point to a valid device.
 * 
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
public class Handle {
    private final String path;
    private final String productName;

    public Handle(final String path, final String productName) {
        this.path = checkNotNull(path);
        this.productName = checkNotNull(productName);
    }

    /**
     * Get the unique path for this USB device.
     * 
     * @return The unique path for this USB device.
     */
    public String getPath() {
        return path;
    }

    /**
     * Find the human readable product name of the device that this packet came
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
