package org.team2471.frc.lib.control.experimental

import edu.wpi.first.wpilibj.DriverStation
import edu.wpi.first.wpilibj.Utility
import kotlinx.coroutines.experimental.delay
import org.team2471.frc.lib.util.measureTimeFPGA
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
    while (condition()) {
        val time = measureTimeFPGA {
            body()
        }
        if (time > period * 1000) DriverStation.reportWarning("Periodic loop went over expected time. " +
                "Got: ${time / 1000}ms, expected: <${period}ms", true)
        delay(Utility.getFPGATime() % (period * 1000), TimeUnit.NANOSECONDS)
    }
}

suspend fun suspendUntil(pollingRate: Int = 20, condition: suspend () -> Boolean) {
    while (!condition()) delay(pollingRate.toLong(), TimeUnit.MILLISECONDS)
}
