package org.team2471.frc.lib.control

import edu.wpi.first.wpilibj.Timer

class PDController(var p: Double, var d: Double) {
    private var lastError: Double = Double.NaN
    private var lastTime: Double = Double.NaN

    fun update(error: Double) : Double {
        val time = Timer.getFPGATimestamp()

        if (lastError == Double.NaN) {
            lastError = error
            lastTime = time
            return 0.0
        }

        val dt = time - lastTime
        val deltaError = (error - lastError) / dt
        lastError = error
        lastTime = time

        return error * p + deltaError * d
    }
}