package org.team2471.frc.lib.util

import edu.wpi.first.wpilibj.Timer
import edu.wpi.first.wpilibj.Utility

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
inline fun measureNanoTimeFPGA(body: () -> Unit): Long {
    val start = Utility.getFPGATime()
    body()
    return Utility.getFPGATime() - start
}
