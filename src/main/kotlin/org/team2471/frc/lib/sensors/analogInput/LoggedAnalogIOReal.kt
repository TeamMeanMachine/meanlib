package org.team2471.frc.lib.sensors.analogInput

import edu.wpi.first.wpilibj.AnalogInput

class LoggedAnalogIOReal(id: Int): LoggedAnalogIO {
    private val analogInput = AnalogInput(id)

    override fun updateInputs(inputs: LoggedAnalogIO.AnalogIOInputs) {
        inputs.ticks = analogInput.value
        inputs.voltage = analogInput.voltage
    }
}