package org.team2471.frc.lib.control.experimental

import edu.wpi.first.wpilibj.DriverStation
import kotlinx.coroutines.experimental.delay
import org.team2471.frc.lib.util.measureTimeFPGAMillis
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
suspend inline fun periodic(period: Long = 20,
                     condition: () -> Boolean = { true }, body: () -> Unit) {
    while (condition()) {
        val time = measureTimeFPGAMillis {
            body()
        }
        if (time > period) DriverStation.reportWarning("Periodic loop went over expected time. " +
                "Got: ${time}ms, expected less than ${period}ms", false)
        delay(period - min(time, period))
    }
}

suspend fun suspendUntil(pollingRate: Int = 20, condition: suspend () -> Boolean) {
    while (!condition()) delay(pollingRate.toLong(), TimeUnit.MILLISECONDS)
}
