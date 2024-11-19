package org.team2471.frc.lib.sensors.canCoder

import org.littletonrobotics.junction.LogTable
import org.littletonrobotics.junction.inputs.LoggableInputs
import org.team2471.frc.lib.units.Angle

interface LoggedCANCoderIO {
    var simAngleSupplier: () -> Angle
    open class CANCoderIOInputs(val name: String): LoggableInputs {
        var position = 0.0
        var velocity = 0.0

        override fun toLog(table: LogTable) {
            table.put("$name/position", position)
            table.put("$name/velocity", velocity)
        }

        override fun fromLog(table: LogTable) {
            position = table.get("$name/position", position)
            velocity = table.get("$name/velocity", velocity)
        }

    }

    fun updateInputs(inputs: CANCoderIOInputs)

    fun setMagnetSensorOffset(offset: Double) {}
    fun setPosition(position: Double) {}
    fun setInverted(invert: Boolean) {}
}