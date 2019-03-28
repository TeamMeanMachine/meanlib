package org.team2471.frc.lib.coroutines

import edu.wpi.first.wpilibj.DriverStation
import edu.wpi.first.wpilibj.Watchdog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.yield
import org.team2471.frc.lib.units.Time
import org.team2471.frc.lib.util.measureTimeFPGA

class PeriodicScope @PublishedApi internal constructor(val period: Double) {
    @PublishedApi
    internal var isDone = false

    fun stop() {
        isDone = true
    }
}

/**
 * An alias of `launch(MeanlibDispatcher)`.
 *
 * @see CoroutineScope.launch
 */
fun CoroutineScope.meanlibLaunch(
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> Unit
) = launch(MeanlibDispatcher, start, block)

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
suspend inline fun periodic(
    period: Double = 0.02,
    watchOverrun: Boolean = false,
    crossinline body: PeriodicScope.() -> Unit
) {
    val scope = PeriodicScope(period)

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
//    var cont: CancellableContinuation<Unit>? = null
//    var notifier: Notifier? = null
//    try {
//        suspendCancellableCoroutine<Unit> { c ->
//            cont = c
//            notifier = Notifier {
//                body(scope)
//                watchdog?.reset()
//                if (scope.isDone && c.isActive) c.resume(Unit)
//            }.apply { startPeriodic(period) }
//        }
//    } catch (e: Throwable) {
//        cont?.cancel(e)
//    } finally {
//        notifier?.close()
//        watchdog?.close()
//    }
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
suspend inline fun CoroutineScope.parallel(vararg blocks: suspend () -> Unit) {
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

suspend inline fun delay(time: Time) = delay(time.asSeconds)

/**
 * Suspends the coroutine forever.
 *
 * A halted coroutine can still be canceled.
 */
suspend inline fun halt(): Nothing = suspendCancellableCoroutine {}
