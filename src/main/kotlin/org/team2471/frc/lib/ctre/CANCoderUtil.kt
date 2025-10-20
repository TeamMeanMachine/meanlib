package org.team2471.frc.lib.ctre

import com.ctre.phoenix6.configs.CANcoderConfiguration
import com.ctre.phoenix6.hardware.CANcoder
import com.ctre.phoenix6.signals.SensorDirectionValue
import edu.wpi.first.units.measure.Angle
import org.team2471.frc.lib.util.isReal
import org.team2471.frc.lib.util.isSim
import org.team2471.frc.lib.units.asRotations
import org.team2471.frc.lib.units.degrees
import org.team2471.frc.lib.units.rotations

/** Grabs the MagnetOffset from the [CANcoder]. */
fun CANcoder.getMagnetSensorOffset(): Angle {
    if (!this.isConnected || isSim) return 0.0.degrees
    val initialConfigs = CANcoderConfiguration()
    PhoenixUtil.tryUntilOk(5) { this.configurator.refresh(initialConfigs) }

    return initialConfigs.MagnetSensor.MagnetOffset.rotations
}

/**
 * Applies the MagnetOffset config to the [CANcoder] while not changing other configuration values.
 * This is probably a backing call
 */
fun CANcoder.setMagnetSensorOffset(offset: Angle) {
    if (isReal) {
        val initialConfigs = CANcoderConfiguration()
        PhoenixUtil.tryUntilOk(5) { this.configurator.refresh(initialConfigs) }

        initialConfigs.MagnetSensor.MagnetOffset = offset.asRotations

        this.configurator.apply(initialConfigs, 0.0)
    }
}

/**
 * Sets the [CANcoder]s MagnetOffset config so its current position equals the specified angle.
 * This is probably a backing call
 */
fun CANcoder.setCANCoderAngle(angle: Angle): Angle {
    val initialPosition = this.absolutePosition.valueAsDouble.rotations
    val initialOffset = getMagnetSensorOffset()
    println("initial position $initialPosition initial offset $initialOffset")

    val rawPosition = initialPosition - initialOffset
    val newOffset = (angle - rawPosition)
    println("rawPosition $rawPosition new offset $newOffset")

    this.setMagnetSensorOffset(newOffset)
    return newOffset
}

fun CANcoderConfiguration.inverted(inverted: Boolean): CANcoderConfiguration {
    return this.apply {
        MagnetSensor.SensorDirection = if (inverted) SensorDirectionValue.Clockwise_Positive else SensorDirectionValue.CounterClockwise_Positive
    }
}

fun CANcoderConfiguration.magnetSensorOffset(magnetOffsetRotations: Double): CANcoderConfiguration {
    return this.apply {
        MagnetSensor.MagnetOffset = magnetOffsetRotations
    }
}

/**
 * Applies a factory default configuration to the [CANcoder].
 *
 * @param modifications optionally provide a block to modify the configuration before it gets sent to the device.
 */
fun CANcoder.applyConfiguration(modifications: CANcoderConfiguration.() -> Unit = {}) {
    this.configurator.apply(CANcoderConfiguration().apply(modifications))
}

/**
 * Modifies the current device configuration and applies to the [CANcoder].
 *
 * @param overrides provide a block to modify the configuration before it gets sent to the device.
 */
fun CANcoder.modifyConfiguration(overrides: CANcoderConfiguration.() -> Unit) {
    val oldConfiguration = CANcoderConfiguration()
    this.configurator.refresh(oldConfiguration)
    this.configurator.apply(oldConfiguration.apply(overrides))
}