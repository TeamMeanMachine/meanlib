package org.team2471.frc.lib.actuators

import com.ctre.phoenix6.configs.*
import com.ctre.phoenix6.controls.*
import com.ctre.phoenix6.hardware.TalonFX
import com.ctre.phoenix6.signals.InvertedValue
import com.ctre.phoenix6.signals.NeutralModeValue
import org.team2471.frc.lib.math.DoubleRange
import org.team2471.frc.lib.units.*

class TalonFXWrapper(val deviceID: Int, canBus: String = "") : MotorControllerIO {
    private val _motorController = TalonFX(deviceID, canBus)
    private var config: TalonFXConfiguration = TalonFXConfiguration()
    private var inputs: MotorControllerIO.MotorControllerIOInputs = MotorControllerIO.MotorControllerIOInputs("null")

    private val timeoutSec = 0.050

    override val outputPercent: Double
        get() = inputs.outputPercent

    override fun updateInputs(inputs: MotorControllerIO.MotorControllerIOInputs) {
        inputs.position = _motorController.position.value
        inputs.outputPercent = _motorController.dutyCycle.value
        inputs.velocity = _motorController.velocity.value
        inputs.current = _motorController.statorCurrent.value

        this.inputs = inputs
    }

    override val current: Double
        get() = inputs.current


    init {
        println("Creating TalonFX motor  ID: $deviceID  canBus: $canBus")
        _motorController.configurator.refresh(config)
    }

    override fun brakeMode() {
        config.MotorOutput.NeutralMode = NeutralModeValue.Brake
        applyConfig()
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

    override fun config_kP(p: Double, simP: Double?) {
        config.Slot0.kP = p
        applyConfig()
    }

    override fun config_kD(d: Double, simD: Double?) {
        config.Slot0.kD = d
        applyConfig()
    }

    override fun config_kI(i: Double, simI: Double?) {
        config.Slot0.kI = i
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

    override fun follow(followerID: MotorControllerIO) {
        _motorController.setControl(StrictFollower((followerID as TalonFXWrapper).deviceID))
    }

    override fun getClosedLoopError(): Double = _motorController.closedLoopError.value

    override fun getPValue(): Double = config.Slot0.kP

    override fun getDValue(): Double = config.Slot0.kD

    override fun getIValue(): Double = config.Slot0.kI

    override fun getInverted(): Boolean = config.MotorOutput.Inverted == InvertedValue.CounterClockwise_Positive

    override fun getSelectedSensorPosition(): Double  = inputs.position

    override fun getSelectedSensorVelocity(): Double = inputs.velocity

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

    override fun setInverted(invert: Boolean) {
        config.MotorOutput.Inverted =
            if (invert) InvertedValue.CounterClockwise_Positive
            else InvertedValue.Clockwise_Positive
        applyConfig()
    }

    override fun setMotionMagicSetpoint(position: Double) {
        _motorController.setControl(MotionMagicDutyCycle(position))
    }

    override fun setMotionMagicSetpoint(position: Double, feedForward: Double) {
        _motorController.setControl(
            MotionMagicDutyCycle(position).withFeedForward(feedForward)
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
        _motorController.setControl(DutyCycleOut(percent).withEnableFOC(true))
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