package org.team2471.frc.lib.coroutines

import edu.wpi.first.wpilibj.DriverStation
import kotlinx.coroutines.*
import org.team2471.frc.lib.util.measureTimeFPGA

internal class BreakPeriodic : Throwable()

class PeriodicScope @PublishedApi internal constructor(val period: Double) {
    fun exitPeriodic(): Nothing = throw BreakPeriodic()
}

/**
 * Runs the provided [body] of code periodically per [period] seconds.
 *
 * The periodic loop will continue until [PeriodicScope.exitPeriodic] is called.
 *
 * The [period] parameter defaults to 0.02 seconds, or 20 milliseconds.
 *
 * If the [body] takes longer than the [period] to complete, a warning is printed. This can
 * be disabled by setting the [watchOverrun] parameter to false.
 */
suspend inline fun periodic(period: Double = 0.02, watchOverrun: Boolean = true, body: PeriodicScope.() -> Unit) = try {
    val scope = PeriodicScope(period)

    while (true) {
        val dt = measureTimeFPGA { body(scope) }
        if (watchOverrun && dt > period) DriverStation.reportWarning(
            "Code in periodic block ran ${period - dt} over it's period!",
            Thread.currentThread().stackTrace
        )

        delay(period - dt)
    }
} catch (_: BreakPeriodic) {
    // do nothing
}

/**
 * Suspends until [condition] evaluates to true.
 *
 * @param pollingRate The time between each check, in milliseconds
 */
suspend inline fun suspendUntil(pollingRate: Int = 20, condition: () -> Boolean) {
    while (!condition()) delay(pollingRate.toLong())
}

/**
 * Runs each block in [blocks] in a child coroutine, and suspends until all of them have completed.
 *
 * If one child is cancelled, the remaining children are stopped, and the exception is propagated upwards.
 */
suspend inline fun parallel(vararg blocks: suspend () -> Unit) = coroutineScope {
    blocks.map { block ->
        launch { block() }
    }.forEach {
        it.join()
    }
}

/**
 * Suspends the coroutine for [time] seconds.
 *
 * @see kotlinx.coroutines.delay
 */
suspend inline fun delay(time: Double) = delay((time * 1000).toLong())

/**
 * Suspends the coroutine forever.
 *
 * A halted coroutine can still be canceled.
 */
suspend inline fun halt(): Nothing = suspendCancellableCoroutine {}
