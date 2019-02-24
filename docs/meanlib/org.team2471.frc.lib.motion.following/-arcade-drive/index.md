[meanlib](../../index.md) / [org.team2471.frc.lib.motion.following](../index.md) / [ArcadeDrive](./index.md)

# ArcadeDrive

`interface ArcadeDrive`

### Properties

| Name | Summary |
|---|---|
| [heading](heading.md) | `abstract val heading: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html) |
| [headingRate](heading-rate.md) | `abstract val headingRate: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html) |
| [parameters](parameters.md) | `abstract val parameters: `[`ArcadeParameters`](../../org.team2471.frc.lib.motion_profiling.following/-arcade-parameters/index.md) |

### Functions

| Name | Summary |
|---|---|
| [driveClosedLoop](drive-closed-loop.md) | `abstract fun driveClosedLoop(leftDistance: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`, leftFeedForward: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`, rightDistance: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`, rightFeedForward: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [driveOpenLoop](drive-open-loop.md) | `abstract fun driveOpenLoop(leftPower: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`, rightPower: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [startFollowing](start-following.md) | `open fun startFollowing(): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [stop](stop.md) | `open fun stop(): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [stopFollowing](stop-following.md) | `open fun stopFollowing(): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |

### Extension Functions

| Name | Summary |
|---|---|
| [hybridDrive](../hybrid-drive.md) | `fun `[`ArcadeDrive`](./index.md)`.hybridDrive(throttle: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`, softTurn: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`, hardTurn: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Allows for teleoperated hybrid drive of the robot, with optional heading correction and turning correction if specified in the [ArcadeParameters](../../org.team2471.frc.lib.motion_profiling.following/-arcade-parameters/index.md). |
