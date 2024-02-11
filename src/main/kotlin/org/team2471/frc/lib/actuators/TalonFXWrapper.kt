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
    private val _motorController = TalonFX(deviceID, canBus)
    private var config: TalonFXConfiguration = TalonFXConfiguration()

//    override var feedbackCoefficient = 1.0
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
        config.Slot0.kI = i * 1024.0
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

    override fun getClosedLoopError(): Double =
        _motorController.closedLoopError.value

    override fun getDValue(): Double {
        println("getDValue not supported by TalonFX")
        return 0.0
    }

    override fun getInverted(): Boolean = config.MotorOutput.Inverted == InvertedValue.CounterClockwise_Positive

    override fun getSelectedSensorPosition(): Double  = _motorController.position.value

    override fun getSelectedSensorVelocity(): Double = _motorController.velocity.value * 600.0 //ms to min

    override fun motionMagic(acceleration: Double, cruisingVelocity: Double) {
        config.MotionMagic.MotionMagicAcceleration = acceleration / 10.0
        config.MotionMagic.MotionMagicCruiseVelocity = cruisingVelocity / 10.0
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
        _motorController.setControl(PositionDutyCycle((setPoint - getSelectedSensorPosition().degrees).wrap().asDegrees))
    }

    override fun setInverted(invert: Boolean) {
        config.MotorOutput.Inverted =
            if (invert) InvertedValue.CounterClockwise_Positive
            else InvertedValue.Clockwise_Positive
        applyConfig()
    }

    override fun setMotionMagicSetpoint(position: Double) {
        println("magicSetpoint = " + (position - rawOffset) + " rawPosition: $rawPosition position: ${position.toInt()} feedbackCoefficient: /*feedbackCoefficient.toInt()*/ rawOffset: $rawOffset")
        _motorController.setControl(MotionMagicDutyCycle(position - rawOffset))
    }

    override fun setMotionMagicSetpoint(position: Double, feedForward: Double) {
        _motorController.setControl(
            MotionMagicDutyCycle((position) - rawOffset).withFeedForward(feedForward)
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
        _motorController.setControl(PositionDutyCycle(position).withSlot(0))
    }

    override fun setPositionSetpoint(position: Double, feedForward: Double) {
        _motorController.setControl(PositionDutyCycle(position).withFeedForward(feedForward).withSlot(0))
    }

    override fun setSelectedSensorPosition(sensorPos: Double) {
        _motorController.setPosition(sensorPos)
    }

    override fun setStatusFramePeriod(periodMs: Int, timeoutSec: Double) {
        _motorController.position.setUpdateFrequency(periodMs.toDouble(), timeoutSec)
    }

    override fun setVelocitySetpoint(velocity: Double) {
        _motorController.setControl(VelocityDutyCycle(velocity / 10.0).withSlot(0))
    }

    override fun setVelocitySetpoint(velocity: Double, feedForward: Double) {
        _motorController.setControl(
            VelocityDutyCycle(velocity).withFeedForward(feedForward).withSlot(0)
        )
    }

    override fun stop() {
        _motorController.setControl(NeutralOut())
    }

    private fun applyConfig(newConfig: TalonFXConfiguration = config) {
        _motorController.configurator.apply(newConfig, timeoutSec)
    }


}