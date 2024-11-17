package org.team2471.frc.lib.sensors.canCoder

import com.ctre.phoenix6.configs.CANcoderConfiguration
import com.ctre.phoenix6.hardware.CANcoder
import com.ctre.phoenix6.signals.SensorDirectionValue
import org.team2471.frc.lib.units.Angle
import org.team2471.frc.lib.units.degrees

class LoggedCANCoderReal(id: Int, canbus: String? = ""): LoggedCANCoderIO {
    private val canCoder = CANcoder(id, canbus)
    private val config  = CANcoderConfiguration()
    private var appliedConfig = CANcoderConfiguration()
    private val configUnsaved: Boolean get() = config.serialize() != appliedConfig.serialize()
    override var simAngleSupplier: () -> Angle = {0.0.degrees}

    override fun updateInputs(inputs: LoggedCANCoderIO.CANCoderIOInputs) {
        inputs.position = canCoder.position.value
        inputs.velocity = canCoder.velocity.value
        applyConfigIfChanged()
    }

    override fun setMagnetSensorOffset(offset: Double) {
        config.MagnetSensor.MagnetOffset = offset
    }

    override fun setInverted(invert: Boolean) {
        config.MagnetSensor.SensorDirection =
            if (invert) SensorDirectionValue.CounterClockwise_Positive
            else SensorDirectionValue.Clockwise_Positive
    }

    override fun setPosition(position: Double) {
        canCoder.setPosition(position)
    }

    private fun applyConfigIfChanged() = if (configUnsaved) applyConfig() else {}

    private fun applyConfig() {
        canCoder.configurator.apply(config)
        appliedConfig = config
    }
}