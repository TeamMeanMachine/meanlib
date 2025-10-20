package org.team2471.frc.lib.ctre.loggedTalonFX

import org.team2471.frc.lib.util.isSim

object MasterMotor {
    private val motors = mutableListOf<LoggedMotor>()

    fun simPeriodic() {
        if (isSim) {
            motors.forEach {
                it.simPeriodic()
            }
        }
    }

    fun addMotor(motor: LoggedMotor) = motors.add(motor)
}