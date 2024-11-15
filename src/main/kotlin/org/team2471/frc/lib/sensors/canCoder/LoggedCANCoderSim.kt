package org.team2471.frc.lib.sensors.canCoder

import org.team2471.frc.lib.util.Timer

class LoggedCANCoderSim(val simRotationSupplier: () -> Double): LoggedCANCoderIO {
    private val timer = Timer()
    private var prevTime = 0.0
    private var prevPosition = 0.0

    init {
        timer.start()
    }

    override fun updateInputs(inputs: LoggedCANCoderIO.CANCoderIOInputs) {
        val position = simRotationSupplier()
        val dt = timer.get() - prevTime

        inputs.position = position
        inputs.velocity = (position - prevPosition) / dt

        prevPosition = position
        prevTime += dt
    }
}