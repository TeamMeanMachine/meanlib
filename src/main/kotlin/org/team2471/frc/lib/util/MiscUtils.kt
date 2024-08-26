package org.team2471.frc.lib.util

import edu.wpi.first.math.geometry.Translation2d
import edu.wpi.first.math.geometry.Translation3d
import edu.wpi.first.wpilibj.RobotController
import edu.wpi.first.wpilibj.Timer
import org.littletonrobotics.junction.Logger
import org.team2471.frc.lib.math.square
import java.lang.System.currentTimeMillis
import kotlin.math.hypot
import kotlin.math.sqrt

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

/**
 * Returns the true FPGA timestamp in seconds, regardless of the timestamp used for logging.
 */
fun getRealFPGATimestamp() = Logger.getRealTimestamp() / 1000000.0

class Timer {
    var startTime: Long = 0

    fun start() {
        startTime = currentTimeMillis()
    }

    fun get() : Double = (currentTimeMillis() - startTime) / 1000.0
}

val Translation2d.length: Double
    get() = hypot(this.x, this.y)

val Translation3d.length: Double
    get() = sqrt(square(this.x) + square(this.y) + square(this.z))
