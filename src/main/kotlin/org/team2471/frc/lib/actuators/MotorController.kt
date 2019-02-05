package org.team2471.frc.lib.actuators

import com.ctre.phoenix.motorcontrol.ControlMode
import com.ctre.phoenix.motorcontrol.DemandType
import com.ctre.phoenix.motorcontrol.NeutralMode
import com.ctre.phoenix.motorcontrol.can.BaseMotorController as CTREMotorController
import kotlin.math.roundToInt
import com.ctre.phoenix.motorcontrol.can.TalonSRX as CTRETalonSRX
import com.ctre.phoenix.motorcontrol.can.VictorSPX as CTREVictorSPX

sealed class MotorControllerID

data class TalonID(val value: Int) : MotorControllerID()
data class VictorID(val value: Int) : MotorControllerID()

private fun CTREMotorController(id: MotorControllerID) = when(id) {
    is TalonID -> CTRETalonSRX(id.value)
    is VictorID -> CTRETalonSRX(id.value)
}

class MotorController(deviceId: MotorControllerID, vararg followerIds: MotorControllerID) {
    private val ctreMotorController = CTREMotorController(deviceId)

    private var feedbackCoefficient = 1.0

    private val followers = followerIds.map { id ->
        val follower = CTREMotorController(id)
        follower.follow(ctreMotorController)
        follower
    }.toTypedArray()

    init {
        allTalons { it.configFactoryDefault(Int.MAX_VALUE) }
        ctreMotorController.selectedSensorPosition = 0
    }

    fun setPercentOutput(percent: Double) = ctreMotorController.set(ControlMode.PercentOutput, percent)

    fun setPositionSetpoint(position: Double) = ctreMotorController.set(ControlMode.Position, position / feedbackCoefficient)

    fun setPositionSetpoint(position: Double, feedForward: Double) =
        ctreMotorController.set(ControlMode.Position, position / feedbackCoefficient, DemandType.ArbitraryFeedForward, feedForward)

    fun setVelocitySetpoint(velocity: Double) = ctreMotorController.set(ControlMode.Velocity, velocity / feedbackCoefficient)

    fun setVelocitySetpoint(velocity: Double, feedForward: Double) =
        ctreMotorController.set(ControlMode.Velocity, velocity / feedbackCoefficient, DemandType.ArbitraryFeedForward, feedForward)

    fun setCurrentSetpoint(current: Double) = ctreMotorController.set(ControlMode.Current, current)

    fun setCurrentSetpoint(current: Double, feedForward: Double) =
        ctreMotorController.set(ControlMode.Current, current, DemandType.ArbitraryFeedForward, feedForward)

    fun setMotionMagicSetpoint(position: Double) = ctreMotorController.set(ControlMode.MotionMagic, position / feedbackCoefficient)

    fun setMotionMagicSetpoint(position: Double, feedForward: Double) =
        ctreMotorController.set(ControlMode.MotionMagic, position / feedbackCoefficient, DemandType.ArbitraryFeedForward, feedForward)

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

        inline fun pid(slot: Int, body: PIDConfigScope.() -> Unit) = body(PIDConfigScope(slot))

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

        inner class PIDConfigScope(private val slot: Int) {
            fun p(p: Double) {
                ctreMotorController.config_kP(slot, p, timeoutMs)
            }

            fun i(i: Double) {
                ctreMotorController.config_kI(slot, i, timeoutMs)
            }

            fun d(d: Double) {
                ctreMotorController.config_kD(slot, d, timeoutMs)
            }

            fun f(f: Double) {
                ctreMotorController.config_kF(slot, f, timeoutMs)
            }

            fun iZone(iZone: Int) {
                ctreMotorController.config_IntegralZone(slot, iZone, timeoutMs)
            }

            fun allowableError(allowableError: Int) {
                ctreMotorController.configAllowableClosedloopError(slot, allowableError, timeoutMs)
            }

            fun maxIntegralAccumulator(maxIntegralAccumulator: Double) {
                ctreMotorController.configMaxIntegralAccumulator(slot, maxIntegralAccumulator, timeoutMs)
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
