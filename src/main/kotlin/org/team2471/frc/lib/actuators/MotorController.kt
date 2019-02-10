package org.team2471.frc.lib.actuators

import com.ctre.phoenix.motorcontrol.ControlMode
import com.ctre.phoenix.motorcontrol.DemandType
import com.ctre.phoenix.motorcontrol.FeedbackDevice
import com.ctre.phoenix.motorcontrol.NeutralMode
import com.ctre.phoenix.motorcontrol.can.BaseMotorController as CTREMotorController
import kotlin.math.roundToInt
import com.ctre.phoenix.motorcontrol.can.TalonSRX as CTRETalonSRX
import com.ctre.phoenix.motorcontrol.can.VictorSPX as CTREVictorSPX

sealed class MotorControllerID

data class TalonID(val value: Int) : MotorControllerID()
data class VictorID(val value: Int) : MotorControllerID()

private fun CTREMotorController(id: MotorControllerID) = when (id) {
    is TalonID -> CTRETalonSRX(id.value)
    is VictorID -> CTREVictorSPX(id.value)
}

class MotorController(deviceId: MotorControllerID, vararg followerIds: MotorControllerID) {
    private val ctreMotorController = CTREMotorController(deviceId)

    private var feedbackCoefficient = 1.0

    private val followers = followerIds.map { id ->
        val follower = CTREMotorController(id)
        follower.follow(ctreMotorController)
        follower
    }.toTypedArray()

    val current: Double
        get() {
            check(ctreMotorController is CTRETalonSRX) { "Current can only be read from talons" }
            return ctreMotorController.outputCurrent
        }

    val velocity: Double
        get() = ctreMotorController.getSelectedSensorVelocity(0) * feedbackCoefficient * 10.0

    val output: Double
        get() = ctreMotorController.motorOutputPercent

    var position: Double
        get() = ctreMotorController.getSelectedSensorPosition(0) * feedbackCoefficient
        set(value) {
            ctreMotorController.selectedSensorPosition = (value / feedbackCoefficient).roundToInt()
        }

    val closedLoopError: Double
        get() = ctreMotorController.closedLoopError * feedbackCoefficient

    init {
        allTalons { it.configFactoryDefault(20) }
        ctreMotorController.selectedSensorPosition = 0
    }

    fun setPercentOutput(percent: Double) = ctreMotorController.set(ControlMode.PercentOutput, percent)

    fun setPositionSetpoint(position: Double) =
        ctreMotorController.set(ControlMode.Position, position / feedbackCoefficient)

    fun setPositionSetpoint(position: Double, feedForward: Double) =
        ctreMotorController.set(
            ControlMode.Position, position / feedbackCoefficient,
            DemandType.ArbitraryFeedForward, feedForward
        )

    fun setVelocitySetpoint(velocity: Double) =
        ctreMotorController.set(ControlMode.Velocity, velocity / feedbackCoefficient / 10.0)

    fun setVelocitySetpoint(velocity: Double, feedForward: Double) =
        ctreMotorController.set(
            ControlMode.Velocity, velocity / feedbackCoefficient / 10.0,
            DemandType.ArbitraryFeedForward, feedForward
        )

    fun setCurrentSetpoint(current: Double) = ctreMotorController.set(ControlMode.Current, current)

    fun setCurrentSetpoint(current: Double, feedForward: Double) =
        ctreMotorController.set(ControlMode.Current, current, DemandType.ArbitraryFeedForward, feedForward)

    fun setMotionMagicSetpoint(position: Double) =
        ctreMotorController.set(ControlMode.MotionMagic, position / feedbackCoefficient)

    fun setMotionMagicSetpoint(position: Double, feedForward: Double) =
        ctreMotorController.set(
            ControlMode.MotionMagic, position / feedbackCoefficient,
            DemandType.ArbitraryFeedForward, feedForward
        )

    fun stop() {
        ctreMotorController.neutralOutput()
    }

    inline fun config(timeoutMs: Int = Int.MAX_VALUE, body: ConfigScope.() -> Unit) = apply {
        body(ConfigScope(timeoutMs))
    }

    private inline fun allTalons(body: (CTREMotorController) -> Unit) {
        body(ctreMotorController)
        followers.forEach(body)
    }

    inner class ConfigScope(private val timeoutMs: Int) {
        val ctreController get() = ctreMotorController

        val ctreFollowers get() = followers

        var feedbackCoefficient: Double
            get() = this@MotorController.feedbackCoefficient
            set(value) {
                this@MotorController.feedbackCoefficient = value
            }

        fun inverted(inverted: Boolean) = allTalons { it.inverted = inverted }

        fun brakeMode() = allTalons { it.setNeutralMode(NeutralMode.Brake) }

        fun coastMode() = allTalons { it.setNeutralMode(NeutralMode.Coast) }

        fun sensorPhase(inverted: Boolean) = ctreMotorController.setSensorPhase(inverted)

        fun closedLoopRamp(secondsToFull: Double) {
            ctreMotorController.configClosedloopRamp(secondsToFull)
        }

        fun openLoopRamp(secondsToFull: Double) {
            ctreMotorController.configOpenloopRamp(secondsToFull)
        }

        fun motionMagic(acceleration: Double, cruisingVelocity: Double) {
            val srxAcceleration = (acceleration / feedbackCoefficient / 10.0).toInt()
            val srxCruisingVelocity = (cruisingVelocity / feedbackCoefficient / 10.0).toInt()
            ctreMotorController.configMotionAcceleration(srxAcceleration)
            ctreMotorController.configMotionCruiseVelocity(srxCruisingVelocity)
        }

        inline fun pid(slot: Int = 0, body: PIDConfigScope.() -> Unit) = body(PIDConfigScope(slot))

        fun pidSlot(slot: Int) = ctreMotorController.selectProfileSlot(slot, 0)

        fun currentLimit(continuousLimit: Int, peakLimit: Int, peakDuration: Int) {
            // apply to following
            allTalons { controller ->
                if (controller is CTRETalonSRX) {
                    controller.configContinuousCurrentLimit(continuousLimit, timeoutMs)
                    controller.configPeakCurrentLimit(peakLimit, timeoutMs)
                    controller.configPeakCurrentDuration(peakDuration, timeoutMs)
                    controller.enableCurrentLimit(true)
                }
            }
        }

        fun encoderType(feedbackDevice: FeedbackDevice) {
            ctreMotorController.configSelectedFeedbackSensor(feedbackDevice, 0, timeoutMs)
        }

        fun encoderContinuous(continuous: Boolean) {
            ctreMotorController.configFeedbackNotContinuous(!continuous, timeoutMs)
        }

        inner class PIDConfigScope(private val slot: Int) {
            fun p(p: Double) {
                ctreMotorController.config_kP(slot, p / feedbackCoefficient, timeoutMs)
            }

            fun i(i: Double) {
                ctreMotorController.config_kI(slot, i / feedbackCoefficient, timeoutMs)
            }

            fun d(d: Double) {
                ctreMotorController.config_kD(slot, d / feedbackCoefficient, timeoutMs)
            }

            fun f(f: Double) {
                ctreMotorController.config_kF(slot, f / feedbackCoefficient, timeoutMs)
            }

            fun iZone(iZone: Double) {
                ctreMotorController.config_IntegralZone(slot, (iZone / feedbackCoefficient).toInt(), timeoutMs)
            }

            fun allowableError(allowableError: Double) {
                ctreMotorController.configAllowableClosedloopError(slot, (allowableError / feedbackCoefficient).toInt(), timeoutMs)
            }

            fun maxIntegralAccumulator(maxIntegralAccumulator: Double) {
                ctreMotorController.configMaxIntegralAccumulator(slot, maxIntegralAccumulator / feedbackCoefficient, timeoutMs)
            }

            fun closedLoopPeakOutput(closedLoopPeakOutput: Double) {
                ctreMotorController.configClosedLoopPeakOutput(slot, closedLoopPeakOutput, timeoutMs)
            }

            fun closedLoopPeriod(closedLoopPeriod: Int) {
                ctreMotorController.configClosedLoopPeriod(slot, closedLoopPeriod, timeoutMs)
            }
        }
    }
}
