package org.team2471.frc.lib.coroutines

import edu.wpi.first.wpilibj.DriverStation
import edu.wpi.first.wpilibj.Timer
import edu.wpi.first.wpilibj.Watchdog
import kotlinx.coroutines.delay
import kotlinx.coroutines.yield
import org.team2471.frc.lib.commands.PeriodicScope


/**
 * Runs the provided [body] of code periodically per [period] seconds.
 *
 * The provided [body] loop will continue to loop until [PeriodicScope.stop] is called, or an exception is thrown.
 * Note that if [PeriodicScope.stop] is called the body will continue to run to the end of the loop. If your
 * intention is to exit the code early, insert a return after calling [PeriodicScope.stop].
 *
 * The [period] parameter defaults to 0.02 seconds, or 20 milliseconds.
 *
 * If the [body] takes longer than the [period] to complete, a warning is printed. This can
 * be disabled by setting the [watchOverrun] parameter to false.
 */
suspend inline fun periodiccc(
    period: Double = 0.02,
    watchOverrun: Boolean = false,
    crossinline body: PeriodicScope.() -> Unit
) {
    val scope = PeriodicScope()

    val watchdog = if (watchOverrun) {
        Watchdog(period) { DriverStation.reportWarning("Periodic loop overrun", true) }
    } else {
        null
    }

    while (true) {
        watchdog?.reset()
        val dt = measureTimeFPGA {
            body(scope)
        }
        if (scope.isDone) break
        val remainder = period - dt
        if (remainder <= 0.0) {
            yield()
        } else {
            delay(remainder)
        }
    }
}

/**
 * Executes the given block and returns elapsed time in seconds.
 */
inline fun measureTimeFPGA(body: () -> Unit): Double {
    val start = Timer.getFPGATimestamp()
    body()
    return Timer.getFPGATimestamp() - start
}

/**
 * Suspends the coroutine for [time] seconds.
 *
 * @see kotlinx.coroutines.delay
 */
suspend inline fun delay(time: Double) = delay((time * 1000).toLong())
