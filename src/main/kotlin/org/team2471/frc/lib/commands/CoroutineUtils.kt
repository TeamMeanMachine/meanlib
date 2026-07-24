package org.team2471.frc.lib.commands

import org.team2471.frc.lib.coroutines.measureTimeMonotonic
import org.team2471.frc.lib.units.seconds
import org.wpilib.command3.Command
import org.wpilib.command3.Coroutine
import org.wpilib.driverstation.DriverStationErrors
import org.wpilib.system.Timer
import org.wpilib.system.Watchdog

/** Utility functions for [Coroutine]s. Which are used inside [Command] actions, and allow for muti-threading "like" features. Ex: yield() await() */
// Different from [kotlinx.coroutines] which are also installed. (Those use the "suspend" keyword)

/**
 * Runs the provided [body] of code periodically per loop.
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
inline fun Coroutine.periodic(
    watchOverrunName: String? = null,
    crossinline body: PeriodicScope.() -> Unit
) {
    val scope = PeriodicScope()

    val watchdog = if (watchOverrunName != null) {
        Watchdog(0.01) { DriverStationErrors.reportWarning("Periodic loop $watchOverrunName overrun > 0.01", true) }
    } else {
        null
    }

    while (true) {
        watchdog?.reset()
        val dt = measureTimeMonotonic {
            body(scope)
        }
        if (scope.isDone) break
        yield()
    }
}

inline fun Coroutine.periodicTimeout(
    timeout: Double,
    watchOverrunName: String? = null,
    crossinline body: PeriodicScope.(Double) -> Unit
): Boolean {
    val timer = Timer()
    var timedOut = false
    timer.start()
    periodic(watchOverrunName) {
        val t = timer.get()
        if (timer.get() > timeout) {
            timedOut = true
            stop()
        } else {
            body(t)
        }
    }
    return timedOut
}

fun Coroutine.parallel(
    vararg commands: Command
) {
    awaitAll(*commands)
}

fun Coroutine.parallel(
    vararg blocks: Coroutine.() -> Unit
) {
    parallel(*blocks.mapIndexed { index, coroutine -> command("unnamedParallel$index") { coroutine() }}.toTypedArray())
}