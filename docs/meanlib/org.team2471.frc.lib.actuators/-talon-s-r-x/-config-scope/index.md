[meanlib](../../../index.md) / [org.team2471.frc.lib.actuators](../../index.md) / [TalonSRX](../index.md) / [ConfigScope](./index.md)

# ConfigScope

`inner class ConfigScope`

### Types

| [PIDConfigScope](-p-i-d-config-scope/index.md) | `inner class PIDConfigScope` |

### Constructors

| [&lt;init&gt;](-init-.md) | `ConfigScope(timeoutMs: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)`)` |

### Properties

| [ctreFollowers](ctre-followers.md) | `val ctreFollowers: `[`Array`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-array/index.html)`<TalonSRX>` |
| [ctreTalon](ctre-talon.md) | `val ctreTalon: TalonSRX` |
| [feedbackCoefficient](feedback-coefficient.md) | `var feedbackCoefficient: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html) |

### Functions

| [brakeMode](brake-mode.md) | `fun brakeMode(): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [closedLoopRamp](closed-loop-ramp.md) | `fun closedLoopRamp(secondsToFull: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [coastMode](coast-mode.md) | `fun coastMode(): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [currentLimit](current-limit.md) | `fun currentLimit(continuousLimit: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)`, peakLimit: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)`, peakDuration: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [inverted](inverted.md) | `fun inverted(inverted: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [nominalOutputRange](nominal-output-range.md) | `fun nominalOutputRange(range: `[`DoubleRange`](../../../org.team2471.frc.lib.math/-double-range.md)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [openLoopRamp](open-loop-ramp.md) | `fun openLoopRamp(secondsToFull: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [peakOutputRange](peak-output-range.md) | `fun peakOutputRange(range: `[`DoubleRange`](../../../org.team2471.frc.lib.math/-double-range.md)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [pid](pid.md) | `fun pid(slot: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)`, body: `[`PIDConfigScope`](-p-i-d-config-scope/index.md)`.() -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [pidSlot](pid-slot.md) | `fun pidSlot(slot: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [sensorPhase](sensor-phase.md) | `fun sensorPhase(inverted: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |

