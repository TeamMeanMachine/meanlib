package org.team2471.frc.lib.testing

import org.team2471.frc.lib.actuators.MotorController
import org.team2471.frc.lib.coroutines.delay
import org.team2471.frc.lib.units.Time

suspend fun MotorController.testAverageAmperage(
    power: Double,
    rampTime: Time,
    sampleTime: Time,
    numSamples: Int = 10
): Double {
    var accum = 0.0
    try {
        config { openLoopRamp(rampTime.asSeconds / power) }
        setPercentOutput(power)
        delay(rampTime / power)

        repeat(numSamples) {
            accum += current
            delay(sampleTime / numSamples.toDouble())
        }

        setPercentOutput(0.0)
        delay(rampTime / power)
    } finally {
        config { openLoopRamp(0.0) }
    }

    return accum / numSamples
}
