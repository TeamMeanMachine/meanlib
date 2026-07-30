package org.team2471.frc.lib.ctre.loggedMotors

object MasterMotor {
    private val motors = mutableListOf<LoggedMotor>()

    /** Logs and simulates all [LoggedMotor] objects. */
    fun periodic() {
        motors.forEach {
            it.periodic()
        }
    }

    fun addMotor(motor: LoggedMotor) {
        //Ensures this gets added to the MasterMotor list only once
        if (!motors.contains(motor)) {
            motors.add(motor)
        }
    }
}