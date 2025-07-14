package com.tm.signal_interpretation;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * BluetoothMotorTransmitter handles sending formatted motor command strings
 * over a Bluetooth serial link.
 *
 * <p>
 * Each command should be separated by a newline. This class writes UTF-8-encoded
 * bytes to the provided OutputStream, flushes immediately, and logs any errors.
 * </p>
 *
 * @author Saptansu Ghosh
 * @version 1.3
 */
public class BluetoothMotorTransmitter {

    private static final Logger LOGGER =
            Logger.getLogger(BluetoothMotorTransmitter.class.getName());

    /** Underlying stream to the Bluetooth serial port */
    private final OutputStream out;

    /**
     * Constructs the transmitter using the given Bluetooth OutputStream.
     *
     * @param bluetoothOutputStream the OutputStream for the Bluetooth serial port
     * @throws NullPointerException if {@code bluetoothOutputStream} is null
     */
    public BluetoothMotorTransmitter(OutputStream bluetoothOutputStream) {
        if (bluetoothOutputStream == null) {
            throw new NullPointerException(
                    "bluetoothOutputStream must not be null");
        }
        this.out = bluetoothOutputStream;
    }

    /**
     * Sends each line in {@code formattedCommands} over Bluetooth.
     * Blank or null input is ignored.
     *
     * @param formattedCommands newline-separated motor command strings
     */
    public void transmit(String formattedCommands) {
        if (formattedCommands == null || formattedCommands.isEmpty()) {
            return;
        }

        // split on any line terminator (Windows, Unix, etc.)
        String[] lines = formattedCommands.split("\\R");
        for (String line : lines) {
            if (line.trim().isEmpty()) {
                continue;
            }
            try {
                // Append newline and convert to UTF-8 bytes
                byte[] data = (line + "\n")
                        .getBytes(StandardCharsets.UTF_8);
                out.write(data);
                out.flush();
            } catch (IOException e) {
                LOGGER.log(
                        Level.SEVERE,
                        String.format(
                                "Failed to transmit command over Bluetooth: %s",
                                line),
                        e);
            }
        }
    }
}