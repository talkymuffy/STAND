package com.tm;

import com.fazecast.jSerialComm.SerialPort;
import com.tm.signal_interpretation.BluetoothMotorTransmitter;
import com.tm.signal_interpretation.SignalEngine;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main application class for the MEG-to-Arduino pipeline.
 *
 * <p>
 * Steps performed:
 * <ol>
 *   <li>Select a COM port (or use the first available).</li>
 *   <li>Configure and open the port for Bluetooth (HC-05/HC-06 at 9600 8-N-1).</li>
 *   <li>Perform a PING/PONG handshake to verify the device is present.</li>
 *   <li>Start {@link SignalEngine}, parsing MEG signals and forwarding motor commands.</li>
 *   <li>Listen for “exit” on the console to trigger a clean shutdown.</li>
 * </ol>
 * </p>
 *
 * @author  Saptansu Ghosh
 * @version 1.4
 */
public class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    /**
     * Application entry point.
     *
     * @param args optional first argument is the COM port name (e.g. "COM3")
     */
    public static void main(String[] args) {
        // 1) Choose serial port
        String portName = (args.length > 0)
                ? args[0]
                : chooseDefaultPort();

        // 2) Configure and open port
        SerialPort port = SerialPort.getCommPort(portName);
        configurePort(port);
        if (!port.openPort()) {
            LOGGER.severe(String.format(
                    "Failed to open serial port \"%s\". Exiting.", portName));
            return;
        }
        LOGGER.info(String.format(
                "Opened serial port \"%s\" at %d baud.",
                portName, port.getBaudRate()));

        // 3) Register JVM shutdown hook (in case of Ctrl-C or crash)
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("JVM shutdown hook: cleaning up resources...");
            closeResources(port, null, null);
        }));

        try (
                // 4) Scanner for console commands
                Scanner console = new Scanner(System.in, StandardCharsets.UTF_8)
        ) {
            // 5) Acquire I/O streams
            InputStream in  = port.getInputStream();
            OutputStream out = port.getOutputStream();

            // 6) Handshake
            if (!performHandshake(in, out)) {
                LOGGER.severe("Handshake failed. No device response. Exiting.");
                return;
            }
            LOGGER.info("Handshake successful.");

            // 7) Start the signal-engine thread
            Thread engineThread = startSignalEngine(in, out);

            // 8) Listen for "exit" command on console
            LOGGER.info("Type 'exit' + Enter to shut down.");
            while (console.hasNextLine()) {
                String line = console.nextLine().trim();
                if ("exit".equalsIgnoreCase(line)) {
                    LOGGER.info("'exit' received: initiating shutdown...");
                    engineThread.interrupt();
                    closeResources(port, in, out);
                    break;
                }
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during setup or execution:", e);
        }
    }

    /**
     * Sends "PING" and awaits "PONG" response within timeout.
     *
     * @return true if handshake succeeded
     */
    private static boolean performHandshake(InputStream in, OutputStream out) {
        final String PING          = "PING\n";
        final String EXPECTED_PONG = "PONG";
        final long   TIMEOUT_MS    = 2_000;
        long deadline = System.currentTimeMillis() + TIMEOUT_MS;

        try {
            out.write(PING.getBytes(StandardCharsets.UTF_8));
            out.flush();
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to send handshake ping:", e);
            return false;
        }

        Scanner scanner = new Scanner(in, StandardCharsets.UTF_8);
        try {
            while (System.currentTimeMillis() < deadline) {
                if (scanner.hasNextLine()) {
                    String resp = scanner.nextLine().trim();
                    if (EXPECTED_PONG.equalsIgnoreCase(resp)) {
                        return true;
                    }
                }
                Thread.sleep(50);
            }
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        } finally {
            scanner.close();
        }
        return false;
    }

    /**
     * Launches {@link SignalEngine} in its own thread, wiring its output
     * to the provided Bluetooth OutputStream.
     *
     * @return the running engine thread
     */
    /**
     * Launches {@link SignalEngine} in its own thread, using the transmitter
     * to send motor command strings over Bluetooth.
     *
     * @param in  the MEG signal InputStream
     * @param out the Bluetooth OutputStream
     * @return the thread running SignalEngine
     */
    private static Thread startSignalEngine(InputStream in, OutputStream out) {
        // Instantiate Bluetooth transmitter module
        BluetoothMotorTransmitter transmitter = new BluetoothMotorTransmitter(out);

        // Pass transmitter::transmit as the output handler to SignalEngine
        Consumer<String> bluetoothSender = transmitter::transmit;

        SignalEngine engine = new SignalEngine(in, bluetoothSender);
        Thread thread = new Thread(engine, "SignalEngine-Thread");
        thread.setDaemon(false);
        thread.start();
        return thread;
    }

    /**
     * Returns the first available COM port name, or defaults to "COM3".
     */
    private static String chooseDefaultPort() {
        String[] names = Arrays.stream(SerialPort.getCommPorts())
                .map(SerialPort::getSystemPortName)
                .toArray(String[]::new);
        LOGGER.info("Available serial ports: " + Arrays.toString(names));
        return (names.length > 0) ? names[0] : "COM3";
    }

    /**
     * Applies standard Bluetooth settings: 9600 baud, 8N1, blocking read.
     */
    private static void configurePort(SerialPort port) {
        port.setBaudRate(9600);
        port.setNumDataBits(8);
        port.setParity(SerialPort.NO_PARITY);
        port.setNumStopBits(SerialPort.ONE_STOP_BIT);
        port.setComPortTimeouts(
                SerialPort.TIMEOUT_READ_BLOCKING,
                /* readTimeoutMillis= */ 0,
                /* writeTimeoutMillis= */ 0
        );
    }

    /**
     * Closes the InputStream, OutputStream, and SerialPort.
     */
    private static void closeResources(
            SerialPort port,
            InputStream in,
            OutputStream out
    ) {
        try { if (in  != null) in.close();  } catch (IOException ignored) {}
        try { if (out != null) out.close(); } catch (IOException ignored) {}
        port.closePort();
    }
}