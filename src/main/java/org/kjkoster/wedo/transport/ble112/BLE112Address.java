package org.kjkoster.wedo.transport.ble112;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import org.thingml.bglib.BDAddr;

import com.google.common.hash.HashCode;

import lombok.Value;

/**
 * A BLE112 MAC address. We don't use BGAPI's BDAddr class, because it does not
 * implement {@link HashCode} or equals. BDAddr also leaks its internal array,
 * making it mutable.
 *
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
@Value
public class BLE112Address {
    private final byte[] macBytes = new byte[6];
    private final String macString;
    private final int address_type;

    /**
     * Create a new MAC address from a MAC tring.
     * 
     * @param macBytes
     *            The sender to take the address from.
     * @param address_type
     *            The address type.
     */
    public BLE112Address(final byte[] macBytes, final int address_type) {
        super();

        checkNotNull(macBytes, "null MAC address");
        checkArgument(macBytes.length == 6, "expected length of 6, found %d",
                macBytes.length);
        for (int i = 0; i < 6; i++) {
            this.macBytes[i] = macBytes[i];
        }

        this.macString = String.format("%02x:%02x:%02x:%02x:%02x:%02x",
                macBytes[0], macBytes[1], macBytes[2], macBytes[3], macBytes[4],
                macBytes[5]);

        checkArgument(address_type == 0 || address_type == 1,
                "bad address type %d", address_type);
        this.address_type = address_type;
    }

    /**
     * Create a new MAC address from a MAC tring.
     * 
     * @param mac
     *            The sender to take the address from.
     * @param address_type
     *            The address type.
     */
    public BLE112Address(final String mac, final int address_type) {
        this(splitBytes(mac), address_type);
    }

    private static byte[] splitBytes(String mac) {
        checkNotNull(mac, "null MAC address");
        final String[] macBytes = mac.split(":");
        checkArgument(macBytes.length == 6, "expected length of 6, found %d",
                macBytes.length);
        final byte[] bytes = new byte[6];
        for (int i = 0; i < 6; i++) {
            bytes[i] = (byte) Integer.parseInt(macBytes[i], 16);
        }
        return bytes;
    }

    /**
     * Create a new MAC address from a BGAPI sender. The internal byte
     * representation of that class is reversed to match the wire protocol, so
     * we have to reverse the byte ordering before we can use the data.
     * 
     * @param sender
     *            The sender to take the address from.
     * @param address_type
     *            The address type.
     */
    public BLE112Address(final BDAddr sender, final int address_type) {
        this(reverse(sender.getByteAddr()), address_type);
    }

    private static byte[] reverse(byte[] macBytes) {
        final byte[] bytes = new byte[6];
        for (int i = 0; i < 6; i++) {
            bytes[i] = macBytes[5 - i];
        }
        return bytes;

    }

    /**
     * Get the BDAddr object that the BGAPI needs. Since that object is mutable
     * we provide a (reversed) copy of our MAC address, not the address itself.
     * 
     * @return A BGAddr that contains the same MAC address as we do.
     */
    public BDAddr getBDAddr() {
        return new BDAddr(reverse(macBytes));
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return macBytes[5] << 41 | macBytes[4] << 33 | macBytes[3] << 25
                | macBytes[2] << 17 | macBytes[1] << 9 | macBytes[0] << 1
                | address_type;
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
        // This is usually a bad idea, but we have the entire address encoded in
        // the hashCode. Might as well use it.
        return hashCode() == obj.hashCode();
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return macString;
    }
}
