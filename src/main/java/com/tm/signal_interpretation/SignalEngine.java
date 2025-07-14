package com.tm.signal_interpretation;

import java.io.InputStream;
import java.util.Objects;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * SignalEngine
 *
 * <p>
 * Acts as the continuous listener and dispatcher for MEG‐decoded signals.
 * On application startup, this engine binds to any InputStream (serial port,
 * socket, or standard input) that delivers raw MEG messages, processes each
 * line in real time, and forwards formatted motor commands to downstream
 * systems via a provided callback.
 * </p>
 *
 * <p>
 * Key responsibilities:
 * <ul>
 *   <li>Initialize and manage the core {@link SignalReceiver} instance.</li>
 *   <li>Continuously poll the input stream without blocking the entire JVM.</li>
 *   <li>Gracefully handle malformed input, interruptions, and unexpected errors.</li>
 *   <li>Log lifecycle events and diagnostic information for debugging.</li>
 *   <li>Forward each non-empty formatted output to a {@code Consumer<String>}
 *       for transmission (e.g. Bluetooth to Arduino).</li>
 * </ul>
 * </p>
 *
 * @author Saptansu Ghosh
 * @version 1.3
 * @see     SignalReceiver
 */
public class SignalEngine implements Runnable {

    /** Logger for lifecycle and error diagnostics */
    private static final Logger LOGGER = Logger.getLogger(SignalEngine.class.getName());

    /**
     * Core interpreter that parses raw messages and maps them to motor commands.
     * This instance is stateless and thread-safe in its usage pattern.
     */
    private final SignalReceiver receiver;

    /**
     * Scanner wrapping the provided InputStream to read newline-separated messages.
     * Avoids manual buffering or low-level stream handling.
     */
    private final Scanner scanner;

    /**
     * Callback that receives each formatted output string. You can plug in
     * your Bluetooth transmitter or any other handler here.
     */
    private final Consumer<String> outputHandler;

    /**
     * Constructs a SignalEngine bound to a given InputStream and output handler.
     *
     * @param in             any InputStream delivering one MEG message per line.
     *                       Examples:
     *                       <ul>
     *                         <li>serialPort.getInputStream() for a COM-port feed</li>
     *                         <li>socket.getInputStream() for network-based acquisition</li>
     *                         <li>System.in for console testing or simulation</li>
     *                       </ul>
     * @param outputHandler  consumes each formatted string (e.g. sends over Bluetooth)
     * @throws NullPointerException if {@code in} or {@code outputHandler} is null
     */
    public SignalEngine(InputStream in, Consumer<String> outputHandler) {
        Objects.requireNonNull(in, "InputStream must not be null");
        Objects.requireNonNull(outputHandler, "OutputHandler must not be null");

        this.receiver      = new SignalReceiver();
        this.scanner       = new Scanner(in);
        this.outputHandler = outputHandler;
    }

    /**
     * Entry point for the SignalEngine’s listening loop.
     *
     * <p>
     * The loop follows this sequence:
     * <ol>
     *   <li>Check for the next line in the InputStream.</li>
     *   <li>Sleep briefly (10ms) if no data is available to avoid busy-waiting.</li>
     *   <li>Read, trim, and discard empty lines.</li>
     *   <li>Invoke {@link SignalReceiver#processRawSignal(String)} to parse/format.</li>
     *   <li>Forward non-empty results to {@code outputHandler}.</li>
     *   <li>Catch and log {@link InterruptedException} to allow clean shutdown.</li>
     *   <li>Catch all other exceptions to keep the loop alive on unexpected errors.</li>
     * </ol>
     * </p>
     */
    @Override
    public void run() {
        LOGGER.info("SignalEngine started... listening for MEG input...");

        while (true) {
            try {
                // If no new line is available, pause briefly to yield CPU.
                if (!scanner.hasNextLine()) {
                    Thread.sleep(10);
                    continue;
                }

                // Read one raw MEG message and trim whitespace.
                String raw = scanner.nextLine().trim();

                // Skip blank lines to prevent unnecessary processing.
                if (raw.isEmpty()) {
                    continue;
                }

                // Delegate parsing/formatting to SignalReceiver.
                String formatted = receiver.processRawSignal(raw);

                // Forward each non-empty formatted string to the output handler.
                if (!formatted.isEmpty()) {
                    outputHandler.accept(formatted);
                }

            } catch (InterruptedException ie) {
                // Thread was interrupted—exit gracefully.
                LOGGER.log(Level.WARNING, "SignalEngine interrupted, shutting down.", ie);
                break;

            } catch (Exception ex) {
                // Catch-all: log and continue to ensure high availability.
                LOGGER.log(Level.SEVERE, "Unexpected error in SignalEngine loop", ex);
            }
        }

        // Clean up resources on exit.
        scanner.close();
        LOGGER.info("SignalEngine stopped.");
    }
}