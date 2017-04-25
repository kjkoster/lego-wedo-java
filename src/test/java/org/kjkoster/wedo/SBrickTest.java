package org.kjkoster.wedo;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.kjkoster.wedo.bricks.Brick.Type;
import org.kjkoster.wedo.bricks.Hub;
import org.kjkoster.wedo.transport.ble112.BLE112Address;

/**
 * Test the SBrick command line utility.
 *
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
public class SBrickTest {
    /**
     * A test case.
     */
    @Test
    public void testOnePort() {
        final Hub hub = SBrickCommandlineUtility.parseBrick("0:7:80:d0:52:bf,light");

        assertEquals(new BLE112Address("0:7:80:d0:52:bf", 0),
                hub.getBLE112Address());
        assertEquals(Type.LIGHT, hub.getBricks().get(0).getType());
        assertEquals(Type.NOT_CONNECTED, hub.getBricks().get(1).getType());
        assertEquals(Type.NOT_CONNECTED, hub.getBricks().get(2).getType());
        assertEquals(Type.NOT_CONNECTED, hub.getBricks().get(3).getType());
    }

    /**
     * A test case.
     */
    @Test
    public void testOnePortAndLotsOfCommas() {
        final Hub hub = SBrickCommandlineUtility.parseBrick("0:7:80:d0:52:bf,light,,,,,,");

        assertEquals(new BLE112Address("0:7:80:d0:52:bf", 0),
                hub.getBLE112Address());
        assertEquals(Type.LIGHT, hub.getBricks().get(0).getType());
        assertEquals(Type.NOT_CONNECTED, hub.getBricks().get(1).getType());
        assertEquals(Type.NOT_CONNECTED, hub.getBricks().get(2).getType());
        assertEquals(Type.NOT_CONNECTED, hub.getBricks().get(3).getType());
    }

    /**
     * A test case.
     */
    @Test
    public void testJustCommas() {
        final Hub hub = SBrickCommandlineUtility.parseBrick("0:7:80:d0:52:bf,,,,,,,");

        assertEquals(new BLE112Address("0:7:80:d0:52:bf", 0),
                hub.getBLE112Address());
        assertEquals(Type.NOT_CONNECTED, hub.getBricks().get(0).getType());
        assertEquals(Type.NOT_CONNECTED, hub.getBricks().get(1).getType());
        assertEquals(Type.NOT_CONNECTED, hub.getBricks().get(2).getType());
        assertEquals(Type.NOT_CONNECTED, hub.getBricks().get(3).getType());
    }

    /**
     * A test case.
     */
    @Test
    public void testFewCommas() {
        final Hub hub = SBrickCommandlineUtility.parseBrick("0:7:80:d0:52:bf,,");

        assertEquals(new BLE112Address("0:7:80:d0:52:bf", 0),
                hub.getBLE112Address());
        assertEquals(Type.NOT_CONNECTED, hub.getBricks().get(0).getType());
        assertEquals(Type.NOT_CONNECTED, hub.getBricks().get(1).getType());
        assertEquals(Type.NOT_CONNECTED, hub.getBricks().get(2).getType());
        assertEquals(Type.NOT_CONNECTED, hub.getBricks().get(3).getType());
    }

    /**
     * A test case.
     */
    @Test
    public void testNoCommas() {
        final Hub hub = SBrickCommandlineUtility.parseBrick("0:7:80:d0:52:bf");

        assertEquals(new BLE112Address("0:7:80:d0:52:bf", 0),
                hub.getBLE112Address());
        assertEquals(Type.NOT_CONNECTED, hub.getBricks().get(0).getType());
        assertEquals(Type.NOT_CONNECTED, hub.getBricks().get(1).getType());
        assertEquals(Type.NOT_CONNECTED, hub.getBricks().get(2).getType());
        assertEquals(Type.NOT_CONNECTED, hub.getBricks().get(3).getType());
    }

    /**
     * A test case.
     */
    @Test
    public void testTwoPorts() {
        final Hub hub = SBrickCommandlineUtility.parseBrick("0:7:80:d0:52:bf,,light,motor");

        assertEquals(new BLE112Address("0:7:80:d0:52:bf", 0),
                hub.getBLE112Address());
        assertEquals(Type.NOT_CONNECTED, hub.getBricks().get(0).getType());
        assertEquals(Type.LIGHT, hub.getBricks().get(1).getType());
        assertEquals(Type.MOTOR, hub.getBricks().get(2).getType());
        assertEquals(Type.NOT_CONNECTED, hub.getBricks().get(3).getType());
    }

    /**
     * A test case.
     */
    @Test
    public void testAllPorts() {
        final Hub hub = SBrickCommandlineUtility
                .parseBrick("0:7:80:d0:52:bf,Motor,light,motor,tilt");

        assertEquals(new BLE112Address("0:7:80:d0:52:bf", 0),
                hub.getBLE112Address());
        assertEquals(Type.MOTOR, hub.getBricks().get(0).getType());
        assertEquals(Type.LIGHT, hub.getBricks().get(1).getType());
        assertEquals(Type.MOTOR, hub.getBricks().get(2).getType());
        assertEquals(Type.TILT, hub.getBricks().get(3).getType());
    }
}
