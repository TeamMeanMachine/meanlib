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
}