[meanlib](../index.md) / [org.team2471.frc.lib.motion.following](index.md) / [hybridDrive](./hybrid-drive.md)

# hybridDrive

`fun `[`ArcadeDrive`](-arcade-drive/index.md)`.hybridDrive(throttle: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`, softTurn: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`, hardTurn: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)

Allows for teleoperated hybrid drive of the robot, with optional heading correction and turning
correction if specified in the [ArcadeParameters](../org.team2471.frc.lib.motion_profiling.following/-arcade-parameters/index.md).

### Parameters

`throttle` - the forward percent speed to drive at

`softTurn` - an amount of turn proportional to the [throttle](hybrid-drive.md#org.team2471.frc.lib.motion.following$hybridDrive(org.team2471.frc.lib.motion.following.ArcadeDrive, kotlin.Double, kotlin.Double, kotlin.Double)/throttle)

`hardTurn` - a raw turn value, added to the left output and subtracted from the right output