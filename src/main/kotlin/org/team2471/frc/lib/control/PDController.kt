package org.team2471.frc.lib.control

import edu.wpi.first.wpilibj.Timer

/**
 * A PD controller for closed loop system control.
 *
 * @param p the proportional gain of the system
 * @param d the differential gain of the system
 */
class PDController(var p: Double, var d: Double) {
    private var lastError: Double = Double.NaN
    private var lastTime: Double = Double.NaN

    fun updatePD(newP: Double, newD: Double)  {
        if (p != newP || d != newD) {
            p = newP
            d = newD
            println("New newP: $p ;  New newD: $d")

        }
    }

    /**
     * Updates the [PDController] with a new [error], and returns an output.
     *
     * @param error the new error of the system
     * @return the system output
     */
    fun update(error: Double) : Double {
        val time = Timer.getFPGATimestamp()

        if (lastError == Double.NaN) {
            lastError = error
            lastTime = time
            return 0.0
        }

        val dt = time - lastTime
        val deltaError = (error - lastError) // dt
        lastError = error
        lastTime = time

        return error * p + deltaError * d
    }
}
