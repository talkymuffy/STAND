package com.tm.signal_interpretation_helper_systems;

/**
 * Defines each motor’s configuration: its angle limits, PWM channel,
 * identifier string (used in your protocol), and default start position.
 */
public enum MotorTypes {
    SERVO_L(10, 180, "s_j_L", 0, 60),    // Left Servo
    SERVO_R(10, 180, "s_j_R", 1, 60),    // Right Servo

    SERVO_J_1(10, 400, "s_j_1",   1, 70),
    SERVO_J_2(10, 380, "s_j_2",   1, 47),
    SERVO_J_3(10, 380, "s_j_3",   1, 63),
    SERVO_J_4(10, 120, "s_j_4",   1, 63),

    STEPPER_BASE(10, 180, "stp_200_360", 1, 0);

    private final int minAngle;
    private final int maxAngle;
    private final String id;
    private final int channel;
    private final int defaultPosition;

    /**
     * @param minAngle        minimum allowed angle (degrees)
     * @param maxAngle        maximum allowed angle (degrees)
     * @param id              protocol identifier (e.g. “s_j_L”)
     * @param channel         PWM or stepper driver channel
     * @param defaultPosition default resting position (degrees or steps)
     */
    MotorTypes(int minAngle, int maxAngle, String id, int channel, int defaultPosition) {
        this.minAngle        = minAngle;
        this.maxAngle        = maxAngle;
        this.id              = id;
        this.channel         = channel;
        this.defaultPosition = defaultPosition;
    }

    /** @return minimum angle (inclusive) */
    public int getMinAngle() {
        return minAngle;
    }

    /** @return maximum angle (inclusive) */
    public int getMaxAngle() {
        return maxAngle;
    }

    /** @return the motor’s protocol identifier */
    public String getId() {
        return id;
    }

    /** @return the PWM/stepper channel number */
    public int getChannel() {
        return channel;
    }

    /** @return default starting position */
    public int getDefaultPosition() {
        return defaultPosition;
    }

    @Override
    public String toString() {
        return id;
    }
}