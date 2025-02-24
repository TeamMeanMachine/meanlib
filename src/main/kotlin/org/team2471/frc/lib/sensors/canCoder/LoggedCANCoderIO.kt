package org.team2471.frc.lib.sensors.canCoder

import org.littletonrobotics.junction.LogTable
import org.littletonrobotics.junction.inputs.LoggableInputs
import org.team2471.frc.lib.units.Angle

interface LoggedCANCoderIO {
    var simAngleSupplier: () -> Angle
    open class CANCoderIOInputs(val name: String): LoggableInputs {
        var position = 0.0
        var absolutePosition = 0.0
        var velocity = 0.0

        override fun toLog(table: LogTable) {
            table.put("$name/position", position)
            table.put("$name/position", absolutePosition)
            table.put("$name/velocity", velocity)
        }

        override fun fromLog(table: LogTable) {
            position = table.get("$name/position", position)
            absolutePosition = table.get("$name/position", position)
            velocity = table.get("$name/velocity", velocity)
        }

    }

    fun updateInputs(inputs: CANCoderIOInputs)

    fun setMagnetOffset(offset: Double) {}
    fun setInverted(invert: Boolean) {}
}