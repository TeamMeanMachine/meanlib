package org.team2471.frc.lib.control.experimental

import edu.wpi.first.wpilibj.DriverStation
import kotlinx.coroutines.experimental.delay
import org.team2471.frc.lib.util.measureNanoTimeFPGA
import java.lang.Long.min
import java.util.concurrent.TimeUnit

/**
 * Runs the provided [body] of code periodically per [period] ms.
 *
 * The [period] parameter defaults to 20ms.
 *
 * Additionally, a [condition] can be provided to allow termination of the loop without cancellation
 * of the command coroutine.
 */
suspend inline fun periodic(period: Int = 20,
                     condition: () -> Boolean = { true }, crossinline body: () -> Unit) {
    val nanoPeriod = period * 1000L
    while (condition()) {
        val time = measureNanoTimeFPGA {
            body()
        }
        if (time > nanoPeriod) DriverStation.reportWarning("Periodic loop went over expected time. " +
                "Got: ${time / 1000.0}ms, expected: <${period}ms", false)
        delay(nanoPeriod - min(time, nanoPeriod), TimeUnit.NANOSECONDS)
    }
}

suspend fun suspendUntil(pollingRate: Int = 20, condition: suspend () -> Boolean) {
    while (!condition()) delay(pollingRate.toLong(), TimeUnit.MILLISECONDS)
}
