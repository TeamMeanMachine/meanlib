package org.team2471.frc.lib.hardware.rev

import com.revrobotics.PersistMode
import com.revrobotics.ResetMode
import com.revrobotics.encoder.DetachedEncoder
import com.revrobotics.spark.ClosedLoopSlot
import com.revrobotics.spark.FeedbackSensor
import com.revrobotics.spark.SparkBase
import com.revrobotics.spark.SparkFlex
import com.revrobotics.spark.SparkMax
import com.revrobotics.spark.config.MAXMotionConfig
import com.revrobotics.spark.config.SparkBaseConfig
import com.revrobotics.spark.config.SparkMaxConfig

/**
 * Applies a factory default configuration to a [SparkBase] motor like [SparkMax] or [SparkFlex].
 *
 * @param modifications optionally provide a block to modify the configuration before it gets sent to the motor.
 *
 */
fun SparkBase.applyConfiguration(modifications: SparkBaseConfig.() -> Unit = {}) {
    // Create a factory default configuration, apply modifications, then apply to the motor.
    this.configureAsync(SparkMaxConfig().apply { modifications() }, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters)
}

/**
 * Modifies the configuration currently on the motor.
 *
 * @param overrides provide a block to modify the configuration before it gets sent to the device.
 *
 * @see com.revrobotics.ResetMode.kNoResetSafeParameters
 */
fun SparkBase.modifyConfiguration(overrides: SparkBaseConfig.() -> Unit) {
    // Create a factory default configuration, apply modifications, then apply to the motor with kNoResetSafeParameters.
    this.configureAsync(SparkMaxConfig().apply { overrides() }, ResetMode.kNoResetSafeParameters, PersistMode.kPersistParameters)
}

/**
 * Add a follower to the main motor. Does NOT apply the master's configuration.
 *
 * If you need to change follower configurations, create another [SparkMax] or [SparkFlex] motor object and apply the configuration to it
 *
 * @param follower a [SparkBase] follower motor.
 * @param invert Set the follower to output opposite of the leader.
 */
fun SparkBase.addFollower(follower: SparkBase, invert: Boolean = false) {
    val leader = this
    follower.modifyConfiguration { follow(leader.deviceId, invert) }
}
/**
 * Add a follower to the main motor. Does NOT apply the master's configuration.
 *
 * If you need to change follower configurations, create another [SparkMax] or [SparkFlex] motor object and apply the configuration to it
 *
 * @param followerID The CAN ID of a [SparkMax] follower motor.
 * @param invert Set the follower to output opposite of the leader.
 */
fun SparkMax.addFollower(followerID: Int, invert: Boolean = false) = this.addFollower(SparkMax(this.busId, followerID, this.motorType), invert)
/**
 * Add a follower to the main motor. Does NOT apply the master's configuration.
 *
 * If you need to change follower configurations, create another [SparkMax] or [SparkFlex] motor object and apply the configuration to it
 *
 * @param followerID The CAN ID of a [SparkFlex] follower motor.
 * @param invert Set the follower to output opposite of the leader.
 */
fun SparkFlex.addFollower(followerID: Int, invert: Boolean = false) = this.addFollower(SparkFlex(this.busId, followerID, this.motorType), invert)

/**
 * Set motor neutral mode to brake.
 */
fun SparkBase.brakeMode() = modifyConfiguration { brakeMode() }
/**
 * Set motor neutral mode to brake.
 */
fun SparkBase.coastMode() = modifyConfiguration { coastMode() }

/**
 * Set motor neutral mode to brake.
 */
fun SparkBaseConfig.brakeMode(): SparkBaseConfig {
    idleMode(SparkBaseConfig.IdleMode.kBrake)
    return this
}

/**
 * Set motor neutral mode to brake.
 */
fun SparkBaseConfig.coastMode(): SparkBaseConfig {
    idleMode(SparkBaseConfig.IdleMode.kCoast)
    return this
}

/**
 * Set the proportional, integral, and derivative gains.
 *
 * @param p The proportional gain
 * @param i The integral gain
 * @param d The derivative gain
 * @param slotNumber The slot number to set the gains for. Defaults to 0.
 */
fun SparkBaseConfig.pid(p: Double, i: Double, d: Double, slotNumber: Int = 0): SparkBaseConfig {
    this.closedLoop.pid(p, i, d, slotFromInt(slotNumber))
    return this
}

/**
 * Set the proportional gain.
 *
 * @param p The proportional gain
 * @param slotNumber The slot number to set the gains for. Defaults to 0.
 */
fun SparkBaseConfig.p(p: Double, slotNumber: Int = 0): SparkBaseConfig {
    this.closedLoop.p(p, slotFromInt(slotNumber))
    return this
}

/**
 * Set the derivative gain.
 *
 * @param d The derivative gain
 * @param slotNumber The slot number to set the gains for. Defaults to 0.
 */
fun SparkBaseConfig.d(d: Double, slotNumber: Int = 0): SparkBaseConfig {
    this.closedLoop.d(d, slotFromInt(slotNumber))
    return this
}

/**
 * Set the integral gain.
 *
 * @param i The integral gain
 * @param slotNumber The slot number to set the gains for. Defaults to 0.
 */
fun SparkBaseConfig.i(i: Double, slotNumber: Int = 0): SparkBaseConfig {
    this.closedLoop.i(i, slotFromInt(slotNumber))
    return this
}

/**
 * Set the acceleration feedforward gain.
 *
 * @param a The acceleration feedforward gain
 * @param slotNumber The slot number to set the gains for. Defaults to 0.
 */
fun SparkBaseConfig.a(a: Double, slotNumber: Int = 0): SparkBaseConfig {
    this.closedLoop.feedForward.kA(a, slotFromInt(slotNumber))
    return this
}

/**
 * Set the gravity feedforward gain.
 *
 * This is statically applied, for an elevator or linear mechanism.
 * For an arm or rotary mechanism, use kCos().
 * Set this to 0 if kCos is being used.
 *
 * @param g The gravity feedforward gain
 * @param slotNumber The slot number to set the gains for. Defaults to 0.
 *
 * @see SparkBaseConfig.kCos
 */
fun SparkBaseConfig.g(g: Double, slotNumber: Int = 0): SparkBaseConfig {
    this.closedLoop.feedForward.kG(g, slotFromInt(slotNumber))
    return this
}


/**
 * Set the velocity feedforward gain.
 *
 * @param v The velocity feedforward gain
 * @param slotNumber The slot number to set the gains for. Defaults to 0.
 */
fun SparkBaseConfig.v(v: Double, slotNumber: Int = 0): SparkBaseConfig {
    this.closedLoop.feedForward.kV(v, slotFromInt(slotNumber))
    return this
}

/**
 * Set the static feedforward gain.
 *
 * @param s The static feedforward gain
 * @param slotNumber The slot number to set the gains for. Defaults to 0.
 */
fun SparkBaseConfig.s(s: Double, slotNumber: Int = 0): SparkBaseConfig {
    this.closedLoop.feedForward.kS(s, slotFromInt(slotNumber))
    return this
}

/**
 * Set the kCos cosine gravity feedforward gain.
 *
 * This is multiplied by the cosine of the absolute position of the mechanism.
 *
 * Refer to REVLib docs if confused.
 *
 * @param kCos The kCos gain in Volts
 * @param kCosRatio This ratio should convert from the units of your setpoint to absolute rotations of your mechanism.
 * @param slotNumber The slot number to set the gains for. Defaults to 0.
 *
 */
fun SparkBaseConfig.kCos(kCos: Double, kCosRatio: Double = 1.0, slotNumber: Int = 0): SparkBaseConfig {
    this.closedLoop.feedForward.kCos(kCos, slotFromInt(slotNumber))
    this.closedLoop.feedForward.kCosRatio(kCosRatio, slotFromInt(slotNumber))
    return this
}

/**
 * Configure the max motion cruise velocity and acceleration.
 */
fun SparkBaseConfig.maxMotion(cruseVelocity: Double, maxAcceleration: Double, slotNumber: Int = 0): SparkBaseConfig {
    this.closedLoop.maxMotion.cruiseVelocity(cruseVelocity, slotFromInt(slotNumber))
    this.closedLoop.maxMotion.maxAcceleration(maxAcceleration, slotFromInt(slotNumber))
    return this
}

/**
 * Configure the max motion cruise velocity.
 */
fun SparkBaseConfig.maxMotionCruiseVelocity(cruiseVelocity: Double, slotNumber: Int = 0): SparkBaseConfig {
    this.closedLoop.maxMotion.cruiseVelocity(cruiseVelocity, slotFromInt(slotNumber))
    return this
}

/**
 * Configure the max motion acceleration.
 */
fun SparkBaseConfig.maxMotionMaxAcceleration(maxAcceleration: Double, slotNumber: Int = 0): SparkBaseConfig {
    this.closedLoop.maxMotion.maxAcceleration(maxAcceleration, slotFromInt(slotNumber))
    return this
}

/**
 * Set the MAXMotion position control mode of the controller.
 *
 * @see MAXMotionConfig.MAXMotionPositionMode
 */
fun SparkBaseConfig.maxMotionPositionMode(positionMode: MAXMotionConfig.MAXMotionPositionMode, slotNumber: Int = 0): SparkBaseConfig {
    this.closedLoop.maxMotion.positionMode(positionMode, slotFromInt(slotNumber))
    return this
}

/**
 * Set the allowed profile error for the MAXMotion mode of the controller.
 * This value is how much deviation from the profile is tolerated before the profile is regenerated.
 *
 * @see MAXMotionConfig.MAXMotionPositionMode
 */
fun SparkBaseConfig.maxMotionAllowedProfileError(allowedError: Double, slotNumber: Int = 0): SparkBaseConfig {
    this.closedLoop.maxMotion.allowedProfileError(allowedError, slotFromInt(slotNumber))
    return this
}

/**
 * Set the allowed closed loop error for the controller.
 * This value is how much deviation from the setpoint is tolerated and is useful in preventing oscillation around the setpoint.
 *
 * @see MAXMotionConfig.MAXMotionPositionMode
 */
fun SparkBaseConfig.allowedClosedLoopError(allowedError: Double, slotNumber: Int = 0): SparkBaseConfig {
    this.closedLoop.allowedClosedLoopError(allowedError, slotFromInt(slotNumber))
    return this
}

/**
 * Set the derivative filter of the controller for a specific closed loop slot.
 */
fun SparkBaseConfig.dFilter(dFilter: Double, slotNumber: Int = 0): SparkBaseConfig {
    this.closedLoop.dFilter(dFilter, slotFromInt(slotNumber))
    return this
}

/**
 * Set the maximum I accumulator of the controller.
 * This value is used to constrain the I accumulator to help manage integral wind-up.
 */
fun SparkBaseConfig.iMaxAccum(iMaxAccum: Double, slotNumber: Int = 0): SparkBaseConfig {
    this.closedLoop.iMaxAccum(iMaxAccum, slotFromInt(slotNumber))
    return this
}

/**
 * Set the integral zone of the controller.
 */
fun SparkBaseConfig.iZone(iZone: Double, slotNumber: Int = 0): SparkBaseConfig {
    this.closedLoop.iZone(iZone, slotFromInt(slotNumber))
    return this
}

/**
 * Set the maximum output of the controller in the range [-1, 1]
 */
fun SparkBaseConfig.maxOutput(maxOutput: Double, slotNumber: Int = 0): SparkBaseConfig {
    this.closedLoop.maxOutput(maxOutput, slotFromInt(slotNumber))
    return this
}

/**
 * Set the minimum output of the controller in the range [-1, 1]
 */
fun SparkBaseConfig.minOutput(minOutput: Double, slotNumber: Int = 0): SparkBaseConfig {
    this.closedLoop.minOutput(minOutput, slotFromInt(slotNumber))
    return this
}

/**
 * Set the output range of the controller
 *
 * @param minOutput The minimum output of the controller in the range [-1, 1]
 * @param maxOutput The maximum output of the controller in the range [-1, 1]
 */
fun SparkBaseConfig.outputRange(minOutput: Double, maxOutput: Double, slotNumber: Int = 0): SparkBaseConfig {
    this.closedLoop.outputRange(minOutput, maxOutput, slotFromInt(slotNumber))
    return this
}

/**
 * Enable or disable PID wrapping for position closed loop control.
 *
 * @param enabled Whether to wrap. Defaults to false
 *
 * @see positionWrappingInputRange
 */
fun SparkBaseConfig.positionWrappingEnabled(enabled: Boolean): SparkBaseConfig {
    this.closedLoop.positionWrappingEnabled(enabled)
    return this
}

/**
 * Set the input range for PID wrapping with position closed loop control.
 *
 * @param minInput The value of min input for the position
 * @param minInput The value of min input for the position
 *
 * @see positionWrappingEnabled
 */
fun SparkBaseConfig.positionWrappingInputRange(minInput: Double, maxInput: Double): SparkBaseConfig {
    this.closedLoop.positionWrappingInputRange(minInput, maxInput)
    this.closedLoop.positionWrappingEnabled(true)
    return this
}

/**
 * Sets the configs that affect the feedback sensor of this motor. Aka: What it will think its own position/velocity is.
 * Useful for eliminating control error between the motor and the mechanism.
 *
 * @param sensor the type of feedback device.
 */
fun SparkBaseConfig.alternateFeedbackSensor(sensor: FeedbackSensor): SparkBaseConfig {
    this.closedLoop.feedbackSensor(sensor)
    return this
}

/**
 * Sets the configs that affect the feedback sensor of this motor. Aka: What it will think its own position/velocity is.
 * Useful for eliminating control error between the motor and the mechanism.
 *
 * @param sensor the type of feedback device.
 * @param detachedEncoderDeviceId the CAN ID of the feedback device on the same CAN bus as the motor.
 */
fun SparkBaseConfig.alternateFeedbackSensor(sensor: FeedbackSensor, detachedEncoderDeviceId: Int): SparkBaseConfig {
    this.closedLoop.feedbackSensor(sensor, detachedEncoderDeviceId)
    return this
}

/**
 * Sets the configs that affect the feedback sensor of this motor. Aka: What it will think its own position/velocity is.
 * Useful for eliminating control error between the motor and the mechanism.
 *
 * @param sensor the type of feedback device.
 * @param detachedEncoder the device on the same CAN bus as the motor.
 */
fun SparkBaseConfig.alternateFeedbackSensor(sensor: FeedbackSensor, detachedEncoder: DetachedEncoder): SparkBaseConfig {
    this.closedLoop.feedbackSensor(sensor, detachedEncoder)
    return this
}

/**
 * Set the forward soft limit based on the position of the selected feedback sensor.
 * This will disable motor actuation in the forward direction past this position.
 * This value should have the position conversion factor applied to it.
 */
fun SparkBaseConfig.forwardSoftLimit(limit: Double): SparkBaseConfig {
    this.softLimit.forwardSoftLimit(limit)
    this.softLimit.forwardSoftLimitEnabled(true)
    return this
}

/**
 * Set the reverse soft limit based on the position of the selected feedback sensor.
 * This will disable motor actuation in the reverse direction past this position.
 * This value should have the position conversion factor applied to it.
 */
fun SparkBaseConfig.reverseSoftLimit(limit: Double): SparkBaseConfig {
    this.softLimit.reverseSoftLimit(limit)
    this.softLimit.reverseSoftLimitEnabled(true)
    return this
}

/**
 * Set the conversion factor for the position of the encoder.
 * Position is returned in native units of rotations and will be multiplied by this conversion factor.
 *
 * @param factor The conversion factor to multiply the native units by
 */
fun SparkBaseConfig.positionConversionFactor(factor: Double): SparkBaseConfig {
    this.encoder.positionConversionFactor(factor)
    return this
}

/**
 * Set the conversion factor for the velocity of the encoder.
 * Velocity is returned in native units of rotations per minute and will be multiplied by this conversion factor.
 *
 * @param factor The conversion factor to multiply the native units by
 */
fun SparkBaseConfig.velocityConversionFactor(factor: Double): SparkBaseConfig {
    this.encoder.velocityConversionFactor(factor)
    return this
}

private fun slotFromInt(slotNumber: Int): ClosedLoopSlot {
    return when (slotNumber) {
        1 -> ClosedLoopSlot.kSlot1
        2 -> ClosedLoopSlot.kSlot2
        3 -> ClosedLoopSlot.kSlot3
        else -> ClosedLoopSlot.kSlot0
    }
}
