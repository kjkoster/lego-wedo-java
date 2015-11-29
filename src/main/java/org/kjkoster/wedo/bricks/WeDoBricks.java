package org.kjkoster.wedo.bricks;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.System.out;
import static org.kjkoster.wedo.bricks.Brick.Type.DISTANCE;
import static org.kjkoster.wedo.bricks.Brick.Type.LIGHT;
import static org.kjkoster.wedo.bricks.Brick.Type.MOTOR;
import static org.kjkoster.wedo.bricks.Brick.Type.NOT_CONNECTED;
import static org.kjkoster.wedo.bricks.Brick.Type.TILT;
import static org.kjkoster.wedo.bricks.Brick.Type.UNKNOWN;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.kjkoster.wedo.bricks.Brick.Type;
import org.kjkoster.wedo.usb.Handle;
import org.kjkoster.wedo.usb.Usb;

/**
 * A class to represent the collection of LEGO WeDo hubs and bricks that are
 * connected to this computer. This class tries to soften the rough edges of the
 * WeDo bricks enough so that in most situations simple changes go unnoticed to
 * the program using them.
 * <p>
 * Addressing WeDo bricks is more tricky than I expected. Four problems: 1) the
 * WeDo hubs have no identifying information. 2) When a motor is running or a
 * light is shining, the associated block ID is set such that we can no longer
 * see what is a motor and what is a light. 3) USB hubs can be removed and added
 * at any time. Re-plugging changes the order and identity of the hubs, even if
 * the system still looks the same to the user. 4) Likewise, WeDo bricks can be
 * plugged in and out or moved at any time.
 * 
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
public class WeDoBricks {
    private final Usb usb;
    private final boolean verbose;

    /**
     * We have to remember what value we set an actuator to. The protocol forces
     * us to write the actuator values for bricks A and B at the same time. Even
     * if we just want to set the value for one brick, we still have to write
     * both values.
     */
    private final Map<String, Map<Boolean, Byte>> rememberedActuatorValues = new HashMap<>();

    /**
     * We have to remember what type an actuator has. The running motors and
     * lights share ID's, making it impossible to see what is what.
     */
    private final Map<String, Map<Boolean, Type>> rememberedActuatorTypes = new HashMap<>();

    /**
     * Create a new WeDo bricks abstraction layer.
     * 
     * @param usb
     *            The USB subsystem that this layer uses for accessing the
     *            actual LEGO WeDo hardware.
     * @param verbose
     *            Print a trace of all interaction with the LEGO WeDo bricks.
     */
    public WeDoBricks(final Usb usb, final boolean verbose) {
        this.usb = checkNotNull(usb);
        this.verbose = verbose;
    }

    /**
     * Read a map of all the bricks. Find any LEGO WeDo hubs and read a packet
     * from each of them. Hubs can be plugged in and out at any time, so it is a
     * surprise how many bricks we get every time.
     * 
     * @return All the bricks, neatly laid out in a map.
     */
    public Map<Handle, Brick[]> readAll() {
        final Map<Handle, Brick[]> bricks = new HashMap<>();
        for (final Map.Entry<Handle, byte[]> packetRead : usb.readFromAll()
                .entrySet()) {
            bricks.put(packetRead.getKey(),
                    parseBrickAB(packetRead.getKey(), packetRead.getValue()));
        }
        return bricks;
    }

    private synchronized Brick[] parseBrickAB(final Handle handle,
            final byte[] buffer) {
        final Brick[] brickAB = new Brick[2];

        final Type brickAType = findType(handle, true, buffer[3]);
        brickAB[0] = new Brick(handle, true, brickAType, buffer[3], buffer[2]);
        if (verbose) {
            out.println("read  " + brickAB[0]);
        }

        final Type brickBType = findType(handle, false, buffer[5]);
        brickAB[1] = new Brick(handle, false, brickBType, buffer[5], buffer[4]);
        if (verbose) {
            out.println("read  " + brickAB[1]);
        }

        return brickAB;
    }

    @SuppressWarnings("cast")
    private Type findType(final Handle handle, final boolean isA, final byte id) {
        Type type;
        switch ((int) id & 0xff) {
        case 0xe6:
        case 0xe7:
            type = NOT_CONNECTED;
            break;
        case 0xee:
        case 0xef:
        case 0xf0:
        case 0xf1:
            type = MOTOR;
            break;
        case 0xcb:
        case 0xcc:
        case 0xcd:
            type = LIGHT;
            break;
        case 0xb0:
        case 0xb1:
        case 0xb2:
        case 0xb3:
            type = DISTANCE;
            break;
        case 0x26:
        case 0x27:
            type = TILT;
            break;
        default:
            type = UNKNOWN;
        }

        Map<Boolean, Type> hub = rememberedActuatorTypes.get(handle.getPath());
        if (hub == null) {
            hub = new HashMap<>();
            rememberedActuatorTypes.put(handle.getPath(), hub);
        }

        if (type == UNKNOWN && hub.containsKey(isA)) {
            type = hub.get(isA);
        }

        if (type != UNKNOWN && type != NOT_CONNECTED) {
            hub.put(isA, type);
        } else {
            hub.remove(isA);
        }

        return type;
    }

    /**
     * Set all motors to the speed. This does nothing if no motor was found.
     * 
     * @param speed
     *            The speed to run the motors at (-127 to 127, 0 is off).
     */
    public void motor(final byte speed) {
        actuator(true, true, speed, true, false);
    }

    /**
     * Set all motors on connector A to the specified speed. This does nothing
     * if no motor was found.
     * 
     * @param speed
     *            The speed to run the motors at (-127 to 127, 0 is off).
     */
    public void motorA(final byte speed) {
        actuator(true, false, speed, true, false);
    }

    /**
     * Set all motors on connector B to the specified speed. This does nothing
     * if no motor was found.
     * 
     * @param speed
     *            The speed to run the motors at (-127 to 127, 0 is off).
     */
    public void motorB(final byte speed) {
        actuator(false, true, speed, true, false);
    }

    /**
     * Set all lights to the specified intensity. This does nothing if no light
     * was found.
     * 
     * @param intensity
     *            The intensity to set the lights to (0 to 127, 0 is off).
     */
    public void light(final byte intensity) {
        actuator(true, true, intensity, false, true);
    }

    /**
     * Set all lights on connector A to the specified intensity. This does
     * nothing if no light was found.
     * 
     * @param intensity
     *            The intensity to set the lights to (0 to 127, 0 is off).
     */
    public void lightA(final byte intensity) {
        actuator(true, false, intensity, false, true);
    }

    /**
     * Set all lights on connector B to the specified intensity. This does
     * nothing if no light was found.
     * 
     * @param intensity
     *            The intensity to set the lights to (0 to 127, 0 is off).
     */
    public void lightB(final byte intensity) {
        actuator(false, true, intensity, false, true);
    }

    public void all(final byte value) {
        actuator(true, true, value, true, true);
    }

    public void allA(final byte value) {
        actuator(true, false, value, true, true);
    }

    public void allB(final byte value) {
        actuator(false, true, value, true, true);
    }

    /**
     * Setting a motor speed is trickier than you'd think in the case of a
     * multi-hub system. Here we set "motor A", but we still don't know
     * <i>which</i> "motor A". There might be a "motor A" on every hub. So we
     * take the blunt approach by setting all "motor A"'s to the specified
     * speed. It is then up to the user to make sure that the motors are hooked
     * up properly for addressing.
     * 
     * TODO We use code 64 to set motor speed. Code 60 allows for other values
     * too. See
     * https://github.com/PetrGlad/lego-wedo4j/blob/master/src/main/java
     * /com/salaboy/legowedo4j/impl/WedoMotorImpl.java
     */
    private void actuator(final boolean setA, final boolean setB,
            final byte value, final boolean setMotor, final boolean setLight) {
        checkArgument(setA || setB);

        final Map<Handle, Brick[]> hubs = readAll();
        for (final Map.Entry<Handle, Brick[]> hub : hubs.entrySet()) {
            if (setA) {
                actuator(hub.getValue()[0], value, setMotor, setLight);
            }
            if (setB) {
                actuator(hub.getValue()[1], value, setMotor, setLight);
            }
        }
    }

    private void actuator(final Brick brick, final byte value,
            final boolean setMotor, final boolean setLight) {
        if (setMotor && brick.getType() == MOTOR) {
            write(brick, value);
        }
        if (setLight && brick.getType() == LIGHT) {
            write(brick, value);
        }
    }

    private synchronized void write(final Brick brick, final byte value) {
        // read the 'other' value
        final byte otherValue = lookupOtherValue(brick);
        storeNewValue(brick, value);

        final byte valueA = (byte) ((brick.isA() ? value : otherValue) & 0xff);
        final byte valueB = (byte) ((brick.isA() ? otherValue : value) & 0xff);
        final byte[] buffer = new byte[9];
        buffer[0] = 0x00;
        buffer[1] = 0x40;
        buffer[2] = valueA;
        buffer[3] = valueB;
        buffer[4] = 0x00;
        buffer[5] = 0x00;
        buffer[6] = 0x00;
        buffer[7] = 0x00;
        buffer[8] = 0x00;

        if (verbose) {
            out.printf("write %s -> value A: 0x%02x value B: 0x%02x\n", brick,
                    valueA, valueB);
        }

        usb.write(brick.getHandle(), buffer);
    }

    private void storeNewValue(final Brick brick, final byte value) {
        Map<Boolean, Byte> hub = rememberedActuatorValues.get(brick.getHandle()
                .getPath());
        if (hub == null) {
            hub = new HashMap<>();
            rememberedActuatorValues.put(brick.getHandle().getPath(), hub);
        }

        hub.put(brick.isA(), value);
    }

    private byte lookupOtherValue(final Brick brick) {
        final Map<Boolean, Byte> hub = rememberedActuatorValues.get(brick
                .getHandle().getPath());
        if (hub == null) {
            return (byte) 0x00;
        }

        // note the bang (!) because we look up the other value
        final Byte otherValue = hub.get(!brick.isA());
        if (otherValue == null) {
            return (byte) 0x00;
        }

        return otherValue;
    }

    /**
     * Reset the devices by setting all values to 0.
     */
    public void reset() {
        final Map<Handle, Brick[]> hubs = readAll();
        for (final Map.Entry<Handle, Brick[]> hub : hubs.entrySet()) {
            write(hub.getValue()[0], (byte) 0x00);
            write(hub.getValue()[1], (byte) 0x00);
        }
    }

    /**
     * Read all distance sensors.
     * 
     * @return The distance values. May be empty, but is never <code>null</code>
     */
    public Collection<Distance> readDistances() {
        final Collection<Distance> distances = new ArrayList<>();
        for (final Brick[] brick : readAll().values()) {
            if (brick[0].getType() == DISTANCE) {
                distances.add(brick[0].getDistance());
            }
            if (brick[1].getType() == DISTANCE) {
                distances.add(brick[1].getDistance());
            }
        }
        return distances;
    }

    /**
     * Read all tilt sensors.
     * 
     * @return The tilt values. May be empty, but is never <code>null</code>.
     */
    public Collection<Tilt> readTilts() {
        final Collection<Tilt> tilts = new ArrayList<>();
        for (final Brick[] brick : readAll().values()) {
            if (brick[0].getType() == TILT) {
                tilts.add(brick[0].getTilt());
            }
            if (brick[1].getType() == TILT) {
                tilts.add(brick[1].getTilt());
            }
        }
        return tilts;
    }
}
