package org.team2471.frc.lib.sensors.AnalogInput

import org.littletonrobotics.junction.LogTable
import org.littletonrobotics.junction.inputs.LoggableInputs

interface LoggedAnalogIO {
    open class AnalogIOInputs(val name: String): LoggableInputs {
        var ticks = 0
        var voltage = 0.0

        override fun toLog(table: LogTable) {
            table.put("$name/Ticks", ticks)
            table.put("$name/Voltage", voltage)
        }

        override fun fromLog(table: LogTable) {
            table.get("$name/Ticks", ticks)
            table.get("$name/Voltage", voltage)
        }
    }

    fun updateInputs(inputs: AnalogIOInputs)
}