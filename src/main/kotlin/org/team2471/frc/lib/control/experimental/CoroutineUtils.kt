package org.team2471.frc.lib.control.experimental

import edu.wpi.first.wpilibj.DriverStation
import edu.wpi.first.wpilibj.Notifier
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.suspendCancellableCoroutine
import org.team2471.frc.lib.util.measureTimeFPGA
import java.util.concurrent.TimeUnit
import kotlin.coroutines.experimental.coroutineContext

/**
 * Runs the provided [body] of code periodically per [period] seconds.
 *
 * The [period] parameter defaults to 0.02 seconds, or 20 milliseconds.
 */
suspend fun periodic(period: Double = 0.02, watchdog: Boolean = true, body: (Double) -> Boolean) {
    val stackTrace = if (watchdog) Thread.currentThread().stackTrace else null

    suspendCancellableCoroutine<Unit> { cont ->
        var isFinished = false
        val notifier = Notifier {
            if (watchdog) {
                val dt = measureTimeFPGA {
                    isFinished = body(period)
                }

                if (dt > period) {
                    DriverStation.reportWarning("Periodic loop took ${dt - period}s longer " +
                            "than allowed period (${period}s).", stackTrace!!)
                }
            } else {
                isFinished = body(period)
            }

            if (isFinished) {
                cont.completeResume(Unit)
            }
        }

        try {
            notifier.startPeriodic(period)
        } finally {
            notifier.stop()
        }
    }
}

suspend fun suspendUntil(pollingRate: Int = 20, condition: suspend () -> Boolean) {
    while (!condition()) delay(pollingRate.toLong(), TimeUnit.MILLISECONDS)
}

suspend fun parallel(vararg blocks: suspend () -> Unit) {
    blocks.map { block ->
        launch(coroutineContext) { block() }
    }.forEach { it.join() }
}

suspend fun delay(time: Double) = delay((time * 1000).toLong())
