package org.team2471.frc.lib.control

import com.ctre.phoenix.ParamEnum
import com.ctre.phoenix.motorcontrol.ControlMode
import com.ctre.phoenix.motorcontrol.can.TalonSRX
import edu.wpi.first.wpilibj.Sendable
import edu.wpi.first.wpilibj.smartdashboard.SendableBuilder

operator fun TalonSRX.plus(slave: TalonSRX): TalonSRX = apply {
    slave.set(ControlMode.Follower, deviceID.toDouble())
}

inline val TalonSRX.pidSendable
    get() = object : Sendable {
        private var name = "Talon SRX"
        private var subsystem = "Ungrouped"

        override fun setName(name: String?) {
            if (name != null) this.name = name
        }

        override fun getName(): String = name

        override fun initSendable(builder: SendableBuilder) {
            listOf("P" to ParamEnum.eProfileParamSlot_P,
                    "I" to ParamEnum.eProfileParamSlot_I,
                    "D" to ParamEnum.eProfileParamSlot_D,
                    "F" to ParamEnum.eProfileParamSlot_F,
                    "I Zone" to ParamEnum.eClosedLoopIAccum).forEach { (key, param) ->
                builder.addDoubleProperty(key, { configGetParameter(param, 0, 0) },
                        { value -> configSetParameter(param, value, 0, 0, 0) })
            }
        }

        override fun getSubsystem(): String = subsystem

        override fun setSubsystem(subsystem: String?) {
            if (subsystem != null) this.subsystem = subsystem
        }
    }
