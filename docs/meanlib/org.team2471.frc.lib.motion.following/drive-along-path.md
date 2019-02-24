[meanlib](../index.md) / [org.team2471.frc.lib.motion.following](index.md) / [driveAlongPath](./drive-along-path.md)

# driveAlongPath

`suspend fun <T> `[`T`](drive-along-path.md#T)`.driveAlongPath(path: `[`Path2D`](../org.team2471.frc.lib.motion_profiling/-path2-d/index.md)`, extraTime: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)` = 0.0): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)` where T : `[`ArcadeDrive`](-arcade-drive/index.md)`, T : `[`Subsystem`](../org.team2471.frc.lib.framework/-subsystem/index.md)

Follows a specified [path](drive-along-path.md#org.team2471.frc.lib.motion.following$driveAlongPath(org.team2471.frc.lib.motion.following.driveAlongPath.T, org.team2471.frc.lib.motion_profiling.Path2D, kotlin.Double)/path) using the robot's [ArcadeParameters](../org.team2471.frc.lib.motion_profiling.following/-arcade-parameters/index.md).

### Parameters

`path` - the [Path2D](../org.team2471.frc.lib.motion_profiling/-path2-d/index.md) to follow

`extraTime` - the amount of extra time to wait for minor corrections to the path after its completion`suspend fun `[`SwerveDrive`](-swerve-drive/index.md)`.driveAlongPath(path: `[`Path2D`](../org.team2471.frc.lib.motion_profiling/-path2-d/index.md)`, extraTime: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)` = 0.0): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)