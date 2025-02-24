package org.team2471.frc.lib.sensors.canCoder

import com.ctre.phoenix6.configs.CANcoderConfiguration
import com.ctre.phoenix6.hardware.CANcoder
import com.ctre.phoenix6.signals.SensorDirectionValue
import org.team2471.frc.lib.units.Angle
import org.team2471.frc.lib.units.degrees

class LoggedCANCoderReal(id: Int, canbus: String? = ""): LoggedCANCoderIO {
    private val canCoder = CANcoder(id, canbus)
    override var simAngleSupplier: () -> Angle = {0.0.degrees}
    private val config = CANcoderConfiguration()
    private var configUnsaved = true
//    private var inverted = false

    override fun updateInputs(inputs: LoggedCANCoderIO.CANCoderIOInputs) {
        inputs.position = canCoder.position.valueAsDouble// * if (inverted) -1.0 else 1.0
        inputs.absolutePosition = canCoder.absolutePosition.valueAsDouble// * if (inverted) -1.0 else 1.0
        inputs.velocity = canCoder.velocity.valueAsDouble// * if (inverted) -1.0 else 1.0

        applyConfigIfChanged()
    }

    init {
        applyConfig()
    }

    override fun setMagnetOffset(offset: Double) {
        config.MagnetSensor.MagnetOffset = offset
        configUnsaved = true
    }

    override fun setInverted(invert: Boolean) {
        if (invert) {
            config.MagnetSensor.SensorDirection = SensorDirectionValue.Clockwise_Positive
        } else {
            config.MagnetSensor.SensorDirection = SensorDirectionValue.CounterClockwise_Positive
        }
        configUnsaved = true
    }

    fun applyConfigIfChanged() = if (configUnsaved) applyConfig() else {}

    fun applyConfig() {
        configUnsaved = false
        canCoder.configurator.apply(config)
    }

}