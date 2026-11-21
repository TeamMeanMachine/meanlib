package org.team2471.frc.lib.hardware.ctre

import com.ctre.phoenix6.configs.CANrangeConfiguration
import com.ctre.phoenix6.hardware.CANrange
import com.ctre.phoenix6.signals.UpdateModeValue


/**
 * Specifies the target range of the Field of View in the X direction. This is the full range of the FOV.
 */
fun CANrangeConfiguration.fovRangeX(fovRangeX: Double): CANrangeConfiguration {
    return this.apply {
        FovParams.FOVRangeX = fovRangeX
    }
}

/**
 * Specifies the target range of the Field of View in the Y direction in degrees. This is the full range of the FOV =.
 */
fun CANrangeConfiguration.fovRangeY(fovRangeY: Double): CANrangeConfiguration {
    return this.apply {
        FovParams.FOVRangeY = fovRangeY
    }
}

/**
 * Specifies the target range of the Field of View in the X and Y direction in degrees. This is the full range of the FOV.
 */
fun CANrangeConfiguration.fovRange(x: Double, y: Double): CANrangeConfiguration {
    return this.apply {
        fovRangeX(x)
        fovRangeY(y)
    }
}

/**
 * The [CANrange] supports short-range and long-range detection at various update frequencies.
 *
 * [UpdateModeValue.LongRangeUserFreq] Uses long-range detection mode and user-specified update frequency.
 *
 * [UpdateModeValue.ShortRangeUserFreq] Uses short-range detection mode for improved detection under high ambient infrared light conditions. Uses user-specified update frequency.
 *
 * [UpdateModeValue.ShortRange100Hz] Updates distance/proximity at 100hz using short-range detection mode.
 */
fun CANrangeConfiguration.updateMode(updateMode: UpdateModeValue): CANrangeConfiguration {
    return this.apply {
        ToFParams.UpdateMode = updateMode
    }
}

/**
 * Rate at which the CANrange will take measurements. A lower frequency may provide more stable readings but will reduce the data rate of the sensor.
 */
fun CANrangeConfiguration.updateFrequency(updateFrequencyHz: Double): CANrangeConfiguration {
    return this.apply {
        ToFParams.UpdateFrequency = updateFrequencyHz
    }
}

/**
 * Applies a factory default configuration to the [CANrange].
 *
 * @param modifications optionally provide a block to modify the configuration before it gets sent to the device.
 */
fun CANrange.applyConfiguration(modifications: CANrangeConfiguration.() -> Unit = {}) {
    this.configurator.apply(CANrangeConfiguration().apply(modifications))
}

/**
 * Modifies the current device configuration and applies to the [CANrange].
 *
 * @param overrides provide a block to modify the configuration before it gets sent to the device.
 */
fun CANrange.modifyConfiguration(overrides: CANrangeConfiguration.() -> Unit) {
    val oldConfiguration = CANrangeConfiguration()
    this.configurator.refresh(oldConfiguration)
    this.configurator.apply(oldConfiguration.apply(overrides))
}