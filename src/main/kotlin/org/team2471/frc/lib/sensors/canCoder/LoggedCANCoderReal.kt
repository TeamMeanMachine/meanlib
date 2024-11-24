package org.team2471.frc.lib.sensors.canCoder

import com.ctre.phoenix6.hardware.CANcoder
import org.team2471.frc.lib.units.Angle
import org.team2471.frc.lib.units.degrees

class LoggedCANCoderReal(id: Int, canbus: String? = ""): LoggedCANCoderIO {
    private val canCoder = CANcoder(id, canbus)
    override var simAngleSupplier: () -> Angle = {0.0.degrees}
    private var inverted = false

    override fun updateInputs(inputs: LoggedCANCoderIO.CANCoderIOInputs) {
        inputs.position = canCoder.position.value * if (inverted) -1.0 else 1.0
        inputs.velocity = canCoder.velocity.value * if (inverted) -1.0 else 1.0
    }

    override fun setInverted(invert: Boolean) {
        inverted = invert
    }

    override fun setPosition(position: Double) {
        canCoder.setPosition(position)
    }
}