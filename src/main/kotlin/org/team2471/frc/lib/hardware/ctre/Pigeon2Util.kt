package org.team2471.frc.lib.hardware.ctre

import com.ctre.phoenix6.configs.Pigeon2Configuration
import com.ctre.phoenix6.hardware.Pigeon2
import org.team2471.frc.lib.units.asDegrees
import org.team2471.frc.lib.units.wrap
import org.wpilib.units.measure.Angle

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

@IgnorableReturnValue
fun Pigeon2Configuration.mountPosePitch(pitch: Angle): Pigeon2Configuration {
    this.MountPose.withMountPosePitch(pitch)
    return this
}

@IgnorableReturnValue
fun Pigeon2Configuration.mountPoseRoll(roll: Angle): Pigeon2Configuration {
    this.MountPose.withMountPoseRoll(roll)
    return this
}

@IgnorableReturnValue
fun Pigeon2Configuration.mountPoseYaw(yaw: Angle): Pigeon2Configuration {
    this.MountPose.withMountPoseYaw(yaw)
    return this
}

@IgnorableReturnValue
fun Pigeon2Configuration.mountPose(pitch: Angle, roll: Angle, yaw: Angle): Pigeon2Configuration {
    this.mountPosePitch(pitch)
    this.mountPoseRoll(roll)
    this.mountPoseYaw(yaw)
    return this
}

/**
 * The gyro scalar component for the X axis. In [Angle] per rotation units.
 */
@IgnorableReturnValue
fun Pigeon2Configuration.gyroScalarX(x: Angle): Pigeon2Configuration {
    this.GyroTrim.withGyroScalarX(x.wrap().asDegrees)
    return this
}

/**
 * The gyro scalar component for the Y axis. In [Angle] per rotation units.
 */
@IgnorableReturnValue
fun Pigeon2Configuration.gyroScalarY(y: Angle): Pigeon2Configuration {
    this.GyroTrim.withGyroScalarY(y.wrap().asDegrees)
    return this
}

/**
 * The gyro scalar component for the Z axis. In [Angle] per rotation units.
 */
@IgnorableReturnValue
fun Pigeon2Configuration.gyroScalarZ(z: Angle): Pigeon2Configuration {
    this.GyroTrim.withGyroScalarZ(z.wrap().asDegrees)
    return this
}