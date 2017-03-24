package org.kjkoster.wedo.transport.ble112;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;
import org.thingml.bglib.BDAddr;

/**
 * Tests for the BLE112 address parsing and handling.
 *
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
public class BLE112AddressTest {
    /**
     * A test case.
     */
    @Test(expected = NullPointerException.class)
    public void nullStringShouldThrowException() {
        new BLE112Address((String) null, 0);
    }

    /**
     * A test case.
     */
    @Test(expected = NullPointerException.class)
    public void nullBDAddrShouldThrowException() {
        new BLE112Address((BDAddr) null, 0);
    }

    /**
     * A test case.
     */
    @Test(expected = IllegalArgumentException.class)
    public void emptyStringShouldThrowException() {
        new BLE112Address("", 0);
    }

    /**
     * A test case.
     */
    @Test
    public void zeroPaddedAndNoPaddingShouldResultInSameAddress() {
        final BLE112Address notPadded = new BLE112Address("0:7:80:d0:52:bf", 1);
        final BLE112Address zeroPadded = new BLE112Address("00:07:80:d0:52:bf",
                1);

        assertEquals("00:07:80:d0:52:bf", notPadded.toString());
        assertEquals("00:07:80:d0:52:bf", zeroPadded.toString());
        assertEquals(notPadded.hashCode(), zeroPadded.hashCode());
        assertEquals(notPadded, zeroPadded);
    }

    /**
     * A test case.
     */
    @Test
    public void zeroPaddedAndBAddrShouldResultInSameAddress() {
        final BLE112Address fromString = new BLE112Address("0:7:80:d0:52:bf",
                1);
        final byte[] macBytes = { (byte) 0xbf, (byte) 0x52, (byte) 0xd0,
                (byte) 0x80, (byte) 0x07, (byte) 0x00 };
        final BDAddr bdAddr = new BDAddr(macBytes);
        final BLE112Address fromBDAddr = new BLE112Address(bdAddr, 1);

        assertEquals("00:07:80:d0:52:bf", fromString.toString());
        assertEquals("00:07:80:d0:52:bf", fromBDAddr.toString());
        assertEquals(fromString.hashCode(), fromBDAddr.hashCode());
        assertEquals(fromString, fromBDAddr);
    }

    /**
     * A test case.
     */
    @Test
    public void zeroPaddedAndStringBAddrShouldResultInSameAddress() {
        final BLE112Address fromString = new BLE112Address("0:7:80:d0:52:bf",
                1);
        final BLE112Address fromBDAddr = new BLE112Address(
                BDAddr.fromString("0:7:80:d0:52:bf"), 1);

        assertEquals("00:07:80:d0:52:bf", fromString.toString());
        assertEquals("00:07:80:d0:52:bf", fromBDAddr.toString());
        assertEquals(fromString.hashCode(), fromBDAddr.hashCode());
        assertEquals(fromString, fromBDAddr);
    }

    /**
     * A test case.
     */
    @Test
    public void bdaddrShouldPassThroughBLE112AddressUnharmed() {
        final BDAddr original = BDAddr.fromString("0:7:80:d0:52:bf");
        final BDAddr viaBLE112Address = new BLE112Address(original, 0)
                .getBDAddr();

        for (int i = 0; i < 6; i++) {
            assertEquals(original.getByteAddr()[i],
                    viaBLE112Address.getByteAddr()[i]);
        }
    }

    /**
     * A test case.
     */
    @Test
    public void differentAddressTypesShouldMakeAddressesDifferent() {
        final BLE112Address addr_1 = new BLE112Address("0:7:80:d0:52:bf", 1);
        final BLE112Address addr_1_too = new BLE112Address("0:7:80:d0:52:bf",
                1);
        final BLE112Address addr_0 = new BLE112Address("00:07:80:d0:52:bf", 0);

        assertEquals(addr_1.hashCode(), addr_1_too.hashCode());
        assertEquals(addr_1, addr_1_too);

        assertNotEquals(addr_0.hashCode(), addr_1.hashCode());
        assertNotEquals(addr_0, addr_1);
    }
}
