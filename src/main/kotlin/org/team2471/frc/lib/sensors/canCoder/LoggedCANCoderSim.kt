package org.team2471.frc.lib.sensors.canCoder

import org.team2471.frc.lib.units.Angle
import org.team2471.frc.lib.units.asRotations
import org.team2471.frc.lib.units.rotations
import org.team2471.frc.lib.util.Timer

class LoggedCANCoderSim(override var simAngleSupplier: () -> Angle): LoggedCANCoderIO {
    private val timer = Timer()
    private var prevTime = 0.0
    private var prevPosition = 0.0

    init {
        timer.start()
    }

    override fun updateInputs(inputs: LoggedCANCoderIO.CANCoderIOInputs) {
        val position = simAngleSupplier().asRotations
        val dt = timer.get() - prevTime

        inputs.position = position
        inputs.absolutePosition = position.rotations.wrap().asRotations
        inputs.velocity = (position - prevPosition) / dt

        prevPosition = position
        prevTime += dt
    }
}