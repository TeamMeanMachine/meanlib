[meanlib](../index.md) / [org.team2471.frc.lib.motion.following](./index.md)

## Package org.team2471.frc.lib.motion.following

### Types

| [ArcadeDrive](-arcade-drive/index.md) | `interface ArcadeDrive` |
| [ArcadePath](-arcade-path/index.md) | `class ArcadePath` |
| [SwerveDrive](-swerve-drive/index.md) | `interface SwerveDrive` |

### Functions

| [drive](drive.md) | `fun `[`SwerveDrive`](-swerve-drive/index.md)`.drive(translation: `[`Vector2`](../org.team2471.frc.lib.math/-vector2/index.md)`, turn: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`, fieldCentric: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = true): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [driveAlongPath](drive-along-path.md) | `suspend fun <T> `[`T`](drive-along-path.md#T)`.driveAlongPath(path: `[`Path2D`](../org.team2471.frc.lib.motion_profiling/-path2-d/index.md)`, extraTime: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)` = 0.0): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)` where T : `[`ArcadeDrive`](-arcade-drive/index.md)`, T : `[`Subsystem`](../org.team2471.frc.lib.framework/-subsystem/index.md)<br>Follows a specified [path](drive-along-path.md#org.team2471.frc.lib.motion.following$driveAlongPath(org.team2471.frc.lib.motion.following.driveAlongPath.T, org.team2471.frc.lib.motion_profiling.Path2D, kotlin.Double)/path) using the robot's [ArcadeParameters](../org.team2471.frc.lib.motion_profiling.following/-arcade-parameters/index.md).`suspend fun `[`SwerveDrive`](-swerve-drive/index.md)`.driveAlongPath(path: `[`Path2D`](../org.team2471.frc.lib.motion_profiling/-path2-d/index.md)`, extraTime: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)` = 0.0): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [hybridDrive](hybrid-drive.md) | `fun `[`ArcadeDrive`](-arcade-drive/index.md)`.hybridDrive(throttle: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`, softTurn: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`, hardTurn: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Allows for teleoperated hybrid drive of the robot, with optional heading correction and turning correction if specified in the [ArcadeParameters](../org.team2471.frc.lib.motion_profiling.following/-arcade-parameters/index.md). |
| [recordOdometry](record-odometry.md) | `fun `[`SwerveDrive`](-swerve-drive/index.md)`.recordOdometry(): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>`fun `[`Module`](-swerve-drive/-module/index.md)`.recordOdometry(heading: `[`Angle`](../org.team2471.frc.lib.units/-angle/index.md)`): `[`Vector2`](../org.team2471.frc.lib.math/-vector2/index.md) |
| [steerToAngle](steer-to-angle.md) | `suspend fun `[`Module`](-swerve-drive/-module/index.md)`.steerToAngle(angle: `[`Angle`](../org.team2471.frc.lib.units/-angle/index.md)`, tolerance: `[`Angle`](../org.team2471.frc.lib.units/-angle/index.md)` = 2.degrees): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [stop](stop.md) | `fun `[`SwerveDrive`](-swerve-drive/index.md)`.stop(): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [zeroEncoders](zero-encoders.md) | `fun `[`SwerveDrive`](-swerve-drive/index.md)`.zeroEncoders(): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |

