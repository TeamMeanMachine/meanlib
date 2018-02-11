package org.team2471.frc.lib.control

import edu.wpi.first.wpilibj.SendableBase
import edu.wpi.first.wpilibj.smartdashboard.SendableBuilder
import kotlinx.coroutines.experimental.launch
import org.team2471.frc.lib.control.experimental.periodic
import org.team2471.frc.lib.control.experimental.suspendUntil

class PIDController(
        @Volatile var p: Double,
        @Volatile var i: Double,
        @Volatile var d: Double,
        @Volatile var f: Double,
        input: () -> Double,
        private val output: (Double) -> Unit,
        period: Int = 20) : SendableBase() {

    @Volatile
    var setpoint = input()
    @Volatile
    var isEnabled = false
        set(value) {
            if (!value && field) output(0.0)
            field = value
        }
    @Volatile
    var iZone: Double? = null

    init {
        var accum = 0.0
        var previousError: Double? = null

        launch {
            periodic(period) {
                suspendUntil { isEnabled }

                val setpoint = setpoint
                val error = input() - setpoint

                // P gain
                var outputValue = error * p

                // I gain
                outputValue += accum * i
                accum += error

                // D gain
                outputValue += ((previousError ?: error) - error) * d
                previousError = error

                // F gain
                outputValue += setpoint * f

                // I zone
                iZone?.run {
                    if (Math.abs(error) > this) {
                        accum = 0.0
                    }
                }

                output(outputValue)
                println("Error: $error")
            }
        }
    }

    override fun initSendable(builder: SendableBuilder) {
        builder.setSmartDashboardType("PIDController")
        builder.setSafeState { isEnabled = false }
        builder.addDoubleProperty("p", { p }, { p = it })
        builder.addDoubleProperty("i", { i }, { i = it })
        builder.addDoubleProperty("d", { d }, { d = it })
        builder.addDoubleProperty("f", { f }, { f = it })
        builder.addDoubleProperty("setpoint", { setpoint }, { setpoint = it })
        builder.addBooleanProperty("enabled", { isEnabled }, { isEnabled = it })
    }
}