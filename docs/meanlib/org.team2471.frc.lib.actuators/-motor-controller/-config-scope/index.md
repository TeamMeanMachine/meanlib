[meanlib](../../../index.md) / [org.team2471.frc.lib.actuators](../../index.md) / [MotorController](../index.md) / [ConfigScope](./index.md)

# ConfigScope

`inner class ConfigScope`

### Types

| [PIDConfigScope](-p-i-d-config-scope/index.md) | `inner class PIDConfigScope` |

### Constructors

| [&lt;init&gt;](-init-.md) | `ConfigScope(timeoutMs: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)`)` |

### Properties

| [ctreController](ctre-controller.md) | `val ctreController: BaseMotorController`<br>The primary, "master" [MotorController](../index.md). |
| [ctreFollowers](ctre-followers.md) | `val ctreFollowers: `[`Array`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-array/index.html)`<BaseMotorController>`<br>An array of [MotorController](../index.md)s which follow [ctreController](ctre-controller.md). |
| [feedbackCoefficient](feedback-coefficient.md) | `var feedbackCoefficient: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)<br>A coefficient applied to the attached encoder's raw value in order to convert it into a desired unit of measurement. For example, if 7126 encoder ticks equals 1 foot of drive distance on your drivetrain, [feedbackCoefficient](feedback-coefficient.md) should be set to `1.0/7126.0`. |

### Functions

| [brakeMode](brake-mode.md) | `fun brakeMode(): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Enables brake mode. |
| [closedLoopRamp](closed-loop-ramp.md) | `fun closedLoopRamp(secondsToFull: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Sets the amount of time required for closed loop control of the [MotorController](../index.md) to go from neutral output to full power. |
| [coastMode](coast-mode.md) | `fun coastMode(): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Enables coast mode. |
| [currentLimit](current-limit.md) | `fun currentLimit(continuousLimit: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)`, peakLimit: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)`, peakDuration: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Limits the current to a [continuousLimit](current-limit.md#org.team2471.frc.lib.actuators.MotorController.ConfigScope$currentLimit(kotlin.Int, kotlin.Int, kotlin.Int)/continuousLimit), [peakLimit](current-limit.md#org.team2471.frc.lib.actuators.MotorController.ConfigScope$currentLimit(kotlin.Int, kotlin.Int, kotlin.Int)/peakLimit) and [peakDuration](current-limit.md#org.team2471.frc.lib.actuators.MotorController.ConfigScope$currentLimit(kotlin.Int, kotlin.Int, kotlin.Int)/peakDuration). |
| [encoderContinuous](encoder-continuous.md) | `fun encoderContinuous(continuous: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Sets whether the feedback of the encoder is continuous (i.e. should not wrap back to 0 after a full revolution). |
| [encoderType](encoder-type.md) | `fun encoderType(feedbackDevice: FeedbackDevice): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Sets the [FeedbackDevice](#) to use for closed loop and sensor feedback. |
| [inverted](inverted.md) | `fun inverted(inverted: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Sets whether the motor should be inverted. |
| [motionMagic](motion-magic.md) | `fun motionMagic(acceleration: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`, cruisingVelocity: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Sets the [acceleration](motion-magic.md#org.team2471.frc.lib.actuators.MotorController.ConfigScope$motionMagic(kotlin.Double, kotlin.Double)/acceleration) and [cruisingVelocity](motion-magic.md#org.team2471.frc.lib.actuators.MotorController.ConfigScope$motionMagic(kotlin.Double, kotlin.Double)/cruisingVelocity) for use in Motion Magic closed loop control. |
| [nominalOutputRange](nominal-output-range.md) | `fun nominalOutputRange(range: `[`DoubleRange`](../../../org.team2471.frc.lib.math/-double-range.md)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Sets the minimum allowable output of the [MotorController](../index.md). |
| [openLoopRamp](open-loop-ramp.md) | `fun openLoopRamp(secondsToFull: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Sets the amount of time required for open loop control of the [MotorController](../index.md) to go from neutral output to full power. |
| [peakOutputRange](peak-output-range.md) | `fun peakOutputRange(range: `[`DoubleRange`](../../../org.team2471.frc.lib.math/-double-range.md)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Sets the maximum allowable output of the [MotorController](../index.md). |
| [pid](pid.md) | `fun pid(slot: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)` = 0, body: `[`PIDConfigScope`](-p-i-d-config-scope/index.md)`.() -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [pidSlot](pid-slot.md) | `fun pidSlot(slot: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Selects a specific PID slot. |
| [rawOffset](raw-offset.md) | `fun rawOffset(ticks: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Sets a raw offset, in encoder ticks, to the selected sensor. |
| [sensorPhase](sensor-phase.md) | `fun sensorPhase(inverted: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Sets the phase of the sensor. |

