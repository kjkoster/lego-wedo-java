package org.kjkoster.wedo.ble112;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import org.thingml.bglib.BDAddr;

import com.google.common.hash.HashCode;

/**
 * A BLE112 MAC address. We don't use BGAPI's BDAddr class, because it does not
 * implement {@link HashCode} or equals. BDAddr also leaks its internal array,
 * making it mutable.
 *
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
public class BLE112Address {
    private final String macString;
    private final byte[] macBytes = new byte[6];
    private final int address_type;

    /**
     * Create a new MAC address from a BGAPI sender.
     * 
     * @param sender
     *            The sender to take the address from.
     * @param address_type
     *            The address type.
     */
    public BLE112Address(final BDAddr sender, final int address_type) {
        super();

        checkNotNull(sender, "null sender");
        this.macString = sender.toString().intern();
        // clone the mutable argument into this object
        for (int i = 0; i < 6; i++) {
            macBytes[i] = sender.getByteAddr()[i];
        }

        checkArgument(address_type == 0 || address_type == 1 || false,
                "bad address type %d", address_type);
        this.address_type = address_type;
    }

    /**
     * The BLE address type for this address.
     * 
     * @return the address_type.
     */
    public int getAddress_type() {
        return address_type;
    }

    /**
     * Get the BDAddr object that the BGAPI needs. Since that object is mutable
     * we provide a copy of our MAC address, not the address itself.
     * 
     * @return A BGAddr that contains the same MAC address as we do.
     */
    public BDAddr getBDAddr() {
        final byte[] mac = new byte[6];
        for (int i = 0; i < 6; i++) {
            mac[i] = macBytes[i];
        }
        return new BDAddr(mac);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return macString.hashCode();
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof BLE112Address)) {
            return false;
        }
        return macString.equals(((BLE112Address) obj).macString);
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return macString;
    }
}
