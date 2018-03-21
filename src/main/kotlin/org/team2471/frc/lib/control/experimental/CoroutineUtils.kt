package org.team2471.frc.lib.control.experimental

import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import java.util.concurrent.TimeUnit
import kotlin.coroutines.experimental.coroutineContext
import kotlin.math.roundToLong

/**
 * Runs the provided [body] of code periodically per [period] ms.
 *
 * The [period] parameter defaults to 20ms.
 *
 * Additionally, a [condition] can be provided to allow termination of the loop without cancellation
 * of the command coroutine.
 */
suspend inline fun periodic(period: Long = 20,
                            condition: () -> Boolean = { true }, body: () -> Unit) {
    while (condition()) {
        body()
        delay(period)
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

suspend fun delaySeconds(time: Double) = delay((time * 1000).roundToLong())
