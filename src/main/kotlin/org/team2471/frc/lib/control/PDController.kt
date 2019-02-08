package org.team2471.frc.lib.control

class PDController(var p: Double, var d: Double, val dt: Double) {
    private var lastError: Double = Double.NaN
    fun update(input: Double, target: Double) : Double {
        val error = target - input
        val deltaError = (lastError - error) / dt

        return error * p + deltaError * d
    }
}