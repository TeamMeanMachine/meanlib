package org.team2471.frc.lib.actuators

import com.ctre.phoenix.motorcontrol.ControlMode
import com.ctre.phoenix.motorcontrol.DemandType
import com.ctre.phoenix.motorcontrol.NeutralMode
import org.team2471.frc.lib.math.DoubleRange
import kotlin.math.roundToInt
import com.ctre.phoenix.motorcontrol.can.TalonSRX as CTRETalonSRX

@Deprecated("Replaced with MotorController")
class TalonSRX(deviceId: Int, vararg followerIds: Int) {
    private val talon = CTRETalonSRX(deviceId)

    private var feedbackCoefficient = 1.0

    private val followers = followerIds.map { id ->
        val follower = CTRETalonSRX(id)
        follower.follow(talon)
        follower
    }.toTypedArray()

    init {
        allTalons { it.configFactoryDefault(Int.MAX_VALUE) }
        talon.selectedSensorPosition = 0
    }

    fun setPercentOutput(percent: Double) = talon.set(ControlMode.PercentOutput, percent)

    fun setPositionSetpoint(position: Double) = talon.set(ControlMode.Position, position / feedbackCoefficient)

    fun setPositionSetpoint(position: Double, feedForward: Double) =
        talon.set(ControlMode.Position, position / feedbackCoefficient, DemandType.ArbitraryFeedForward, feedForward)

    fun setVelocitySetpoint(velocity: Double) = talon.set(ControlMode.Velocity, velocity / feedbackCoefficient)

    fun setVelocitySetpoint(velocity: Double, feedForward: Double) =
        talon.set(ControlMode.Velocity, velocity / feedbackCoefficient, DemandType.ArbitraryFeedForward, feedForward)

    fun setCurrentSetpoint(current: Double) = talon.set(ControlMode.Current, current)

    fun setCurrentSetpoint(current: Double, feedForward: Double) =
        talon.set(ControlMode.Current, current, DemandType.ArbitraryFeedForward, feedForward)

    fun setMotionMagicSetpoint(position: Double) = talon.set(ControlMode.MotionMagic, position / feedbackCoefficient)

    fun setMotionMagicSetpoint(position: Double, feedForward: Double) =
        talon.set(ControlMode.MotionMagic, position / feedbackCoefficient, DemandType.ArbitraryFeedForward, feedForward)

    val velocity: Double
        get() = talon.getSelectedSensorVelocity(0) * feedbackCoefficient * 10.0

    val output: Double
        get() = talon.motorOutputPercent

    var position: Double
        get() = talon.getSelectedSensorPosition(0) * feedbackCoefficient
        set(value) {
            talon.selectedSensorPosition = (value / feedbackCoefficient).roundToInt()
        }

    val closedLoopError: Double
        get() = talon.closedLoopError * feedbackCoefficient

    fun stop() {
        talon.neutralOutput()
    }

    inline fun config(timeoutMs: Int = 20, body: ConfigScope.() -> Unit) = apply {
        body(ConfigScope(timeoutMs))
    }

    private inline fun allTalons(body: (CTRETalonSRX) -> Unit) {
        body(talon)
        followers.forEach(body)
    }

    inner class ConfigScope(private val timeoutMs: Int) {
        val ctreTalon get() = talon

        val ctreFollowers get() = followers

        var feedbackCoefficient: Double
            get() = this@TalonSRX.feedbackCoefficient
            set(value) {
                this@TalonSRX.feedbackCoefficient = value
            }

        fun inverted(inverted: Boolean) = allTalons { it.inverted = inverted }

        fun brakeMode() = allTalons { it.setNeutralMode(NeutralMode.Brake) }

        fun coastMode() = allTalons { it.setNeutralMode(NeutralMode.Coast) }

        fun sensorPhase(inverted: Boolean) = talon.setSensorPhase(inverted)

        fun closedLoopRamp(secondsToFull: Double) {
            talon.configClosedloopRamp(secondsToFull)
        }

        fun openLoopRamp(secondsToFull: Double) {
            talon.configOpenloopRamp(secondsToFull)
        }

        fun pidSlot(slot: Int) = talon.selectProfileSlot(slot, 0)

        fun currentLimit(continuousLimit: Int, peakLimit: Int, peakDuration: Int) {
            // apply to following
            allTalons { it.configContinuousCurrentLimit(continuousLimit, timeoutMs) }
            allTalons { it.configPeakCurrentLimit(peakLimit, timeoutMs) }
            allTalons { it.configPeakCurrentDuration(peakDuration, timeoutMs) }
            allTalons { it.enableCurrentLimit(true) }
        }

        fun nominalOutputRange(range: DoubleRange) {
            talon.configNominalOutputReverse(range.start, timeoutMs)
            talon.configNominalOutputForward(range.endInclusive, timeoutMs)
        }

        fun peakOutputRange(range: DoubleRange) {
            talon.configPeakOutputReverse(range.start, timeoutMs)
            talon.configPeakOutputForward(range.endInclusive, timeoutMs)
        }

        inline fun pid(slot: Int, body: PIDConfigScope.() -> Unit) = body(PIDConfigScope(slot))

        inner class PIDConfigScope(private val slot: Int) {
            fun p(p: Double) {
                talon.config_kP(slot, p, timeoutMs)
            }

            fun i(i: Double) {
                talon.config_kI(slot, i, timeoutMs)
            }

            fun d(d: Double) {
                talon.config_kD(slot, d, timeoutMs)
            }

            fun f(f: Double) {
                talon.config_kF(slot, f, timeoutMs)
            }

            fun iZone(iZone: Int) {
                talon.config_IntegralZone(slot, iZone, timeoutMs)
            }

            fun allowableError(allowableError: Int) {
                talon.configAllowableClosedloopError(slot, allowableError, timeoutMs)
            }

            fun maxIntegralAccumulator(maxIntegralAccumulator: Double) {
                talon.configMaxIntegralAccumulator(slot, maxIntegralAccumulator, timeoutMs)
            }

            fun closedLoopPeakOutput(closedLoopPeakOutput: Double) {
                talon.configClosedLoopPeakOutput(slot, closedLoopPeakOutput, timeoutMs)
            }

            fun closedLoopPeriod(closedLoopPeriod: Int) {
                talon.configClosedLoopPeriod(slot, closedLoopPeriod, timeoutMs)
            }
        }
    }
}