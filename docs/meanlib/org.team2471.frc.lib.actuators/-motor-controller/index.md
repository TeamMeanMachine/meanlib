[meanlib](../../index.md) / [org.team2471.frc.lib.actuators](../index.md) / [MotorController](./index.md)

# MotorController

`class MotorController`

A single motor controller or combination of motor controllers which follow a primary device.

### Parameters

`deviceId` - the [MotorControllerID](../-motor-controller-i-d.md) of the primary, "master" motor controller

`followerIds` - optional [MotorControllerID](../-motor-controller-i-d.md)s of motor controllers which should follow the primary

### Types

| Name | Summary |
|---|---|
| [ConfigScope](-config-scope/index.md) | `inner class ConfigScope` |

### Constructors

| Name | Summary |
|---|---|
| [&lt;init&gt;](-init-.md) | `MotorController(deviceId: `[`MotorControllerID`](../-motor-controller-i-d.md)`, vararg followerIds: `[`MotorControllerID`](../-motor-controller-i-d.md)`)`<br>A single motor controller or combination of motor controllers which follow a primary device. |

### Properties

| Name | Summary |
|---|---|
| [closedLoopError](closed-loop-error.md) | `val closedLoopError: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)<br>The closed loop error (in units specified by [ConfigScope.feedbackCoefficient](-config-scope/feedback-coefficient.md)). |
| [current](current.md) | `val current: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)<br>The current being drawn by the [MotorController](./index.md). Note that this will only work if the [MotorController](./index.md) is a Talon SRX. Attempts to use this method on non-Talons will result in an [IllegalStateException](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-illegal-state-exception/index.html). |
| [output](output.md) | `val output: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)<br>The output percent, from 0 to 1. |
| [position](position.md) | `var position: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)<br>The position of the selected sensor (in units specified by [ConfigScope.feedbackCoefficient](-config-scope/feedback-coefficient.md)). |
| [rawPosition](raw-position.md) | `val rawPosition: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)<br>The raw position of the selected sensor in encoder ticks. |
| [velocity](velocity.md) | `val velocity: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)<br>The velocity calculated from the selected sensor (in units specified by [ConfigScope.feedbackCoefficient](-config-scope/feedback-coefficient.md) per second). |

### Functions

| Name | Summary |
|---|---|
| [config](config.md) | `fun config(timeoutMs: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)` = Int.MAX_VALUE, body: `[`ConfigScope`](-config-scope/index.md)`.() -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)`): `[`MotorController`](./index.md)<br>Configures the [MotorController](./index.md) with instructions specified in the [body](config.md#org.team2471.frc.lib.actuators.MotorController$config(kotlin.Int, kotlin.Function1((org.team2471.frc.lib.actuators.MotorController.ConfigScope, kotlin.Unit)))/body). |
| [setCurrentSetpoint](set-current-setpoint.md) | `fun setCurrentSetpoint(current: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Sets the closed-loop current setpoint.`fun setCurrentSetpoint(current: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`, feedForward: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Sets the closed-loop current setpoint with a specified [feedForward](set-current-setpoint.md#org.team2471.frc.lib.actuators.MotorController$setCurrentSetpoint(kotlin.Double, kotlin.Double)/feedForward) value. |
| [setMotionMagicSetpoint](set-motion-magic-setpoint.md) | `fun setMotionMagicSetpoint(position: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Sets the closed-loop Motion Magic position setpoint.`fun setMotionMagicSetpoint(position: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`, feedForward: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Sets the closed-loop Motion Magic position setpoint with a specified [feedForward](set-motion-magic-setpoint.md#org.team2471.frc.lib.actuators.MotorController$setMotionMagicSetpoint(kotlin.Double, kotlin.Double)/feedForward) value. |
| [setPercentOutput](set-percent-output.md) | `fun setPercentOutput(percent: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Sets the percent output. |
| [setPositionSetpoint](set-position-setpoint.md) | `fun setPositionSetpoint(position: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Sets the closed-loop position setpoint.`fun setPositionSetpoint(position: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`, feedForward: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Sets the closed-loop position setpoint with a specified [feedForward](set-position-setpoint.md#org.team2471.frc.lib.actuators.MotorController$setPositionSetpoint(kotlin.Double, kotlin.Double)/feedForward) value. |
| [setVelocitySetpoint](set-velocity-setpoint.md) | `fun setVelocitySetpoint(velocity: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Sets the closed-loop velocity setpoint.`fun setVelocitySetpoint(velocity: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`, feedForward: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Sets the closed-loop velocity setpoint with a specified [feedForward](set-velocity-setpoint.md#org.team2471.frc.lib.actuators.MotorController$setVelocitySetpoint(kotlin.Double, kotlin.Double)/feedForward) value. |
| [stop](stop.md) | `fun stop(): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Neutralizes the motor output. |

### Extension Functions

| Name | Summary |
|---|---|
| [smoothDrivePosition](../../org.team2471.frc.lib.testing/smooth-drive-position.md) | `suspend fun `[`MotorController`](./index.md)`.smoothDrivePosition(position: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`, time: `[`Time`](../../org.team2471.frc.lib.units/-time/index.md)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [testAverageAmperage](../../org.team2471.frc.lib.testing/test-average-amperage.md) | `suspend fun `[`MotorController`](./index.md)`.testAverageAmperage(power: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`, rampTime: `[`Time`](../../org.team2471.frc.lib.units/-time/index.md)`, sampleTime: `[`Time`](../../org.team2471.frc.lib.units/-time/index.md)`, numSamples: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)` = 10): `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html) |
