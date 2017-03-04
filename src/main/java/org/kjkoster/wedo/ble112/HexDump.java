package org.kjkoster.wedo.ble112;

import static java.lang.System.out;
import static java.nio.charset.StandardCharsets.UTF_8;

// Found as http://pastebin.com/UyEFrsR7
class HexDump {

    /**
     * Formatted hex dump like Wireshark does.
     * 
     * @return The formatted string organised by columns
     */
    public static String hexDump(final String argument, final byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        int step = 8;
        long startAddress = 0L;

        for (int i = 0; i < bytes.length; ++i) {
            if (i % step == 0 && i > 0) {
                sb.append(" '");
                sb.append(getPrintableString(bytes, i - step, step));
                sb.append("'\n");
            }

            if (i % step == 0) {
                sb.append(String.format("    %s %04d:  ", argument,
                        startAddress));
                startAddress += step;
            }

            sb.append(String.format("0x%02x ", bytes[i] & 0xff));

            if (i == bytes.length - 1) {
                // We print out the last printable part
                boolean isMultipleLength = (step
                        - (bytes.length % step)) == step;
                String padder = new String(
                        new char[step - (bytes.length % step)]).replace("\0",
                                "     ");
                if (isMultipleLength)
                    padder = "";
                sb.append(padder);
                sb.append(" '");

                int offset = bytes.length - (bytes.length % step);
                int size = (bytes.length % step);
                if (offset < 0)
                    offset = 0; // buffer size less than 'step'
                if (size > bytes.length)
                    size = bytes.length; // buffer size less than 'step'

                if (isMultipleLength) {
                    offset = bytes.length - step;
                    size = step;
                }

                sb.append(getPrintableString(bytes, offset, size));
                sb.append("'\n");
            }
        }
        return sb.toString();
    }

    public static String getPrintableString(byte[] bytes, int offset,
            int length) {
        StringBuilder sb = new StringBuilder(length);

        for (int i = offset; i < offset + length; i++) {
            boolean isPrintable = (bytes[i] >= 33 && bytes[i] <= 126);
            char c = (char) (isPrintable ? bytes[i] : '.');
            sb.append(c);
        }

        return sb.toString();
    }

    public static final void main(String[] args) {
        // Some tests

        byte[] testShort = { 0x10, 0x10, 0x10, 0x55, 0x12 };

        byte[] testLarge = { 0x1, 0x2, 0x3, 0x4, 0x5, 0x6, 0x7, 0x8, 0x9, 0x10,
                0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18, 0x19, 0x20,
                0x21, 0x22, 0x23, 0x24, 0x25, 0x26, 0x27, 0x28, 0x29, 0x30,
                0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x40,
                0x41, 0x42, 0x43, 0x44, 0x45, 0x46, 0x47, 0x48, 0x49, 0x50,
                0x51, 0x52, 0x53, 0x54, 0x55, 0x56, 0x57, 0x58, 0x59, 0x60,
                0x61, 0x62, 0x63, 0x64, 0x65, 0x66, 0x67, 0x68, 0x69, 0x70,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };

        out.println(hexDump("short", testShort));

        out.println(hexDump("loooong", testLarge));

        out.println(hexDump("poo",
                "The pile of poo UTF-8 test: \u20AC \uD83D\uDCA9."
                        .getBytes(UTF_8)));
    }
}