package org.team2471.frc.lib.actuators

import com.ctre.phoenix6.configs.*
import com.ctre.phoenix6.controls.*
import com.ctre.phoenix6.hardware.TalonFX
import com.ctre.phoenix6.signals.InvertedValue
import com.ctre.phoenix6.signals.NeutralModeValue
import org.team2471.frc.lib.math.DoubleRange
import org.team2471.frc.lib.units.Angle
import org.team2471.frc.lib.units.degrees

class TalonFXWrapper(override val deviceID: Int, canBus: String? = null) : IMotorController {
    private val _motorController = TalonFX(deviceID, canBus).apply { restoreFactoryDefaults() }

    override var feedbackCoefficient = 1.0
    override var timeoutMs = 100
    override var rawOffset = 0

    val rawPosition: Double
        get() = _motorController.position.value
    override val motorOutputPercent: Double
        get() = _motorController.dutyCycle.value
    override val current: Double
        get() = _motorController.statorCurrent.value

    override fun brakeMode() {
        _motorController.configurator.apply(MotorOutputConfigs().withNeutralMode(NeutralModeValue.Brake))
    }

    override fun burnFlash() {
        println("no")
    }

    override fun closedLoopRamp(secondsToFull: Double) {
        _motorController.configurator.apply(ClosedLoopRampsConfigs().withDutyCycleClosedLoopRampPeriod(secondsToFull)) //untested
    }

    override fun coastMode() {
        _motorController.configurator.apply(MotorOutputConfigs().withNeutralMode(NeutralModeValue.Coast))
    }

    override fun config_kP(p: Double) {
        _motorController.configurator.apply(Slot0Configs().withKP(p / feedbackCoefficient * 1024.0), timeoutMs * 1000.0)
    }

    override fun config_kD(d: Double) {
        _motorController.configurator.apply(Slot0Configs().withKP(d / feedbackCoefficient * 1024.0), timeoutMs * 1000.0)
    }

    override fun config_kI(i: Double) {
        _motorController.configurator.apply(Slot0Configs().withKI(i / feedbackCoefficient * 1024.0), timeoutMs * 1000.0)
    }

    override fun currentLimit(continuousLimit: Int, peakLimit: Int, peakDuration: Int) {
        _motorController.configurator.apply(CurrentLimitsConfigs().withSupplyCurrentLimit(continuousLimit.toDouble()).withStatorCurrentLimit(peakLimit.toDouble()).withSupplyTimeThreshold(peakDuration.toDouble()).withStatorCurrentLimitEnable(true).withSupplyCurrentLimitEnable(true))

    }

    override fun encoderContinuous(continuous: Boolean) {
        val config = ClosedLoopGeneralConfigs()
        config.ContinuousWrap = continuous
        _motorController.configurator.apply(config)
    }

    override fun follow(followerID: IMotorController) {
        (followerID as TalonFX).setControl(StrictFollower(_motorController.deviceID)) //untested
    }

    override fun getClosedLoopError(pidIdx: Int): Double =
        _motorController.closedLoopError.value * feedbackCoefficient

    override fun getDValue(): Double {
        println("no")
        return 0.0
    }

    override fun getInverted(): Boolean { //untested
        val config = MotorOutputConfigs()
        _motorController.configurator.refresh(config)
        return config.Inverted == InvertedValue.Clockwise_Positive
    }

    override fun getSelectedSensorPosition(pidIdx: Int): Double  = _motorController.position.value

    override fun getSelectedSensorVelocity(pidIdx: Int): Double = _motorController.velocity.value

    override fun motionMagic(acceleration: Double, cruisingVelocity: Double) {
        val srxAcceleration = (acceleration / feedbackCoefficient / 10.0)
        val srxCruisingVelocity = (cruisingVelocity / feedbackCoefficient / 10.0)
        _motorController.configurator.apply(MotionMagicConfigs().withMotionMagicAcceleration(srxAcceleration), 0.020)
        _motorController.configurator.apply(MotionMagicConfigs().withMotionMagicCruiseVelocity(srxCruisingVelocity), 0.020)
    }

    override fun openLoopRamp(secondsToFull: Double) {
        _motorController.configurator.apply(OpenLoopRampsConfigs().withDutyCycleOpenLoopRampPeriod(secondsToFull), timeoutMs * 1000.0) //untested
    }

    override fun peakOutputRange(range: DoubleRange) {
        _motorController.configurator.apply(MotorOutputConfigs().withPeakForwardDutyCycle(range.start), timeoutMs * 1000.0) //untested
        _motorController.configurator.apply(MotorOutputConfigs().withPeakReverseDutyCycle(range.endInclusive), timeoutMs * 1000.0)
    }

    override fun restoreFactoryDefaults() {
        println("no")
    }

    override fun setAngle(setPoint: Angle) {
        _motorController.setControl(PositionDutyCycle((setPoint - getSelectedSensorPosition(0).degrees).wrap().asDegrees))
    }

    override fun setInverted(invert: Boolean) {
        if (invert) {
            val config = MotorOutputConfigs()
            _motorController.configurator.refresh(config) //.refresh populates the passed in config??
            config.Inverted = when (config.Inverted) {
                InvertedValue.CounterClockwise_Positive -> InvertedValue.Clockwise_Positive
                InvertedValue.Clockwise_Positive -> InvertedValue.CounterClockwise_Positive
                else -> config.Inverted
            }
            _motorController.configurator.apply(config)
        } else {
            _motorController.configurator.apply(MotorOutputConfigs())
        }
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

    override fun setStatusFramePeriod(periodMs: Int, timeoutMs: Int) {
        _motorController.position.setUpdateFrequency(periodMs.toDouble(), timeoutMs.toDouble())
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


}