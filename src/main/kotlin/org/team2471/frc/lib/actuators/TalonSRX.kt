package org.team2471.frc.lib.actuators

import com.ctre.phoenix.motorcontrol.ControlMode
import com.ctre.phoenix.motorcontrol.DemandType
import com.ctre.phoenix.motorcontrol.can.TalonSRX as CTRETalonSRX

class TalonSRX(deviceId: Int, vararg followerIds: Int) {
    private val talon = CTRETalonSRX(deviceId)

    private val followers = followerIds.map { id ->
        val follower = CTRETalonSRX(id)
        follower.follow(talon)
        follower
    }

    init {
        allTalons { it.configFactoryDefault(Int.MAX_VALUE) }
    }

    fun setPercent(percent: Double) = talon.set(ControlMode.PercentOutput, percent)

    fun setPosition(position: Double) = talon.set(ControlMode.Position, position)

    fun setPosition(position: Double, feedForward: Double) =
        talon.set(ControlMode.Position, position, DemandType.ArbitraryFeedForward, feedForward)

    fun setVelocity(velocity: Double) = talon.set(ControlMode.Velocity, velocity)

    fun setVelocity(velocity: Double, feedForward: Double) =
        talon.set(ControlMode.Velocity, velocity, DemandType.ArbitraryFeedForward, feedForward)

    fun setCurrent(current: Double) = talon.set(ControlMode.Current, current)

    fun setCurrent(current: Double, feedForward: Double) =
        talon.set(ControlMode.Current, current, DemandType.ArbitraryFeedForward, feedForward)

    fun setMotionMagic(position: Double) = talon.set(ControlMode.MotionMagic, position)

    fun setMotionMagic(position: Double, feedForward: Double) =
        talon.set(ControlMode.MotionMagic, position, DemandType.ArbitraryFeedForward, feedForward)

    inline fun config(timeoutMs: Int = 0, body: ConfigScope.() -> Unit) = apply {
        body(ConfigScope(timeoutMs))
    }

    private inline fun allTalons(body: (CTRETalonSRX) -> Unit) {
        body(talon)
        followers.forEach(body)
    }

    inner class ConfigScope(private val timeoutMs: Int) {
        inline fun pid(slot: Int, body: PIDConfigScope.() -> Unit) = body(PIDConfigScope(slot))

        fun pidSlot(slot: Int) {
            talon.selectProfileSlot(slot, 0)
        }

        fun currentLimit(continuousLimit: Int, peakLimit: Int, peakDuration: Int) {
            // apply to followers
            allTalons { it.configContinuousCurrentLimit(continuousLimit, timeoutMs) }
            allTalons { it.configPeakCurrentLimit(peakLimit, timeoutMs) }
            allTalons { it.configPeakCurrentDuration(peakDuration, timeoutMs) }
            allTalons { it.enableCurrentLimit(true) }
        }

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