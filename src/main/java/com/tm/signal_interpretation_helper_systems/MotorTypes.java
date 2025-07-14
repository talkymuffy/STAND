package com.tm.signal_interpretation_helper_systems;

import java.util.List;

/**
 * {@code MotorTypes}
 * Defines each motor’s configuration and its mapping to MEG-based cortical targets.
 * Each enum constant encapsulates:
 * <ul><li>angle/step limits</li>
 * <li>protocol ID (for serial commands)</li>
 * <li>driver channel</li>
 *  <li> default start position</li>
 *  <li>list of triggering MEG regions</li>
 *  </ul>
 *
 *   @author Saptansu Ghosh
 *   @version 2.0
 *  @see ElectrodeSystem
 */
public enum MotorTypes {

    /**
     * {@code SERVO_L}
     * Left shoulder servo (abduction/adduction).
     * Triggered by {@link ElectrodeSystem#PREMOTOR_L} and {@link ElectrodeSystem#MOTOR_C3}.
     */
    SERVO_L(
            10, 180,
            "s_j_L",
            0,
            60,
            List.of(ElectrodeSystem.PREMOTOR_L, ElectrodeSystem.MOTOR_C3)
    ),

    /**
     * {@code SERVO_R}
     * Right shoulder servo (abduction/adduction).
     * Triggered by {@link ElectrodeSystem#PREMOTOR_R} and {@link ElectrodeSystem#MOTOR_C4}.
     */
    SERVO_R(
            10, 180,
            "s_j_R",
            1,
            60,
            List.of(ElectrodeSystem.PREMOTOR_R, ElectrodeSystem.MOTOR_C4)
    ),

    /**
     * {@code SERVO_J_1}
     * Elbow joint 1 (flexion/extension).
     * Triggered by {@link ElectrodeSystem#MOTOR_C3} and {@link ElectrodeSystem#PARIETAL_L}.
     */
    SERVO_J_1(
            10, 400,
            "s_j_1",
            2,
            70,
            List.of(ElectrodeSystem.MOTOR_C3, ElectrodeSystem.PARIETAL_L)
    ),

    /**
     * {@code SERVO_J_2}
     * Elbow joint 2 (supination/pronation).
     * Triggered by {@link ElectrodeSystem#SMA} and {@link ElectrodeSystem#MOTOR_C3}.
     */
    SERVO_J_2(
            10, 380,
            "s_j_2",
            3,
            47,
            List.of(ElectrodeSystem.SMA, ElectrodeSystem.MOTOR_C3)
    ),

    /**
     * {@code SERVO_J_3}
     * Wrist flexion/extension joint.
     * Triggered by {@link ElectrodeSystem#MOTOR_CZ} and {@link ElectrodeSystem#PARIETAL_L}.
     */
    SERVO_J_3(
            10, 380,
            "s_j_3",
            4,
            63,
            List.of(ElectrodeSystem.MOTOR_CZ, ElectrodeSystem.PARIETAL_L)
    ),

    /**
     * {@code SERVO_J_4}
     * Wrist rotation joint.
     * Triggered by {@link ElectrodeSystem#MOTOR_C3} and {@link ElectrodeSystem#PREMOTOR_L}.
     */
    SERVO_J_4(
            10, 120,
            "s_j_4",
            5,
            63,
            List.of(ElectrodeSystem.MOTOR_C3, ElectrodeSystem.PREMOTOR_L)
    ),

    /**
     * {@code STEPPER_BASE}
     * Base rotation stepper (360° turn).
     * Triggered by {@link ElectrodeSystem#SMA}, {@link ElectrodeSystem#PARIETAL_L},
     * and {@link ElectrodeSystem#PARIETAL_R}.
     */
    STEPPER_BASE(
            10, 180,
            "stp_200_360",
            6,
            0,
            List.of(
                    ElectrodeSystem.SMA,
                    ElectrodeSystem.PARIETAL_L,
                    ElectrodeSystem.PARIETAL_R
            )
    );

    /** Minimum allowed angle or step (inclusive). */
    private final int minAngle;

    /** Maximum allowed angle or step (inclusive). */
    private final int maxAngle;

    /** Protocol identifier (e.g. {@code "s_j_L"}). */
    private final String id;

    /** PWM/stepper driver channel index. */
    private final int channel;

    /** Default resting position (degrees or steps). */
    private final int defaultPosition;

    /** MEG regions whose signals drive this motor. */
    private final List<ElectrodeSystem> mappedElectrodes;

    /**
     * @param minAngle         minimum allowed angle or step (inclusive)
     * @param maxAngle         maximum allowed angle or step (inclusive)
     * @param id               protocol identifier for this motor
     * @param channel          driver channel index
     * @param defaultPosition  default resting position (degrees or steps)
     * @param mappedElectrodes list of {@link ElectrodeSystem} regions whose MEG
     *                         signals trigger this motor
     */
    MotorTypes(int minAngle,
               int maxAngle,
               String id,
               int channel,
               int defaultPosition,
               List<ElectrodeSystem> mappedElectrodes) {
        this.minAngle = minAngle;
        this.maxAngle = maxAngle;
        this.id = id;
        this.channel = channel;
        this.defaultPosition = defaultPosition;
        this.mappedElectrodes = mappedElectrodes;
    }

    /** @return minimum allowed angle or step (inclusive) */
    public int getMinAngle() {
        return minAngle;
    }

    /** @return maximum allowed angle or step (inclusive) */
    public int getMaxAngle() {
        return maxAngle;
    }

    /** @return protocol identifier for this motor */
    public String getId() {
        return id;
    }

    /** @return PWM/stepper driver channel index */
    public int getChannel() {
        return channel;
    }

    /** @return default resting position (degrees or steps) */
    public int getDefaultPosition() {
        return defaultPosition;
    }

    /**
     * @return list of {@link ElectrodeSystem} regions whose MEG signals drive this motor
     */
    public List<ElectrodeSystem> getMappedElectrodes() {
        return mappedElectrodes;
    }

    /** @return human-readable description for logging or UI */
    @Override
    public String toString() {
        return id
                + " on channel " + channel
                + " [" + minAngle + "-" + maxAngle + "] triggers "
                + mappedElectrodes;
    }
}