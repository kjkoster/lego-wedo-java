package org.kjkoster.wedo.bricks;

import static com.google.common.base.Preconditions.checkArgument;
import static org.kjkoster.wedo.bricks.Brick.FIRST_PORT;

import java.util.HashMap;
import java.util.Map;

import lombok.NonNull;

/**
 * Sometimes we have to remember what value we set an actuator to. The WeDo and
 * SBrick protocols force us to write the actuator values for all bricks at the
 * same time. Even if we just want to set the value for one brick, we still have
 * to write both values.
 *
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
public class ActuatorValueMemory {
    private final int ports;
    private final Map<String, Map<Character, Byte>> rememberedActuatorValues = new HashMap<>();

    /**
     * Set up a new actuator value memory cache, specifying the number of ports
     * to accept to this cache.
     * 
     * @param ports
     *            The number of ports to accept on this cache.
     */
    public ActuatorValueMemory(final int ports) {
        super();

        this.ports = ports;
    }

    /**
     * Write a new value into the actuator memory.
     * 
     * @param hub
     *            The hub to write for.
     * @param port
     *            The port on the hub to write for.
     * @param value
     *            The value to memorise for that hub and port.
     */
    public void write(@NonNull final Hub hub, final char port,
            final byte value) {
        checkArgument(port >= FIRST_PORT && port < FIRST_PORT + ports);

        Map<Character, Byte> rememberedActuatorValue = rememberedActuatorValues
                .get(hub.getPath());
        if (rememberedActuatorValue == null) {
            rememberedActuatorValue = new HashMap<>();
            rememberedActuatorValues.put(hub.getPath(),
                    rememberedActuatorValue);
        }

        rememberedActuatorValue.put(port, value);
    }

    /**
     * Read a value from the actuator memory. We return the last value that was
     * written for this hub and port, or return 0x00 when no value was ever
     * written.
     * 
     * @param hub
     *            The hub to read for.
     * @param port
     *            The port to read for.
     * @return The value for that hub and port, with a default of 0x00 if no
     *         value was ever written.
     */
    public byte read(@NonNull final Hub hub, final char port) {
        checkArgument(port >= FIRST_PORT && port < FIRST_PORT + ports);

        final Map<Character, Byte> rememberedActuatorValue = rememberedActuatorValues
                .get(hub.getPath());
        if (rememberedActuatorValue == null) {
            return (byte) 0x00;
        }

        final Byte otherValue = rememberedActuatorValue.get(port);
        if (otherValue == null) {
            return (byte) 0x00;
        }

        return otherValue;
    }
}
