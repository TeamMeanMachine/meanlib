package org.team2471.frc.lib.vision

import org.littletonrobotics.junction.LogTable
import org.littletonrobotics.junction.inputs.LoggableInputs

interface LimelightCameraIO {
    class LimelightCameraInputs(val name: String): LoggableInputs {
        var mt2Result: DoubleArray = DoubleArray(11)

        override fun toLog(table: LogTable) {
            table.put("$name/MT2Result", mt2Result)
        }

        override fun fromLog(table: LogTable) {
            mt2Result = table.get("$name/MT2Result", mt2Result)
        }
    }

    fun updateInputs(inputs: LimelightCameraInputs) {}
    fun reset() {}
}