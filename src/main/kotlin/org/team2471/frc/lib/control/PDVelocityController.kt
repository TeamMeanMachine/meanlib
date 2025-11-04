package org.team2471.frc.lib.control

/**
 * A PD controller for closed loop velocity control.
 *
 * @param p the proportional gain of the system
 * @param d the differential gain of the system
 * @param ff the velocity feedforward gain of the system (gets multiplied by velocity setpoint)
 * @param coastToStop disable the PD controller when the setpoint is zero.
 */
class PDVelocityController(var p: Double, var d: Double, var ff: Double, val coastToStop: Boolean = false ) {
    var lastError: Double = 0.0
    var pdPower: Double = 0.0

    fun updatePDF(newP: Double = p, newD: Double = d, newFF: Double = ff) {
        if (p != newP || d != newD || ff != newFF) {
            p = newP
            d = newD
            ff = newFF
            println("New newP: $p ;  New newD: $d")
        }
    }

    fun update(velocitySetpoint: Double, currVelocity: Double): Double {
        val error = velocitySetpoint - currVelocity
        val ffPower = velocitySetpoint * ff

        val deltaError = error - lastError
        lastError = error

        pdPower += error * p + deltaError * d

        var power = pdPower + ffPower

        if (coastToStop && velocitySetpoint == 0.0) {
            power = 0.0
            pdPower = 0.0
        }

        if (pdPower + ffPower > 1.0) pdPower = 1.0 - ffPower

        return power
    }
}