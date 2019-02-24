[meanlib](../../index.md) / [org.team2471.frc.lib.motion.following](../index.md) / [SwerveDrive](./index.md)

# SwerveDrive

`interface SwerveDrive`

### Types

| Name | Summary |
|---|---|
| [Module](-module/index.md) | `interface Module` |

### Properties

| Name | Summary |
|---|---|
| [backLeftModule](back-left-module.md) | `abstract val backLeftModule: `[`Module`](-module/index.md) |
| [backRightModule](back-right-module.md) | `abstract val backRightModule: `[`Module`](-module/index.md) |
| [frontLeftModule](front-left-module.md) | `abstract val frontLeftModule: `[`Module`](-module/index.md) |
| [frontRightModule](front-right-module.md) | `abstract val frontRightModule: `[`Module`](-module/index.md) |
| [heading](heading.md) | `abstract val heading: `[`Angle`](../../org.team2471.frc.lib.units/-angle/index.md) |
| [headingRate](heading-rate.md) | `abstract val headingRate: `[`AngularVelocity`](../../org.team2471.frc.lib.units/-angular-velocity/index.md) |
| [parameters](parameters.md) | `abstract val parameters: `[`SwerveParameters`](../../org.team2471.frc.lib.motion_profiling.following/-swerve-parameters/index.md) |
| [position](position.md) | `abstract var position: `[`Vector2`](../../org.team2471.frc.lib.math/-vector2/index.md) |
| [prevPathPosition](prev-path-position.md) | `abstract var prevPathPosition: `[`Vector2`](../../org.team2471.frc.lib.math/-vector2/index.md) |
| [prevPosition](prev-position.md) | `abstract var prevPosition: `[`Vector2`](../../org.team2471.frc.lib.math/-vector2/index.md) |
| [prevTime](prev-time.md) | `abstract var prevTime: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html) |
| [velocity](velocity.md) | `abstract var velocity: `[`Vector2`](../../org.team2471.frc.lib.math/-vector2/index.md) |

### Functions

| Name | Summary |
|---|---|
| [startFollowing](start-following.md) | `open fun startFollowing(): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [stopFollowing](stop-following.md) | `open fun stopFollowing(): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |

### Extension Functions

| Name | Summary |
|---|---|
| [drive](../drive.md) | `fun `[`SwerveDrive`](./index.md)`.drive(translation: `[`Vector2`](../../org.team2471.frc.lib.math/-vector2/index.md)`, turn: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`, fieldCentric: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = true): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [driveAlongPath](../drive-along-path.md) | `suspend fun `[`SwerveDrive`](./index.md)`.driveAlongPath(path: `[`Path2D`](../../org.team2471.frc.lib.motion_profiling/-path2-d/index.md)`, extraTime: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)` = 0.0): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [recordOdometry](../record-odometry.md) | `fun `[`SwerveDrive`](./index.md)`.recordOdometry(): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [stop](../stop.md) | `fun `[`SwerveDrive`](./index.md)`.stop(): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [zeroEncoders](../zero-encoders.md) | `fun `[`SwerveDrive`](./index.md)`.zeroEncoders(): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
