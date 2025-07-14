package com.tm.signal_interpretation;

import com.fazecast.jSerialComm.SerialPort;
import com.tm.signal_interpretation_helper_systems.MotorTypes;

import java.nio.charset.StandardCharsets;

/**
 * <h4>Signal Receiver</h4>
 * <p>Receives ASCII-encoded signal samples (one per line), parses them as ints,
 * and returns a pipe-delimited string of all samples received since last call.</p>
 * <h6>Member Methods:</h6>
 * <ul>
 *   <li>{@code SignalReceiver()} – default COM3 @ 9600 bps</li>
 *   <li>{@code SignalReceiver(String)} – custom port, 9600 bps</li>
 *   <li>{@code SignalReceiver(String, int)} – custom port & baud</li>
 *   <li>{@code signalValuePerSecondWithSetBandWidth()} – returns all samples as {@code “v1|v2|…”}</li>
 *   <li>{@code interPeteredValue()} – internal parser + buffer</li>
 * </ul>
 *
 * <h6>NOTE: <p>The FORMAT IS STILL IN WORKS AND NEEDS TO BE TAKEN CARE OF ONCE WE HAVE THE MACHINE ITSELF READY TO WORK ALONG WITH THE S.T.A.N.D. Physical Device.</p></h6>
 *
 * <h6>See Also:</h6>
 * <ul>
 *  <li>{@link com.tm.signal_interpretation_helper_systems.MotorTypes} -Contains The Servo Motor Types [Servo and Stepper Motor Types]</li>
 * </ul>
 * @author Saptansu Ghosh
 * @version 1.2
 */
public class SignalReceiver {
    public static SerialPort PORT;
    public String PORT_NAME;
    int BAUD_RATE;

    // Buffer For Fragments Between The Read Gaps
    private final StringBuilder LEFTOVER = new StringBuilder();

    /**
     * {@code SignalReceiver()} - No Parameters So Use {@code COM3} and {@code 9600} As Default Values*/
    public SignalReceiver() {
        this("COM3", 9600);
    }

    /**
     * {@code SignalReceiver()} - One Parameters So Use{@code 9600} As Default Value
     * @param portName Name Of The Port When Creating An Instance Of Single Parameter Using {@code new}*/
    public SignalReceiver(String portName) {
        this(portName, 9600);
    }

    /**
     * {@code SignalReceiver()} - Two Parameters So No Default Value Used.
     * @param portName Name Of The Port When Creating An Instance Of Single Parameter Using {@code new}
     * @param baudRate Baud Rate In No. Of Bits For The Port To Receive The Data In Packets Of.*/

    public SignalReceiver(String portName, int baudRate) {
        this.PORT_NAME = portName;
        this.BAUD_RATE = baudRate;
        setPortName();
        configurePort();
    }

    /**
     * Set Port Name Here- {@code setPortName()}*/
    private void setPortName() {
        PORT = SerialPort.getCommPort(PORT_NAME);
    }

    /**
     * Configure Port Here- {@code configurePort()}*/
    private void configurePort() {
        PORT.setComPortParameters(BAUD_RATE, 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
        PORT.setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, 0, 0);
        if (!PORT.isOpen()) {
            PORT.openPort();
        }
    }

    /**
     * Reads and parses all complete signal samples from the serial port, then
     * picks a target motor based on the last sample value.
     *
     * <p>
     * Workflow:
     * <ol>
     *   <li>Invoke {@link #interPeteredValue()} to fetch and buffer raw ASCII lines
     *       from the serial port, returning a pipe-delimited string of ints,
     *       e.g. "512|600|450|".</li>
     *   <li>If no complete samples arrived, immediately return
     *       "NO_DATA|NO MOTORS".</li>
     *   <li>Strip the trailing "|" and split on "|" to get individual sample tokens.</li>
     *   <li>Attempt to parse the last token as an integer; if that fails,
     *       return "&lt;allValues&gt; | NO_MOTORS".</li>
     *
     *   <li>Build and return the final string in the format:
     *       "&lt;v1&gt;|&lt;v2&gt;|…| &lt;servo_id&gt;".</li>
     * </ol>
     *
     * @return pipe-delimited samples plus space, pipe, space and the chosen
     *         motor ID; or "NO_DATA|NO MOTORS" if no samples were available.
     *
     * <br>
     * To Be Replaced With A System Which Will Check For The Required Joint Changes.
     */
    public String signalValuePerSecondWithSetBandWidth() {
        String parsed = interPeteredValue();   // e.g. "512|600|450|"
        if (parsed.isEmpty()) {
            return "NO_DATA|NO MOTORS";
        }

        // Remove trailing '|' so we can split cleanly
        String valuesOnly = parsed.substring(0, parsed.length() - 1);

        // Split into individual sample strings
        String[] tokens = valuesOnly.split("\\|");
        String last = tokens[tokens.length - 1];


        // Final output: "<v1>|<v2>|...| <servo_id>"
        return valuesOnly + " | " + updateMotorOfID();

    }

    /**
     * Internal: Checks For The Required Motor And Then Returns The ID Of The Motor.
     * For Fallback, It Returns {@code null} As A String Type.
     * */
    private String updateMotorOfID(){





        return "null";
    }

    /**
     * Internal: accumulate bytes into leftover buffer, split on {@code '\n'},
     * parse each line as integer, append {@code "|"}, and keep incomplete tail.
     */
    private String interPeteredValue() {
        StringBuilder output = new StringBuilder();//Make A New StringBuilder Instance

        //Store The Available Bytes From The Port
        int available = PORT.bytesAvailable();

        //If There Is Any Available Byes
        if (available > 0) {
            //Making An Array Of Bytes Acting As Buffer
            byte[] buf = new byte[available];
            //Reading The Numbers Coming From The Port
            int numRead = PORT.readBytes(buf, buf.length);

            //Building A Sequence of Characters
            String chunk = new String(buf, 0, numRead, StandardCharsets.UTF_8);
            //Append Every Single Line On LEFTOVER--
            LEFTOVER.append(chunk);
        }

        //Store The Date Till Now In allData Variable
        String allData = LEFTOVER.toString();

        //Get The Index Of The New Line in -newlineIndex
        int newlineIndex;
        // Process Each Complete Line
        while ((newlineIndex = allData.indexOf('\n')) >= 0) {
            //Store in line
            String line = allData.substring(0, newlineIndex).trim();
            //If line is not empty do the tasks
            if (!line.isEmpty()) {
                try {
                    //Get The Value Of The Line in Int -> <v> |
                    int value = Integer.parseInt(line);
                    output.append(value).append("|");
                } catch (NumberFormatException e) {
                    //Check For Parse-ability Fails To An Exception
                    output.append(line).append(",UNPARSEABLE").append("|");
                }
            }
            //Store The Whole Data Into allData
            allData = allData.substring(newlineIndex + 1);
        }

        // save any partial line back into leftover
        LEFTOVER.setLength(0);
        LEFTOVER.append(allData);

        return output.toString();
    }
}