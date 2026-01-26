package org.team2471.frc.lib.ctre

import com.ctre.phoenix6.configs.Pigeon2Configuration
import com.ctre.phoenix6.hardware.Pigeon2

/**
 * Applies a factory default configuration to the [Pigeon2].
 *
 * @param modifications optionally provide a block to modify the configuration before it gets sent to the pigeon.
 *
 * @see modifyConfiguration
 */
fun Pigeon2.applyConfiguration(modifications: Pigeon2Configuration.() -> Unit = {}) {
    // Create a factory default configuration, apply modifications, then apply to the pigeon.
    this.configurator.apply(Pigeon2Configuration().apply { modifications() })
}

/**
 * Modifies the configuration currently on the pigeon.
 *
 * @param overrides provide a block to modify the configuration before it gets sent to the device.
 *
 * @see applyConfiguration
 */
fun Pigeon2.modifyConfiguration(overrides: Pigeon2Configuration.() -> Unit) {
    // Get the current gyro configuration, apply modifications, then apply to the pigeon.
    val oldConfiguration = Pigeon2Configuration()
    this.configurator.refresh(oldConfiguration) // Get pigeon configuration parameters
    this.configurator.apply(oldConfiguration.apply(overrides)) // Apply overrides to the config and send config to pigeon.
}