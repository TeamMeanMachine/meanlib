package org.team2471.frc.lib.util

import edu.wpi.first.wpilibj.RobotController
import edu.wpi.first.wpilibj.Timer
import java.lang.System.currentTimeMillis

/**
 * Executes the given block and returns elapsed time in seconds.
 */
inline fun measureTimeFPGA(body: () -> Unit): Double {
    val start = Timer.getFPGATimestamp()
    body()
    return Timer.getFPGATimestamp() - start
}

/**
 * Executes the given block and returns elapsed time in nanoseconds.
 */
inline fun measureTimeFPGAMicros(body: () -> Unit): Long {
    val start = RobotController.getFPGATime()
    body()
    return RobotController.getFPGATime() - start
}

class Timer {
    var startTime: Long = 0

    fun start() {
        startTime = currentTimeMillis()
    }

    fun get() : Double = (currentTimeMillis() - startTime) / 1000.0
}
