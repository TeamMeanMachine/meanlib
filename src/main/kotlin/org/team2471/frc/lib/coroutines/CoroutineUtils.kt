package org.team2471.frc.lib.coroutines

import edu.wpi.first.wpilibj.DriverStation
import edu.wpi.first.wpilibj.Notifier
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.team2471.frc.lib.util.measureTimeFPGA
import kotlin.coroutines.resume

/**
 * Runs the provided [body] of code periodically per [period] seconds.
 *
 * When [body] evaluates to true, the periodic loop is terminated.
 *
 * The [period] parameter defaults to 0.02 seconds, or 20 milliseconds.
 */
suspend fun periodic(period: Double = 0.02, watchdog: Boolean = true, body: (Double) -> Boolean) {
    val stackTrace = if (watchdog) Thread.currentThread().stackTrace else null

    lateinit var notifier: Notifier
    suspendCancellableCoroutine<Unit> { cont ->
        var isFinished = false
        notifier = Notifier {
            if (watchdog) {
                val dt = measureTimeFPGA {
                    isFinished = body(period)
                }

                if (dt > period) {
                    DriverStation.reportWarning(
                        "Periodic loop took ${dt - period}s longer " +
                                "than allowed period (${period}s).", stackTrace!!
                    )
                }
            } else {
                isFinished = body(period)
            }

            if (isFinished) {
                cont.resume(Unit)
            }
        }

        notifier.startPeriodic(period)
    }

    notifier.stop()
}

/**
 * Runs the provided [body] of code periodically per [period] seconds.
 *
 * The [period] parameter defaults to 0.02 seconds, or 20 milliseconds.
 */
suspend fun loop(period: Double = 0.02, watchdog: Boolean = true, body: (Double) -> Unit) {
    val stackTrace = if (watchdog) Thread.currentThread().stackTrace else null

    lateinit var notifier: Notifier
    suspendCancellableCoroutine<Unit> { cont ->
        notifier = Notifier {
            if (watchdog) {
                val dt = measureTimeFPGA { body(period) }

                if (dt > period) DriverStation.reportWarning(
                        "Periodic loop took ${dt - period}s longer " +
                                "than allowed period (${period}s).", stackTrace!!
                )
            } else {
                body(period)
            }
        }

        notifier.startPeriodic(period)
    }

    notifier.stop()
}

suspend fun suspendUntil(pollingRate: Int = 20, condition: suspend () -> Boolean) {
    while (!condition()) delay(pollingRate.toLong())
}

suspend fun parallel(vararg blocks: suspend () -> Unit) = coroutineScope {
    blocks.map { block ->
        launch(coroutineContext) { block() }
    }.forEach { it.join() }
}

suspend fun delay(time: Double) = delay((time * 1000).toLong())

suspend fun halt(): Nothing = suspendCancellableCoroutine {}
