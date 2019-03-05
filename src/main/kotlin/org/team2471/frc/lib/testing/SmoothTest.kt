package org.team2471.frc.lib.testing

import org.team2471.frc.lib.actuators.MotorController
import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.math.cubicMap
import org.team2471.frc.lib.units.Time
import org.team2471.frc.lib.units.seconds
import org.team2471.frc.lib.util.Timer

suspend fun MotorController.smoothDrivePosition(position: Double, time: Time) {
    val timer = Timer()
    timer.start()
    periodic {
        val setpoint = cubicMap(0.0, time.asSeconds, this@smoothDrivePosition.position, position, timer.get())
        setPositionSetpoint(setpoint)

        if (timer.get().seconds >= time) {
            stop()
        }
    }
}
