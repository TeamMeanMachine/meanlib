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


inline fun Coroutine.periodic(
    period: Double = 0.0,
    watchOverrunName: String? = null,
    crossinline body: PeriodicScope.() -> Unit
) {
    val scope = PeriodicScope()

    val watchdog = if (watchOverrunName != null) {
        Watchdog(period) { DriverStationErrors.reportWarning("Periodic loop $watchOverrunName overrun", true) }
    } else {
        null
    }

    while (true) {
        watchdog?.reset()
        val dt = measureTimeMonotonic {
            body(scope)
        }
        if (scope.isDone) break
        val remainder = period - dt
        if (remainder <= 0.0 || period == 0.0) {
            yield()
        } else {
            wait(remainder.seconds)
        }
    }
}

inline fun Coroutine.periodicTimeout(
    timeout: Double,
    period: Double = 0.0,
    watchOverrunName: String? = null,
    crossinline body: PeriodicScope.(Double) -> Unit
): Boolean {
    val timer = Timer()
    var timedOut = false
    timer.start()
    periodic(period, watchOverrunName) {
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