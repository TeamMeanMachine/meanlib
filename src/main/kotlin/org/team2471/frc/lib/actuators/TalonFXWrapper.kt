package org.team2471.frc.lib.actuators

import com.ctre.phoenix6.configs.*
import com.ctre.phoenix6.controls.*
import com.ctre.phoenix6.hardware.TalonFX
import com.ctre.phoenix6.signals.*
import org.team2471.frc.lib.math.DoubleRange

class TalonFXWrapper(val deviceID: Int, canBus: String = "") : MotorControllerIO {
    private val _motorController = TalonFX(deviceID, canBus)
    private var config: TalonFXConfiguration = TalonFXConfiguration()
    private var inputs: MotorControllerIO.MotorControllerIOInputs = MotorControllerIO.MotorControllerIOInputs("null")

    private val timeoutSec = 0.050

//    private val configUnsaved: Boolean get() = config.serialize() != appliedConfig.serialize()
    private var configUnsaved: Boolean = true

    override val outputPercent: Double
        get() = inputs.outputPercent

    override fun updateInputs(inputs: MotorControllerIO.MotorControllerIOInputs) {
        inputs.position = _motorController.position.valueAsDouble
        inputs.outputPercent = _motorController.dutyCycle.valueAsDouble
        inputs.velocity = _motorController.velocity.valueAsDouble
        inputs.current = _motorController.supplyCurrent.valueAsDouble
        inputs.temp = _motorController.deviceTemp.valueAsDouble

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
        config.Slot0.StaticFeedforwardSign = StaticFeedforwardSignValue.UseClosedLoopSign
        configUnsaved = true
    }

    override fun config_kV(v: Double) {
        config.Slot0.kV = v
        configUnsaved = true
    }

    override fun currentLimit(continuousLimit: Int, peakLimit: Int, peakDuration: Double) {
        config.CurrentLimits.apply {
            SupplyCurrentLowerLimit = continuousLimit.toDouble()
            SupplyCurrentLimit = peakLimit.toDouble()
            SupplyCurrentLowerTime = peakDuration
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

    override fun remoteCANCoder(encoderID: Int, motorToSensorRatio: Double, sensorToMechanismRatio: Double) {
        config.Feedback.FeedbackSensorSource = FeedbackSensorSourceValue.RemoteCANcoder
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

    override fun getInverted(): Boolean = config.MotorOutput.Inverted != InvertedValue.CounterClockwise_Positive

    override fun getSelectedSensorPosition(): Double  = _motorController.position.valueAsDouble

    override fun getSelectedSensorVelocity(): Double = _motorController.velocity.valueAsDouble

    override fun getSelectedSensorAcceleration(): Double = _motorController.acceleration.valueAsDouble

    override fun motionMagic(acceleration: Double, cruisingVelocity: Double) {
        config.MotionMagic.MotionMagicAcceleration = acceleration
        config.MotionMagic.MotionMagicCruiseVelocity = cruisingVelocity
        configUnsaved = true
    }

    override fun motionMagicExpo(acceleration: Double, cruisingVelocityPower: Double ) {
        config.MotionMagic.MotionMagicExpo_kA = acceleration
        config.MotionMagic.MotionMagicExpo_kV = cruisingVelocityPower
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
            if (invert) InvertedValue.Clockwise_Positive
            else InvertedValue.CounterClockwise_Positive
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

    override fun setMotionMagicExpoSetpoint(position: Double) {
        _motorController.setControl(MotionMagicExpoDutyCycle(position))
    }

    override fun setMotionMagicExpoSetpoint(position: Double, feedForward: Double) {
        _motorController.setControl(MotionMagicExpoDutyCycle(position).withFeedForward(feedForward))
    }

    override fun setPercentOutput(percent: Double) {
        _motorController.setControl(DutyCycleOut(percent).withEnableFOC(true))
    }

    override fun setPositionSetpoint(position: Double) {
        _motorController.setControl(PositionDutyCycle(position).withSlot(0))
    }

    override fun setPositionSetpoint(position: Double, feedForward: Double) {
        _motorController.setControl(PositionDutyCycle(position).withFeedForward(feedForward).withSlot(0).withEnableFOC(true))
    }

    override fun setSelectedSensorPosition(sensorPos: Double) {
        _motorController.setPosition(sensorPos)
    }

    override fun setStatusFramePeriod(periodMs: Int, timeoutSec: Double) {
        _motorController.position.setUpdateFrequency(periodMs.toDouble(), timeoutSec)
    }

    override fun setVelocitySetpointVoltage(velocity: Double, feedForward: Double) {
        _motorController.setControl(VelocityVoltage(velocity).withFeedForward(feedForward))
    }

    override fun setVelocitySetpoint(velocity: Double) {
        _motorController.setControl(VelocityDutyCycle(velocity).withSlot(0))
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
        _motorController.configurator.apply(config, timeoutSec)
    }


}