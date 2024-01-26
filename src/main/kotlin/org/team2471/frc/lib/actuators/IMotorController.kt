package org.team2471.frc.lib.actuators

import com.ctre.phoenix6.signals.NeutralModeValue
import org.team2471.frc.lib.math.DoubleRange
import org.team2471.frc.lib.units.Angle

interface IMotorController {
    val deviceID: Int
    val motorOutputPercent: Double
    val current: Double
    var feedbackCoefficient: Double
    var timeoutMs: Int
    var rawOffset: Int

    fun brakeMode()
    fun burnFlash()
    fun closedLoopRamp(secondsToFull: Double)
    fun coastMode()
    fun config_kP(p: Double)
    fun config_kD(d: Double)
    fun config_kI(i: Double)
    fun currentLimit(continuousLimit: Int, peakLimit: Int, peakDuration: Int)
    fun encoderContinuous(continuous: Boolean)
    fun follow(followerID: IMotorController)
    fun getClosedLoopError(pidIdx: Int): Double
    fun getDValue() : Double
    fun getInverted(): Boolean
    fun getSelectedSensorPosition(pidIdx: Int): Double
    fun getSelectedSensorVelocity(pidIdx: Int): Double
    fun motionMagic(acceleration: Double, cruisingVelocity: Double)
    fun openLoopRamp(secondsToFull: Double)
    fun peakOutputRange(range: DoubleRange)
    fun restoreFactoryDefaults()
    fun setAngle(setPoint : Angle)
    fun setInverted(invert: Boolean)
    fun setMotionMagicSetpoint(position: Double)
    fun setMotionMagicSetpoint(position: Double, feedForward: Double)
    fun setNeutralMode(neutralMode: NeutralModeValue?)
    fun setPercentOutput(percent: Double)
    fun setPositionSetpoint(position: Double)
    fun setPositionSetpoint(position: Double, feedForward: Double)
    fun setSelectedSensorPosition(sensorPos: Double, pidIdx: Int)
    fun setStatusFramePeriod(periodMs: Int, timeoutMs: Int = 100)
    fun setVelocitySetpoint(velocity: Double)
    fun setVelocitySetpoint(velocity: Double, feedForward: Double)
    fun stop()
}