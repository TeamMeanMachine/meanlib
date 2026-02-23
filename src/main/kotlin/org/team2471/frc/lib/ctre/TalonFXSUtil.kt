package org.team2471.frc.lib.ctre

import com.ctre.phoenix6.configs.MotionMagicConfigs
import com.ctre.phoenix6.configs.TalonFXSConfiguration
import com.ctre.phoenix6.controls.Follower
import com.ctre.phoenix6.hardware.TalonFX
import com.ctre.phoenix6.hardware.TalonFXS
import com.ctre.phoenix6.signals.ExternalFeedbackSensorSourceValue
import com.ctre.phoenix6.signals.FeedbackSensorSourceValue
import com.ctre.phoenix6.signals.GravityTypeValue
import com.ctre.phoenix6.signals.InvertedValue
import com.ctre.phoenix6.signals.MotorAlignmentValue
import com.ctre.phoenix6.signals.NeutralModeValue
import com.ctre.phoenix6.signals.StaticFeedforwardSignValue
import edu.wpi.first.wpilibj.DriverStation

/**
 * Add a follower to the main motor and applies the master's configuration.
 *
 * MUST call this function AFTER configuring the master motor.
 * If the master motor's configuration changes after this function is called, the follower configuration will NOT update to match the master motor.
 *
 * (If you need to change both configurations, create another [TalonFXS] object for the follower and apply configuration to both)
 *
 * @param followerID The CAN ID of a [TalonFXS] follower motor.
 * @param motorAlignment Relationship between this motor and the master motor direction.
 *
 * @see Follower
 * @see MotorAlignmentValue
 */
fun TalonFXS.addFollower(followerID: Int, motorAlignment: MotorAlignmentValue = MotorAlignmentValue.Aligned) {
    try {
        val follower = TalonFXS(followerID, network)
        val masterConfig = TalonFXSConfiguration()
        this.configurator.refresh(masterConfig)
        follower.configurator.apply(masterConfig)
        follower.setControl(Follower(deviceID, motorAlignment))
    } catch (e: Exception) {
        DriverStation.reportError("Failed to add follower to $deviceID: ${e.message}", true)
    }
}

/**
 * Add a follower to the main motor and applies the master's configuration.
 *
 * Make sure to call this function AFTER configuring the master motor.
 * If the master motor's configuration changes after this function is called, the follower configuration will NOT update to match the master motor.
 *
 * @param follower The follower motor.
 * @param motorAlignment Relationship between this motor and the master motor direction.
 *
 * @see Follower
 * @see MotorAlignmentValue
 */
fun TalonFXS.addFollower(follower: TalonFXS, motorAlignment: MotorAlignmentValue = MotorAlignmentValue.Aligned) = this.addFollower(follower.deviceID, motorAlignment)

/**
 * Set the supply current limits.
 * @param continuousLimit the continuous allowable limit
 * @param peakLimit the maximum possible current the motor can draw.
 * @param peakDuration amount of seconds the motor limits to [peakLimit], then it will limit to [continuousLimit]
 */
fun TalonFXSConfiguration.currentLimits(continuousLimit: Double, peakLimit: Double, peakDuration: Double): TalonFXSConfiguration {
    this.CurrentLimits.apply {
        SupplyCurrentLimit = peakLimit
        SupplyCurrentLowerLimit = continuousLimit
        SupplyCurrentLowerTime = peakDuration
        SupplyCurrentLimitEnable = true
    }
    return this
}

/**
 * Set the stator current limits.
 * @param peakLimit the maximum possible current the motor can draw.
 */
fun TalonFXSConfiguration.statorCurrentLimit(peakLimit: Double): TalonFXSConfiguration {
    this.CurrentLimits.apply {
        StatorCurrentLimit = peakLimit
        StatorCurrentLimitEnable = true
    }
    return this
}

/**
 * Motor will update its position and velocity whenever the CANcoder publishes its information on the CAN bus.
 * The motor's internal rotor will not be used.
 *
 * @param encoderID CAN ID of the CANcoder on the same CAN bus as the motor.
 * @param motorToSensorRatio number of motor rotations for 1 CANcoder rotation.
 * @param sensorToMechanismRatio number of sensor rotations for 1 mechanism rotation.
 *
 * @see FeedbackSensorSourceValue.RemoteCANcoder
 * @see fusedCANCoder
 * @see alternateFeedbackSensor
 *
 * @author Justin likes "Remote" modes better than "Fused." Test both but start with RemoteCANCoder.
 */
fun TalonFXSConfiguration.remoteCANCoder(encoderID: Int, motorToSensorRatio: Double, sensorToMechanismRatio: Double = 1.0): TalonFXSConfiguration =
    alternateFeedbackSensor(encoderID, ExternalFeedbackSensorSourceValue.RemoteCANcoder, motorToSensorRatio, sensorToMechanismRatio)

/**
 * Motor will fuse its position and velocity with another CANcoder. Slow speed will use CANcoder, fast speed will use motor rotor.
 *
 * Make sure the motor and encoder move in the same direction.
 *
 * @param encoderID CAN ID of the CANcoder on the same CAN bus as the motor.
 * @param motorToSensorRatio number of motor rotations for 1 CANcoder rotation.
 * @param sensorToMechanismRatio number of sensor rotations for 1 mechanism rotation.
 *
 * @see FeedbackSensorSourceValue.FusedCANcoder
 * @see remoteCANCoder
 * @see alternateFeedbackSensor
 *
 * @author Justin likes "Remote" modes better than "Fused." Test both but start with RemoteCANCoder.
 */
fun TalonFXSConfiguration.fusedCANCoder(encoderID: Int, motorToSensorRatio: Double, sensorToMechanismRatio: Double = 1.0): TalonFXSConfiguration =
    alternateFeedbackSensor(encoderID, ExternalFeedbackSensorSourceValue.FusedCANcoder, motorToSensorRatio, sensorToMechanismRatio)

/**
 * Sets the configs that affect the feedback sensor of this motor. Aka: What it will think its own position/velocity is.
 * Useful for eliminating control error between the motor and the mechanism.
 *
 * This will automatically apply any gear ratio you put in, causing the motor to be in the "mechanism perspective"
 *
 * @param encoderID CAN ID of the feedback device on the same CAN bus as the motor.
 * @param feedbackSensorSource the type of feedback device.
 * @param motorToSensorRatio number of motor rotations for 1 feedback device rotation.
 * @param sensorToMechanismRatio number of sensor rotations for 1 mechanism rotation.
 *
 * @see FeedbackSensorSourceValue
 * @see TalonFXSConfiguration.Feedback
 */
fun TalonFXSConfiguration.alternateFeedbackSensor(encoderID: Int, feedbackSensorSource: ExternalFeedbackSensorSourceValue, motorToSensorRatio: Double, sensorToMechanismRatio: Double = 1.0): TalonFXSConfiguration {
    this.ExternalFeedback.apply {
        ExternalFeedbackSensorSource = feedbackSensorSource
        FeedbackRemoteSensorID = encoderID
        RotorToSensorRatio = motorToSensorRatio
        SensorToMechanismRatio = sensorToMechanismRatio
    }
    return this
}

/**
 * The ratio of sensor rotations to the mechanism's output, where a ratio greater than 1 is a reduction.
 *
 * This is equivalent to the mechanism's gear ratio if the sensor is located on the input of a gearbox. If sensor is on the output of a gearbox, then this is typically set to 1.
 *
 * @param sensorToMechanismRatio The ratio of sensor rotations to the mechanism's output. Defaults to 1.
 *
 * @see TalonFXSConfiguration.Feedback
 */
fun TalonFXSConfiguration.sensorToMechanismRatio(sensorToMechanismRatio: Double): TalonFXSConfiguration {
    this.ExternalFeedback.SensorToMechanismRatio = sensorToMechanismRatio
    return this
}
/**
 * The ratio of motor rotor rotations to remote sensor rotations, where a ratio greater than 1 is a reduction.
 *
 * @param rotorToSensorRatio The ratio of motor rotor rotations to feedback sensor rotations. Defaults to 1.
 *
 * @see TalonFXSConfiguration.Feedback
 */
fun TalonFXSConfiguration.rotorToSensorRatio(rotorToSensorRatio: Double): TalonFXSConfiguration {
    this.ExternalFeedback.RotorToSensorRatio = rotorToSensorRatio
    return this
}

/**
 * Wrap differential difference position error within [-0.5, +0.5) mechanism rotations.
 *
 * @param continuousWrap Whether to wrap the position error. Defaults to false
 *
 * @see TalonFXSConfiguration.ClosedLoopGeneral
 */
fun TalonFXSConfiguration.continuousCloseLoopWrap(continuousWrap: Boolean): TalonFXSConfiguration {
    this.ClosedLoopGeneral.ContinuousWrap = continuousWrap
    return this
}

/**
 * Set whether the motor should be inverted.
 *
 * True - Clockwise_Positive
 *
 * False - CounterClockwise_Positive (Factory Default)
 *
 * @param invert whether to invert the motor
 *
 * @see InvertedValue.CounterClockwise_Positive
 * @see InvertedValue.Clockwise_Positive
 */
fun TalonFXSConfiguration.inverted(invert: Boolean): TalonFXSConfiguration =
    this.inverted(if (invert) InvertedValue.Clockwise_Positive else InvertedValue.CounterClockwise_Positive)


/**
 * Set which direction is positive for the motor.
 *
 * CounterClockwise_Positive (Factory Default)
 *
 * @param invertedValue determines the positive direction of the motor.
 *
 * @see InvertedValue.CounterClockwise_Positive
 * @see InvertedValue.Clockwise_Positive
 */
fun TalonFXSConfiguration.inverted(invertedValue: InvertedValue): TalonFXSConfiguration {
    this.MotorOutput.Inverted =  invertedValue
    return this
}

/**
 * Set motor neutral mode to brake.
 */
fun TalonFXSConfiguration.brakeMode(): TalonFXSConfiguration {
    this.MotorOutput.NeutralMode = NeutralModeValue.Brake
    return this
}

/**
 * Set motor neutral mode to coast.
 */
fun TalonFXSConfiguration.coastMode(): TalonFXSConfiguration {
    this.MotorOutput.NeutralMode = NeutralModeValue.Coast
    return this
}

/**
 * Set the proportional gain.
 *
 * @see TalonFXSConfiguration.Slot0
 */
fun TalonFXSConfiguration.p(p: Double): TalonFXSConfiguration {
    this.Slot0.kP = p
    return this
}

/**
 * Set the derivative gain.
 *
 * @see TalonFXSConfiguration.Slot0
 */
fun TalonFXSConfiguration.d(d: Double): TalonFXSConfiguration {
    this.Slot0.kD = d
    return this
}

/**
 * Set the integral gain.
 *
 * @see TalonFXSConfiguration.Slot0
 */
fun TalonFXSConfiguration.i(i: Double): TalonFXSConfiguration {
    this.Slot0.kI = i
    return this
}

/**
 * Set the static feedforward gain.
 *
 * @see StaticFeedforwardSignValue.UseClosedLoopSign
 * @see StaticFeedforwardSignValue.UseVelocitySign
 */
fun TalonFXSConfiguration.s(s: Double, staticFeedforwardSign: StaticFeedforwardSignValue): TalonFXSConfiguration {
    this.Slot0.apply {
        kS = s
        StaticFeedforwardSign = staticFeedforwardSign
    }
    return this
}

/**
 * Set the velocity feedforward gain.
 *
 * @see TalonFXSConfiguration.Slot0
 */
fun TalonFXSConfiguration.v(v: Double): TalonFXSConfiguration {
    this.Slot0.kV = v
    return this
}

/**
 * Set the acceleration feedforward gain.
 *
 * @see TalonFXSConfiguration.Slot0
 */
fun TalonFXSConfiguration.a(a: Double): TalonFXSConfiguration {
    this.Slot0.kA = a
    return this
}

/**
 * Set the gravity feedforward/feedback gain.
 *
 * @see GravityTypeValue.Elevator_Static
 * @see GravityTypeValue.Arm_Cosine
 */
fun TalonFXSConfiguration.g(g: Double, gravityType: GravityTypeValue): TalonFXSConfiguration {
    this.Slot0.apply {
        kG = g
        GravityType = gravityType
    }
    return this
}

/**
 * Configure the motion magic cruse velocity, acceleration, and optional jerk.
 *
 * @see TalonFXSConfiguration.MotionMagic
 */
fun TalonFXSConfiguration.motionMagic(cruseVelocity: Double, acceleration: Double, jerk: Double? = null): TalonFXSConfiguration {
    this.MotionMagic.apply {
        MotionMagicCruiseVelocity = cruseVelocity
        MotionMagicAcceleration = acceleration
        if (jerk != null) MotionMagicJerk = jerk
    }
    return this
}

/**
 * Configure the motion magic expo configs.
 *
 * @see TalonFXSConfiguration.MotionMagic
 * @see MotionMagicConfigs.MotionMagicExpo_kV
 * @see MotionMagicConfigs.MotionMagicExpo_kA
 * @see MotionMagicConfigs.MotionMagicCruiseVelocity
 */
fun TalonFXSConfiguration.motionMagicExpo(expoKV: Double, expoKA: Double, maxVelocity: Double? = null): TalonFXSConfiguration {
    this.MotionMagic.apply {
        MotionMagicExpo_kV = expoKV
        MotionMagicExpo_kA = expoKA
        if (maxVelocity != null) MotionMagicCruiseVelocity = maxVelocity
    }
    return this
}

/**
 * Applies a factory default configuration to the [TalonFXS].
 *
 * @param modifications optionally provide a block to modify the configuration before it gets sent to the motor.
 *
 * @see modifyConfiguration
 */
fun TalonFXS.applyConfiguration(modifications: TalonFXSConfiguration.() -> Unit = {}) {
    // Create a factory default configuration, apply modifications, then apply to the motor.
    this.configurator.apply(TalonFXSConfiguration().apply { modifications() })
}

/**
 * Modifies the configuration currently on the motor.
 *
 * @param overrides provide a block to modify the configuration before it gets sent to the device.
 *
 * @see applyConfiguration
 */
fun TalonFXS.modifyConfiguration(overrides: TalonFXSConfiguration.() -> Unit) {
    // Get the current motor configuration, apply modifications, then apply to the motor.
    val oldConfiguration = TalonFXSConfiguration()
    this.configurator.refresh(oldConfiguration) // Get motor configuration parameters
    this.configurator.apply(oldConfiguration.apply(overrides)) // Apply overrides to the config and send config to motor.
}