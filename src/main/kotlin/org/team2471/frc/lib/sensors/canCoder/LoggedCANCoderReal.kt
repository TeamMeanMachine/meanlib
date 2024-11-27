package org.team2471.frc.lib.sensors.canCoder

import com.ctre.phoenix6.configs.CANcoderConfiguration
import com.ctre.phoenix6.configs.CustomParamsConfigs
import com.ctre.phoenix6.configs.MagnetSensorConfigs
import com.ctre.phoenix6.hardware.CANcoder
import com.ctre.phoenix6.signals.AbsoluteSensorRangeValue
import com.ctre.phoenix6.signals.SensorDirectionValue
import org.team2471.frc.lib.units.Angle
import org.team2471.frc.lib.units.degrees
import kotlin.math.IEEErem

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
//
//        val absolutePosition = canCoder.absolutePosition.value
//        println("curr pose $absolutePosition  ${canCoder.position.value}")
//
//        val config = MagnetSensorConfigs()
//
//        canCoder.configurator.refresh(config)
//
//        println("old magnetOffset ${config.MagnetOffset}")
//
//        val magnetOffset = (absolutePosition + config.MagnetOffset)
//        config.MagnetOffset = magnetOffset
//
//        println("new magnetOffset ${config.MagnetOffset}")
//
//
//
//        canCoder.configurator.apply(config)
//
//        println("finished position ${canCoder.absolutePosition.value}  ${canCoder.position.value}")
//
//        canCoder.setPosition(canCoder.absolutePosition.value)
//
//        println("again finished position ${canCoder.absolutePosition.value}  ${canCoder.position.value}")

        canCoder.configurator.setPosition(position)
    }
}