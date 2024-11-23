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


/**
 * Calculates a running average by incorporating a new number into the existing average.
 *
 * This function uses an incremental formula to update the average, which is more
 * efficient for large datasets as it doesn't require storing all previous values.
 *
 * The math can be found here: https://math.stackexchange.com/questions/2845793/recursive-mean-computation
 *
 * @param previousAverage The current average before adding the new number.
 * @param newNumber The new number to be incorporated into the average.
 * @param size The total number of items in the dataset, including the new number.
 * @return The updated average after incorporating the new number.
 * @throws IllegalArgumentException if size is less than or equal to 0.
 *
 * @author Thatcher Moore
 */
fun calculateAverage(previousAverage: Double, newNumber: Double, size: Int): Double {
    require(size > 0)
    return if (size == 1) {
        newNumber
    } else {
        previousAverage + (1.0 / size) * (newNumber - previousAverage)
    }
}