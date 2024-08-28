package org.team2471.frc.lib.sensors.AnalogInput

class LoggedAnalogIOSim(val simTicksSupplier: () -> Int?): LoggedAnalogIO {
    override fun updateInputs(inputs: LoggedAnalogIO.AnalogIOInputs) {
        inputs.ticks = simTicksSupplier() ?: 0
    }
}