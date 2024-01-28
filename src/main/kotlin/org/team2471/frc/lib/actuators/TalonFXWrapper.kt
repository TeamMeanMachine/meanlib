package org.team2471.frc.lib.actuators

import com.ctre.phoenix6.configs.*
import com.ctre.phoenix6.controls.*
import com.ctre.phoenix6.hardware.TalonFX
import com.ctre.phoenix6.signals.InvertedValue
import com.ctre.phoenix6.signals.NeutralModeValue
import org.team2471.frc.lib.math.DoubleRange
import org.team2471.frc.lib.units.Angle
import org.team2471.frc.lib.units.degrees

class TalonFXWrapper(override val deviceID: Int, canBus: String = "") : IMotorController {
    private val _motorController = TalonFX(deviceID, canBus).apply { restoreFactoryDefaults() }
    private lateinit var config: TalonFXConfiguration

    override var feedbackCoefficient = 1.0
    override var timeoutSec = 0.050
    override var rawOffset = 0

    val rawPosition: Double
        get() = _motorController.position.value
    override val motorOutputPercent: Double
        get() = _motorController.dutyCycle.value
    override val current: Double
        get() = _motorController.statorCurrent.value


    init {
        println("Creating TalonFX motor  ID: $deviceID  canBus: $canBus")
        _motorController.configurator.refresh(config)
    }

    override fun brakeMode() {
        config.MotorOutput.NeutralMode = NeutralModeValue.Brake
        applyConfig()
    }

    override fun burnFlash() {
        println("burnFlash not supported by TalonFX")
    }

    override fun closedLoopRamp(secondsToFull: Double) {
        config.ClosedLoopRamps.DutyCycleClosedLoopRampPeriod = secondsToFull
        config.ClosedLoopRamps.VoltageClosedLoopRampPeriod = secondsToFull
        config.ClosedLoopRamps.TorqueClosedLoopRampPeriod = secondsToFull
        applyConfig()
    }

    override fun coastMode() {
        config.MotorOutput.NeutralMode = NeutralModeValue.Coast
        applyConfig()
    }

    override fun config_kP(p: Double) {
        config.Slot0.kP = p
        applyConfig()
    }

    override fun config_kD(d: Double) {
        config.Slot0.kD = d
        applyConfig()
    }

    override fun config_kI(i: Double) {
        config.Slot0.kI = i / feedbackCoefficient * 1024.0
        applyConfig()
    }

    override fun currentLimit(continuousLimit: Int, peakLimit: Int, peakDuration: Int) {
        config.CurrentLimits.apply {
            SupplyCurrentLimit = continuousLimit.toDouble()
            StatorCurrentLimit = peakLimit.toDouble()
            SupplyTimeThreshold = peakDuration.toDouble()
            StatorCurrentLimitEnable = true
            SupplyCurrentLimitEnable = true
        }
        applyConfig()
    }

    override fun encoderContinuous(continuous: Boolean) {
        config.ClosedLoopGeneral.ContinuousWrap = continuous
        applyConfig()
    }

    override fun follow(followerID: IMotorController) {
        _motorController.setControl(StrictFollower(followerID.deviceID))
    }

    override fun getClosedLoopError(pidIdx: Int): Double =
        _motorController.closedLoopError.value * feedbackCoefficient

    override fun getDValue(): Double {
        println("getDValue not supported by TalonFX")
        return 0.0
    }

    override fun getInverted(): Boolean = config.MotorOutput.Inverted == InvertedValue.CounterClockwise_Positive

    override fun getSelectedSensorPosition(pidIdx: Int): Double  = _motorController.position.value

    override fun getSelectedSensorVelocity(pidIdx: Int): Double = _motorController.velocity.value

    override fun motionMagic(acceleration: Double, cruisingVelocity: Double) {
        config.MotionMagic.MotionMagicAcceleration = acceleration / feedbackCoefficient / 10.0
        config.MotionMagic.MotionMagicCruiseVelocity = cruisingVelocity / feedbackCoefficient / 10.0
        applyConfig()
    }

    override fun openLoopRamp(secondsToFull: Double) {
        config.OpenLoopRamps.DutyCycleOpenLoopRampPeriod = secondsToFull
        config.OpenLoopRamps.VoltageOpenLoopRampPeriod = secondsToFull
        config.OpenLoopRamps.TorqueOpenLoopRampPeriod = secondsToFull
        applyConfig()
    }

    override fun peakOutputRange(range: DoubleRange) {
        config.MotorOutput.PeakForwardDutyCycle = range.start
        config.MotorOutput.PeakReverseDutyCycle = range.endInclusive
        applyConfig()
    }

    override fun restoreFactoryDefaults() {
        config = TalonFXConfiguration()
        applyConfig(config)
    }

    override fun setAngle(setPoint: Angle) {
        _motorController.setControl(PositionDutyCycle((setPoint - getSelectedSensorPosition(0).degrees).wrap().asDegrees))
    }

    override fun setInverted(invert: Boolean) {
        config.MotorOutput.Inverted =
            if (invert) InvertedValue.CounterClockwise_Positive
            else InvertedValue.Clockwise_Positive
        applyConfig()
    }

    override fun setMotionMagicSetpoint(position: Double) {
        println("magicSetpoint = " + (position / feedbackCoefficient - rawOffset) + " rawPosition: $rawPosition position: ${position.toInt()} feedbackCoefficient: $feedbackCoefficient.toInt() rawOffset: $rawOffset")
        _motorController.setControl(MotionMagicDutyCycle(position / feedbackCoefficient - rawOffset))
    }

    override fun setMotionMagicSetpoint(position: Double, feedForward: Double) {
        _motorController.setControl(
            MotionMagicDutyCycle((position / feedbackCoefficient) - rawOffset).withFeedForward(feedForward)
        )
    }

    override fun setNeutralMode(neutralMode: NeutralModeValue?) {
        when (neutralMode) {
            NeutralModeValue.Brake -> brakeMode()
            NeutralModeValue.Coast -> coastMode()
            else -> {}
        }
    }

    override fun setPercentOutput(percent: Double) {
        _motorController.setControl(DutyCycleOut(percent))
    }

    override fun setPositionSetpoint(position: Double) {
        _motorController.setControl(PositionDutyCycle((position / feedbackCoefficient) - rawOffset))
    }

    override fun setPositionSetpoint(position: Double, feedForward: Double) {
        _motorController.setControl(PositionDutyCycle((position / feedbackCoefficient) - rawOffset).withFeedForward(feedForward))
    }

    override fun setSelectedSensorPosition(sensorPos: Double, pidIdx: Int) {
        _motorController.setPosition(sensorPos)
    }

    override fun setStatusFramePeriod(periodMs: Int, timeoutSec: Double) {
        _motorController.position.setUpdateFrequency(periodMs.toDouble(), timeoutSec)
    }

    override fun setVelocitySetpoint(velocity: Double) {
        _motorController.setControl(VelocityDutyCycle(velocity / feedbackCoefficient / 10.0))
    }

    override fun setVelocitySetpoint(velocity: Double, feedForward: Double) {
        _motorController.setControl(
            VelocityDutyCycle(velocity / feedbackCoefficient / 10.0).withFeedForward(feedForward)
        )
    }

    override fun stop() {
        _motorController.setControl(NeutralOut())
    }

    private fun applyConfig(newConfig: TalonFXConfiguration = config) {
        _motorController.configurator.apply(newConfig, timeoutSec)
    }


}