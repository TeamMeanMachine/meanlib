[meanlib](../../../index.md) / [org.team2471.frc.lib.motion.following](../../index.md) / [SwerveDrive](../index.md) / [Module](./index.md)

# Module

`interface Module`

### Properties

| Name | Summary |
|---|---|
| [angle](angle.md) | `abstract val angle: `[`Angle`](../../../org.team2471.frc.lib.units/-angle/index.md) |
| [currentDistance](current-distance.md) | `abstract val currentDistance: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html) |
| [previousDistance](previous-distance.md) | `abstract var previousDistance: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html) |
| [speed](speed.md) | `abstract val speed: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html) |

### Functions

| Name | Summary |
|---|---|
| [drive](drive.md) | `abstract fun drive(angle: `[`Angle`](../../../org.team2471.frc.lib.units/-angle/index.md)`, power: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [driveWithDistance](drive-with-distance.md) | `abstract fun driveWithDistance(angle: `[`Angle`](../../../org.team2471.frc.lib.units/-angle/index.md)`, distance: `[`Length`](../../../org.team2471.frc.lib.units/-length/index.md)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [stop](stop.md) | `abstract fun stop(): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [zeroEncoder](zero-encoder.md) | `abstract fun zeroEncoder(): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |

### Extension Functions

| Name | Summary |
|---|---|
| [recordOdometry](../../record-odometry.md) | `fun `[`Module`](./index.md)`.recordOdometry(heading: `[`Angle`](../../../org.team2471.frc.lib.units/-angle/index.md)`): `[`Vector2`](../../../org.team2471.frc.lib.math/-vector2/index.md) |
| [steerToAngle](../../steer-to-angle.md) | `suspend fun `[`Module`](./index.md)`.steerToAngle(angle: `[`Angle`](../../../org.team2471.frc.lib.units/-angle/index.md)`, tolerance: `[`Angle`](../../../org.team2471.frc.lib.units/-angle/index.md)` = 2.degrees): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
