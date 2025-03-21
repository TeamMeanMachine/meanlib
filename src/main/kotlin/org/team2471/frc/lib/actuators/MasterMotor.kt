package org.team2471.frc.lib.actuators

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.team2471.frc.lib.coroutines.periodic
import java.util.*

object MasterMotor {
//    private val motors =
    private val syncMotors = Collections.synchronizedList(arrayListOf<MotorController>())
    init {
        println("MASTER MOTOR INIT")
        GlobalScope.launch {
            periodic(0.02) {
                try {
//                    synchronized(motors) {
                        syncMotors.forEach {
                            it.periodicLoop()
                        }
//                    }
                } catch (_: Exception) { }
            }
        }
    }

    fun addMotor(motor: MotorController) {
//        synchronized(motors) {
            syncMotors.add(motor)
//        }
    }
}