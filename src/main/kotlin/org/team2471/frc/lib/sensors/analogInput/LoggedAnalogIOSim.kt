package org.team2471.frc.lib.sensors.analogInput

class LoggedAnalogIOSim(val simTicksSupplier: () -> Int): LoggedAnalogIO {
    override fun updateInputs(inputs: LoggedAnalogIO.AnalogIOInputs) {
        inputs.ticks = simTicksSupplier()
    }
}