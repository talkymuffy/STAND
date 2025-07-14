package com.tm.signal_interpretation;

import com.tm.signal_interpretation_helper_systems.ElectrodeSystem;
import com.tm.signal_interpretation_helper_systems.MotorTypes;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * {@code SignalReceiver}
 *
 * <p>This class serves as the signal interpretation hub for decoded MEG input data.
 * It receives raw signal strings (formatted like {@code "MEG_C3:512"}), extracts the
 * protocol and value, identifies relevant motor targets using {@link ElectrodeSystem}
 * and {@link MotorTypes}, and returns a formatted string for downstream usage.</p>
 *
 * <p>Key functions of this class:</p>
 * <ul>
 *   <li>Validates and parses incoming MEG signals.</li>
 *   <li>Maps MEG regions to motor configurations via {@link MotorTypes}.</li>
 *   <li>Formats motor activation data as {@code "digit1,digit2,...|motorId"}.</li>
 *   <li>Ensures robustness by logging and skipping malformed or unknown inputs.</li>
 * </ul>
 *
 * @author Saptansu Ghosh
 * @version 2.0
 *
 * @see ElectrodeSystem
 * @see MotorTypes
 */
public class SignalReceiver {

    /** Used to split the incoming message (e.g. "MEG_C3:512"). */
    private static final String DELIMITER = ":";

    /** Logger for diagnostics and error tracking. */
    private static final Logger LOGGER = Logger.getLogger(SignalReceiver.class.getName());

    /**
     * Holds the reverse mapping: each cortical region → list of motors it triggers.
     * Populated once on initialization and reused for all signal interpretation.
     */
    private final Map<ElectrodeSystem, List<MotorTypes>> regionToMotors;

    /**
     * Initializes the signal receiver by constructing the region-to-motor lookup map.
     */
    public SignalReceiver() {
        this.regionToMotors = buildRegionToMotorMap();
    }

    /**
     * Parses and processes a raw MEG signal message.
     * If valid, produces one formatted output line per motor triggered by the MEG region.
     *
     * Format of each output line:
     * <pre>
     * digit1,digit2,digit3,...|motorId
     * </pre>
     *
     * @param rawMessage the raw signal, such as {@code "MEG_C3:512"}
     * @return a newline-separated string of motor instructions, or empty string on failure
     */
    public String processRawSignal(String rawMessage) {
        // Step 1: Validate message format
        if (rawMessage == null) {
            LOGGER.log(Level.WARNING, "Received null rawMessage");
            return "";
        }

        rawMessage = rawMessage.trim();
        if (!rawMessage.contains(DELIMITER)) {
            LOGGER.log(Level.WARNING, "Malformed message missing delimiter: \"{0}\"", rawMessage);
            return "";
        }

        String[] parts = rawMessage.split(DELIMITER, 2);
        if (parts.length != 2) {
            LOGGER.log(Level.WARNING, "Unexpected number of segments after split: {0}", Arrays.toString(parts));
            return "";
        }

        String protocolId = parts[0];
        String valueStr   = parts[1];

        // Step 2: Convert value to integer
        int value;
        try {
            value = Integer.parseInt(valueStr);
        } catch (NumberFormatException nfe) {
            LOGGER.log(Level.WARNING, "Invalid numeric component: \"{0}\"", valueStr);
            return "";
        }

        // Step 3: Resolve MEG cortical region by protocol ID
        ElectrodeSystem region = findRegionByProtocol(protocolId);
        if (region == null) {
            LOGGER.log(Level.WARNING, "Unknown protocol identifier: \"{0}\"", protocolId);
            return "";
        }

        // Step 4: Lookup motors linked to this region
        List<MotorTypes> motors = regionToMotors.getOrDefault(region, Collections.emptyList());
        if (motors.isEmpty()) {
            LOGGER.log(Level.INFO, "No motors mapped to region: {0}", region);
            return "";
        }

        // Step 5: Convert value to digit CSV and assemble command lines
        String[] digits  = String.valueOf(value).split("");
        String   csv     = String.join(",", digits);
        List<String> outputLines = new ArrayList<>();

        for (MotorTypes motor : motors) {
            if (motor == null || motor.getId() == null) {
                LOGGER.log(Level.WARNING, "Invalid motor entry in mapping for region {0}", region);
                continue;
            }
            // Format: 5,1,2|s_j_1
            outputLines.add(csv + "|" + motor.getId());
        }

        return String.join("\n", outputLines);
    }

    // ----------------------------------------------------------------------
    // Internal helper methods
    // ----------------------------------------------------------------------

    /**
     * Builds the mapping from MEG regions (ElectrodeSystem) to motors (MotorTypes).
     *
     * <p>This constructs a reverse index using the configuration of each motor type
     * and the regions it responds to, avoiding hardcoded connections.</p>
     *
     * @return a map linking cortical regions to motor triggers
     */
    private Map<ElectrodeSystem, List<MotorTypes>> buildRegionToMotorMap() {
        Map<ElectrodeSystem, List<MotorTypes>> map = new EnumMap<>(ElectrodeSystem.class);

        for (MotorTypes motor : MotorTypes.values()) {
            if (motor == null) {
                LOGGER.log(Level.WARNING, "Skipped null motor in enumeration");
                continue;
            }

            List<ElectrodeSystem> regions = motor.getMappedElectrodes();
            if (regions == null || regions.isEmpty()) {
                LOGGER.log(Level.WARNING, "No mapped electrodes found for motor: {0}", motor);
                continue;
            }

            for (ElectrodeSystem region : regions) {
                if (region == null) {
                    LOGGER.log(Level.WARNING, "Null region in motor mapping for motor {0}", motor);
                    continue;
                }
                map.computeIfAbsent(region, k -> new ArrayList<>()).add(motor);
            }
        }

        return map;
    }

    /**
     * Resolves a MEG protocol string (like {@code "MEG_C3"}) to its matching
     * {@link ElectrodeSystem} enum constant.
     *
     * @param protocolId the protocol string received from the MEG system
     * @return matching ElectrodeSystem, or null if not recognized
     */
    private ElectrodeSystem findRegionByProtocol(String protocolId) {
        for (ElectrodeSystem region : ElectrodeSystem.values()) {
            if (region != null && protocolId.equals(region.getProtocolId())) {
                return region;
            }
        }
        return null;
    }
}