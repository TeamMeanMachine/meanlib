package org.team2471.frc.lib.util

import edu.wpi.first.wpilibj.Timer

/**
 * Executes the given block and returns elapsed time in seconds.
 */
fun measureTimeFPGA(body: () -> Unit): Double {
    val start = Timer.getFPGATimestamp()
    body()
    return Timer.getFPGATimestamp() - start
}

fun Double.deadband(tolerance: Double) = if(Math.abs(this) < tolerance) {
    0.0
} else {
    this
}

