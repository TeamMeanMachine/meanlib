package org.team2471.frc.lib.actuators

import com.ctre.phoenix6.configs.*
import com.ctre.phoenix6.controls.*
import com.ctre.phoenix6.hardware.TalonFX
import com.ctre.phoenix6.signals.*
import org.team2471.frc.lib.math.DoubleRange

class TalonFXWrapper(val deviceID: Int, canBus: String = "") : MotorControllerIO {
    private val _motorController = TalonFX(deviceID, canBus)
    private var config: TalonFXConfiguration = TalonFXConfiguration()
    private var appliedConfig: TalonFXConfiguration = TalonFXConfiguration()
    private var inputs: MotorControllerIO.MotorControllerIOInputs = MotorControllerIO.MotorControllerIOInputs("null")

    private val timeoutSec = 0.050

//    private val configUnsaved: Boolean get() = config.serialize() != appliedConfig.serialize()
    private var configUnsaved: Boolean = true

    override val outputPercent: Double
        get() = inputs.outputPercent

    override fun updateInputs(inputs: MotorControllerIO.MotorControllerIOInputs) {
        inputs.position = _motorController.position.value
        inputs.outputPercent = _motorController.dutyCycle.value
        inputs.velocity = _motorController.velocity.value
        inputs.current = _motorController.statorCurrent.value

        this.inputs = inputs
        applyConfigIfChanged()
    }

    override val current: Double
        get() = inputs.current


    init {
        println("TalonFX motor ID: $deviceID  canBus: $canBus isPro: ${_motorController.isProLicensed}")
//        _motorController.configurator.refresh(config)
        applyConfig()
    }

    override fun brakeMode() {
        config.MotorOutput.NeutralMode = NeutralModeValue.Brake
        configUnsaved = true
    }

    override fun closedLoopRamp(secondsToFull: Double) {
        config.ClosedLoopRamps.DutyCycleClosedLoopRampPeriod = secondsToFull
        config.ClosedLoopRamps.VoltageClosedLoopRampPeriod = secondsToFull
        config.ClosedLoopRamps.TorqueClosedLoopRampPeriod = secondsToFull
        configUnsaved = true
    }

    override fun coastMode() {
        config.MotorOutput.NeutralMode = NeutralModeValue.Coast
        configUnsaved = true
    }

    override fun config_kP(p: Double, simP: Double?) {
        config.Slot0.kP = p
        configUnsaved = true
    }

    override fun config_kD(d: Double, simD: Double?) {
        config.Slot0.kD = d
        configUnsaved = true
    }

    override fun config_kI(i: Double, simI: Double?) {
        config.Slot0.kI = i
        configUnsaved = true
    }

    override fun config_kF(f: Double, simF: Double?) {
        config.Slot0.kS = f
        configUnsaved = true
    }

    override fun currentLimit(continuousLimit: Int, peakLimit: Int, peakDuration: Double) {
        config.CurrentLimits.apply {
            SupplyCurrentLimit = continuousLimit.toDouble()
            StatorCurrentLimit = peakLimit.toDouble()
            SupplyTimeThreshold = peakDuration
            StatorCurrentLimitEnable = true
            SupplyCurrentLimitEnable = true
        }
        configUnsaved = true
    }

    override fun encoderContinuous(continuous: Boolean) {
        config.ClosedLoopGeneral.ContinuousWrap = continuous
        configUnsaved = true
    }

    override fun follow(followerID: MotorControllerIO) {
        _motorController.setControl(StrictFollower((followerID as TalonFXWrapper).deviceID))
    }

    override fun fuseCANCoder(encoderID: Int, motorToSensorRatio: Double, sensorToMechanismRatio: Double) {
        config.Feedback.FeedbackSensorSource = FeedbackSensorSourceValue.FusedCANcoder
        config.Feedback.FeedbackRemoteSensorID = encoderID
        config.Feedback.RotorToSensorRatio = motorToSensorRatio
        config.Feedback.SensorToMechanismRatio = sensorToMechanismRatio
        configUnsaved = true
    }

    override fun getClosedLoopError(): Double = _motorController.closedLoopError.value

    override fun getPValue(): Double = config.Slot0.kP
    override fun getDValue(): Double = config.Slot0.kD
    override fun getIValue(): Double = config.Slot0.kI
    override fun getFValue(): Double = config.Slot0.kS

    override fun getInverted(): Boolean = config.MotorOutput.Inverted == InvertedValue.CounterClockwise_Positive

    override fun getSelectedSensorPosition(): Double  = _motorController.position.value

    override fun getSelectedSensorVelocity(): Double = _motorController.velocity.value

    override fun getSelectedSensorAcceleration(): Double = _motorController.acceleration.value

    override fun motionMagic(acceleration: Double, cruisingVelocity: Double) {
        config.MotionMagic.MotionMagicAcceleration = acceleration / 10.0
        config.MotionMagic.MotionMagicCruiseVelocity = cruisingVelocity / 10.0
        configUnsaved = true
    }

    override fun openLoopRamp(secondsToFull: Double) {
        config.OpenLoopRamps.DutyCycleOpenLoopRampPeriod = secondsToFull
        config.OpenLoopRamps.VoltageOpenLoopRampPeriod = secondsToFull
        config.OpenLoopRamps.TorqueOpenLoopRampPeriod = secondsToFull
        configUnsaved = true
    }

    override fun peakOutputRange(range: DoubleRange) {
        config.MotorOutput.PeakForwardDutyCycle = range.start
        config.MotorOutput.PeakReverseDutyCycle = range.endInclusive
        configUnsaved = true
    }

    override fun restoreFactoryDefaults() {
        config = TalonFXConfiguration()
        configUnsaved = true
    }

    override fun setInverted(invert: Boolean) {
        config.MotorOutput.Inverted =
            if (invert) InvertedValue.CounterClockwise_Positive
            else InvertedValue.Clockwise_Positive
        configUnsaved = true
    }

    override fun setMotionMagicSetpoint(position: Double) {
        _motorController.setControl(MotionMagicDutyCycle(position))
    }

    override fun setMotionMagicSetpoint(position: Double, feedForward: Double) {
        _motorController.setControl(
            MotionMagicDutyCycle(position).withFeedForward(feedForward)
        )
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

    override fun setTorqueCurrent(current: Double) {
        _motorController.setControl(
            TorqueCurrentFOC(current)
        )
    }

    override fun stop() {
        _motorController.setControl(NeutralOut())
    }

    fun applyConfigIfChanged() = if (configUnsaved) applyConfig() else {}

    /*
    apply() is a blocking API call that waits on the device to respond.
    Calling apply() periodically may slow down the execution time of the periodic function,
    as it will always wait up to defaultTimeoutSeconds for the response
    when no timeout parameter is specified.
     */
    override fun applyConfig() {
        configUnsaved = false
        val newConfig = config
        _motorController.configurator.apply(newConfig, timeoutSec)
        appliedConfig = newConfig
    }


}