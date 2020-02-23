package org.team2471.frc.lib.control

import edu.wpi.first.wpilibj.Timer
import kotlin.math.sign

/**
 * A PD controller for closed loop system control.
 *
 * @param p the proportional gain of the system
 * @param d the differential gain of the system
 */
class PDConstantFController(var p: Double, var d: Double, var f: Double) {
    private var lastError: Double = 0.0
    private var lastTime: Double = Double.NaN

    fun updatePDF(newP: Double, newD: Double, newF: Double) {
        if (p != newP || d != newD || f != newF) {
            p = newP
            d = newD
            f = newF
            println("New newP: $p ;  New newD: $d ;  New newF: $f")

        }
    }

    /**
     * Updates the [PDConstantFController] with a new [error], and returns an output.
     *
     * @param error the new error of the system
     * @return the system output
     */
    fun update(error: Double): Double {
////        val time = Timer.getFPGATimestamp()
//
////        val dt = time - lastTime
//        val deltaError = (error - lastError) // dt
//        lastError = error
////        lastTime = time

        val deltaError = (error - lastError)
        lastError = error

        return error * p + deltaError * d + error.sign * f
    }
}
