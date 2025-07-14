package com.tm.signal_interpretation_helper_systems;

/**
 * <h5>{@code ElectrodeSystem}</h5>
 * <p>Represents virtual cortical targets used in MEG-based motor decoding.
 *  MEG magnetometers detect synchronized neuronal magnetic fields, and this
 *  enum maps source‐localized regions to protocol IDs for downstream
 *  robotic control.</p>
 *
 * @author Saptansu Ghosh
 * @version 1.2
 */
public enum ElectrodeSystem {

    /**
     * {@code MOTOR_C3}
     * Left hemisphere motor cortex, linked to right‐hand and wrist movement.
     */
    MOTOR_C3("motor_cortex_left", "MEG_C3"),

    /**
     * {@code MOTOR_C4}
     * Right hemisphere motor cortex, controls left‐hand movement.
     */
    MOTOR_C4("motor_cortex_right", "MEG_C4"),

    /**
     * {@code MOTOR_CZ}
     * Midline motor cortex, orchestrates trunk and bilateral limb movement.
     */
    MOTOR_CZ("motor_cortex_midline", "MEG_CZ"),

    /**
     * {@code PREMOTOR_L}
     * Left premotor area, involved in planning right‐side movements.
     */
    PREMOTOR_L("premotor_left", "MEG_PM_L"),

    /**
     * {@code PREMOTOR_R}
     * Right premotor area, involved in planning left‐side movements.
     */
    PREMOTOR_R("premotor_right", "MEG_PM_R"),

    /**
     * {@code SMA}
     * Supplementary Motor Area, coordinates sequential and bimanual tasks.
     */
    SMA("supplementary_motor_area", "MEG_SMA"),

    /**
     * {@code PARIETAL_L}
     * Left parietal cortex, integrates somatosensory feedback for right‐hand control.
     */
    PARIETAL_L("parietal_left", "MEG_PAR_L"),

    /**
     * {@code PARIETAL_R}
     * Right parietal cortex, integrates sensory data for left‐hand coordination.
     */
    PARIETAL_R("parietal_right", "MEG_PAR_R");

    /** Human‐readable label of this cortical region */
    private final String description;

    /** Protocol identifier used for routing or signal mapping */
    private final String protocolId;

    ElectrodeSystem(String description, String protocolId) {
        this.description = description;
        this.protocolId = protocolId;
    }

    /**
     * @return the human‐readable description of this region
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return the protocol identifier for signal mapping
     */
    public String getProtocolId() {
        return protocolId;
    }

    /**
     * @return a combined string of protocol ID and description
     */
    @Override
    public String toString() {
        return protocolId + " (" + description + ")";
    }
}