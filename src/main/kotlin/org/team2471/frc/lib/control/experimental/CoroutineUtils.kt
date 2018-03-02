package org.team2471.frc.lib.control.experimental

import edu.wpi.first.wpilibj.DriverStation
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import org.team2471.frc.lib.util.measureTimeFPGAMicros
import java.lang.Long.min
import java.util.concurrent.TimeUnit
import kotlin.coroutines.experimental.CoroutineContext
import kotlin.math.roundToLong

/**
 * Runs the provided [body] of code periodically per [period] ms.
 *
 * The [period] parameter defaults to 20ms.
 *
 * Additionally, a [condition] can be provided to allow termination of the loop without cancellation
 * of the command coroutine.
 */
suspend inline fun periodic(period: Int = 20,
                            condition: () -> Boolean = { true }, body: () -> Unit) {
    while (condition()) {
        val microPeriod = period * 1000L

        val time = measureTimeFPGAMicros {
            body()
        }
        if (time > microPeriod) DriverStation.reportWarning("Periodic loop went over expected time. " +
                "Got: ${time / 1000}ms, expected less than ${period}ms", false)
        delay(microPeriod - min(time, microPeriod), TimeUnit.MICROSECONDS)
    }
}

suspend fun suspendUntil(pollingRate: Int = 20, condition: suspend () -> Boolean) {
    while (!condition()) delay(pollingRate.toLong(), TimeUnit.MILLISECONDS)
}

suspend fun parallel(coroutineContext: CoroutineContext, vararg blocks: suspend () -> Unit) {
    blocks.map { block ->
        launch(coroutineContext) { block() }
    }.forEach { it.join() }
}

suspend fun delaySeconds(time: Double) = delay((time * 1000).roundToLong())
