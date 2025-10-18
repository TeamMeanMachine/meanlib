package org.team2471.frc.lib.ctre

import com.ctre.phoenix6.configs.MotionMagicConfigs
import com.ctre.phoenix6.configs.TalonFXConfiguration
import com.ctre.phoenix6.controls.Follower
import com.ctre.phoenix6.hardware.TalonFX
import com.ctre.phoenix6.signals.FeedbackSensorSourceValue
import com.ctre.phoenix6.signals.GravityTypeValue
import com.ctre.phoenix6.signals.InvertedValue
import com.ctre.phoenix6.signals.NeutralModeValue
import com.ctre.phoenix6.signals.StaticFeedforwardSignValue
import edu.wpi.first.wpilibj.DriverStation

/**
 * Add a follower to the main motor and applies the master's configuration.
 *
 * Make sure to call this function AFTER configuring the master motor.
 * If the master motor's configuration changes after this function is called, the follower configuration will NOT update to match the master motor.
 *
 * @param followerID The CAN ID of a [TalonFX] follower motor.
 * @param opposeMasterDirection Whether to respect the master motor's invert setting or do the opposite.
 *
 * @see Follower
 */
fun TalonFX.addFollower(followerID: Int, opposeMasterDirection: Boolean = false) {
    try {
        val follower = TalonFX(followerID, network)
        val masterConfig = TalonFXConfiguration()
        this.configurator.refresh(masterConfig)
        follower.configurator.apply(masterConfig)
        follower.setControl(Follower(deviceID, opposeMasterDirection))
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
 * @param opposeMasterDirection Whether to respect the master motor's invert setting or do the opposite.
 *
 * @see Follower
 */
fun TalonFX.addFollower(follower: TalonFX, opposeMasterDirection: Boolean) = this.addFollower(follower.deviceID, opposeMasterDirection)

/**
 * Set the supply current limits.
 * @param continuousLimit the continuous allowable limit
 * @param peakLimit the maximum possible current the motor can draw.
 * @param peakDuration amount of seconds the motor limits to [peakLimit], then it will limit to [continuousLimit]
 */
fun TalonFXConfiguration.currentLimits(continuousLimit: Double, peakLimit: Double, peakDuration: Double): TalonFXConfiguration {
    this.CurrentLimits.apply {
        SupplyCurrentLimit = peakLimit
        SupplyCurrentLowerLimit = continuousLimit
        SupplyCurrentLowerTime = peakDuration
        SupplyCurrentLimitEnable = true
    }
    return this
}

/**
 * Motor will update its position and velocity whenever the CANcoder publishes its information on the CAN bus.
 * The motor's internal rotor will not be used.
 *
 * @param encoderID CAN ID of the CANcoder.
 * @param motorToSensorRatio number of motor rotations for 1 CANcoder rotation.
 * @param sensorToMechanismRatio number of sensor rotations for 1 mechanism rotation.
 *
 * @see FeedbackSensorSourceValue.RemoteCANcoder
 * @see fusedCANCoder
 * @see alternateFeedbackSensor
 *
 * @author Justin likes "Remote" better than "Fused." Test both but start with RemoteCANCoder.
 */
fun TalonFXConfiguration.remoteCANCoder(encoderID: Int, motorToSensorRatio: Double, sensorToMechanismRatio: Double = 1.0): TalonFXConfiguration =
    alternateFeedbackSensor(encoderID, FeedbackSensorSourceValue.RemoteCANcoder, motorToSensorRatio, sensorToMechanismRatio)

/**
 * Motor will fuse its position and velocity with another CANcoder. Slow speed will use CANcoder, fast speed will use motor rotor.
 *
 * Make sure the motor and encoder move in the same direction.
 *
 * @param encoderID CAN ID of the CANcoder.
 * @param motorToSensorRatio number of motor rotations for 1 CANcoder rotation.
 * @param sensorToMechanismRatio number of sensor rotations for 1 mechanism rotation.
 *
 * @see FeedbackSensorSourceValue.FusedCANcoder
 * @see remoteCANCoder
 * @see alternateFeedbackSensor
 *
 * @author Justin likes "Remote" better than "Fused." Test both but start with RemoteCANCoder.
 */
fun TalonFXConfiguration.fusedCANCoder(encoderID: Int, motorToSensorRatio: Double, sensorToMechanismRatio: Double = 1.0): TalonFXConfiguration =
    alternateFeedbackSensor(encoderID, FeedbackSensorSourceValue.FusedCANcoder, motorToSensorRatio, sensorToMechanismRatio)

/**
 * Sets the configs that affect the feedback of this motor. Aka: What it will think its own position/velocity is.
 * Useful for eliminating control error between the motor and the mechanism.
 *
 * This will automatically apply any gear ratio you put in, causing the motor to be in the "mechanism perspective"
 *
 * @param encoderID CAN ID of the feedback device.
 * @param feedbackSensorSource the type of feedback device.
 * @param motorToSensorRatio number of motor rotations for 1 feedback device rotation.
 * @param sensorToMechanismRatio number of sensor rotations for 1 mechanism rotation.
 *
 * @see FeedbackSensorSourceValue
 * @see TalonFXConfiguration.Feedback
 */
fun TalonFXConfiguration.alternateFeedbackSensor(encoderID: Int, feedbackSensorSource: FeedbackSensorSourceValue, motorToSensorRatio: Double, sensorToMechanismRatio: Double = 1.0): TalonFXConfiguration {
    this.Feedback.apply {
        FeedbackSensorSource = feedbackSensorSource
        FeedbackRemoteSensorID = encoderID
        RotorToSensorRatio = motorToSensorRatio
        SensorToMechanismRatio = sensorToMechanismRatio
    }
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
fun TalonFXConfiguration.inverted(invert: Boolean): TalonFXConfiguration {
    this.MotorOutput.Inverted = if (invert) InvertedValue.Clockwise_Positive else InvertedValue.CounterClockwise_Positive
    return this
}

/**
 * Set motor neutral mode to brake.
 */
fun TalonFXConfiguration.brakeMode(): TalonFXConfiguration {
    this.MotorOutput.NeutralMode = NeutralModeValue.Brake
    return this
}

/**
 * Set motor neutral mode to coast.
 */
fun TalonFXConfiguration.coastMode(): TalonFXConfiguration {
    this.MotorOutput.NeutralMode = NeutralModeValue.Coast
    return this
}

/**
 * Set the proportional gain.
 *
 * @see TalonFXConfiguration.Slot0
 */
fun TalonFXConfiguration.p(p: Double): TalonFXConfiguration {
    this.Slot0.kP = p
    return this
}

/**
 * Set the derivative gain.
 *
 * @see TalonFXConfiguration.Slot0
 */
fun TalonFXConfiguration.d(d: Double): TalonFXConfiguration {
    this.Slot0.kD = d
    return this
}

/**
 * Set the integral gain.
 *
 * @see TalonFXConfiguration.Slot0
 */
fun TalonFXConfiguration.i(i: Double): TalonFXConfiguration {
    this.Slot0.kI = i
    return this
}

/**
 * Set the static feedforward gain.
 *
 * @see StaticFeedforwardSignValue.UseClosedLoopSign
 * @see StaticFeedforwardSignValue.UseVelocitySign
 */
fun TalonFXConfiguration.s(s: Double, staticFeedforwardSign: StaticFeedforwardSignValue): TalonFXConfiguration {
    this.Slot0.apply {
        kS = s
        StaticFeedforwardSign = staticFeedforwardSign
    }
    return this
}

/**
 * Set the velocity feedforward gain.
 *
 * @see TalonFXConfiguration.Slot0
 */
fun TalonFXConfiguration.v(v: Double): TalonFXConfiguration {
    this.Slot0.kV = v
    return this
}

/**
 * Set the acceleration feedforward gain.
 *
 * @see TalonFXConfiguration.Slot0
 */
fun TalonFXConfiguration.a(a: Double): TalonFXConfiguration {
    this.Slot0.kA = a
    return this
}

/**
 * Set the gravity feedforward/feedback gain.
 *
 * @see GravityTypeValue.Elevator_Static
 * @see GravityTypeValue.Arm_Cosine
 */
fun TalonFXConfiguration.g(g: Double, gravityType: GravityTypeValue): TalonFXConfiguration {
    this.Slot0.apply {
        kG = g
        GravityType = gravityType
    }
    return this
}

/**
 * Configure the motion magic cruse velocity, acceleration, and optional jerk.
 *
 * @see TalonFXConfiguration.MotionMagic
 */
fun TalonFXConfiguration.motionMagic(cruseVelocity: Double, acceleration: Double, jerk: Double? = null): TalonFXConfiguration {
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
 * @see TalonFXConfiguration.MotionMagic
 * @see MotionMagicConfigs.MotionMagicExpo_kV
 * @see MotionMagicConfigs.MotionMagicExpo_kA
 * @see MotionMagicConfigs.MotionMagicCruiseVelocity
 */
fun TalonFXConfiguration.motionMagicExpo(expoKV: Double, expoKA: Double, maxVelocity: Double? = null): TalonFXConfiguration {
    this.MotionMagic.apply {
        MotionMagicExpo_kV = expoKV
        MotionMagicExpo_kA = expoKA
        if (maxVelocity != null) MotionMagicCruiseVelocity = maxVelocity
    }
    return this
}

/**
 * Applies a factory default configuration to the [TalonFX].
 *
 * @param modifications optionally provide a block to modify the configuration before it gets sent to the motor.
 *
 * @see modifyConfiguration
 */
fun TalonFX.applyConfiguration(modifications: TalonFXConfiguration.() -> Unit = {}) {
    // Create a factory default configuration, apply modifications, then apply to the motor.
    this.configurator.apply(TalonFXConfiguration().apply { modifications() })
}

/**
 * Modifies the configuration currently on the motor.
 *
 * @param overrides provide a block to modify the configuration before it gets sent to the device.
 *
 * @see applyConfiguration
 */
fun TalonFX.modifyConfiguration(overrides: TalonFXConfiguration.() -> Unit) {
    val oldConfiguration = TalonFXConfiguration()
    this.configurator.refresh(oldConfiguration) // Get motor configuration parameters
    this.configurator.apply(oldConfiguration.apply(overrides)) // Apply overrides to the config and send config to motor.
}