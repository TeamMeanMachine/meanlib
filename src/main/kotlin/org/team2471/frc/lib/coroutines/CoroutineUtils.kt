package org.team2471.frc.lib.coroutines

import edu.wpi.first.wpilibj.DriverStation
import edu.wpi.first.wpilibj.Notifier
import kotlinx.coroutines.*
import org.team2471.frc.lib.util.measureTimeFPGA
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Runs the provided [body] of code periodically per [period] seconds.
 *
 * The periodic loop will continue until a [PeriodicBreak] is thrown from the [body].
 *
 * The [period] parameter defaults to 0.02 seconds, or 20 milliseconds.
 *
 * If the [body] takes longer than the [period] to complete, a warning is printed. This can
 * be disabled by setting the [watchdog] parameter to false.
 */
suspend fun periodic(period: Double = 0.02, watchdog: Boolean = true, body: () -> Unit) {
    val stackTrace = if (watchdog) Thread.currentThread().stackTrace else null

    lateinit var notifier: Notifier
    suspendCancellableCoroutine<Unit> { cont ->
        notifier = Notifier {
            try {
                if (watchdog) {
                    val dt = measureTimeFPGA { body() }
                    if (dt > period) DriverStation.reportWarning(
                        "Periodic loop took ${dt - period}s longer " +
                                "than allowed period (${period}s).", stackTrace!!
                    )
                } else {
                    body()
                }
            } catch (_: PeriodicBreak) {
                cont.resume(Unit)
                notifier.stop()
            } catch (e: Throwable) {
                cont.resumeWithException(e)
                notifier.stop()
            }
        }

        notifier.startPeriodic(period)
    }
}

class PeriodicBreak : Throwable()

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
suspend fun parallel(vararg blocks: suspend () -> Unit) = coroutineScope {
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
