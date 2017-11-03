package org.team2471.frc.lib.control

import com.ctre.MotorControl.CANTalon
import com.ctre.MotorControl.SmartMotorController.TalonControlMode

operator fun CANTalon.plus(slave: CANTalon): CANTalon = apply {
    slave.changeControlMode(TalonControlMode.Follower)
    slave.set(this.deviceID.toDouble())
}
