package org.team2471.frc.lib.control.experimental

import edu.wpi.first.wpilibj.DriverStation
import kotlinx.coroutines.experimental.delay
import org.team2471.frc.lib.util.measureTimeFPGAMicros
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
                     condition: () -> Boolean = { true }, body: () -> Unit) {
    while (condition()) {
        val microPeriod = period * 1000L

        val time = measureTimeFPGAMicros {
            body()
        }
        if (time > microPeriod) DriverStation.reportWarning("Periodic loop went over expected time. " +
                "Got: ${time}ms, expected less than ${period}ms", false)
        delay(microPeriod - min(time, microPeriod), TimeUnit.MICROSECONDS)
    }
}

suspend fun suspendUntil(pollingRate: Int = 20, condition: suspend () -> Boolean) {
    while (!condition()) delay(pollingRate.toLong(), TimeUnit.MILLISECONDS)
}
