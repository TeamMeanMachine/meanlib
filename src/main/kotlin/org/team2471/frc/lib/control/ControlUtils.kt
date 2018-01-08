package org.team2471.frc.lib.control

import com.ctre.phoenix.motorcontrol.ControlMode
import com.ctre.phoenix.motorcontrol.can.TalonSRX

operator fun TalonSRX.plus(slave: TalonSRX): TalonSRX = apply {
    slave.set(ControlMode.Follower, deviceID.toDouble())
}
