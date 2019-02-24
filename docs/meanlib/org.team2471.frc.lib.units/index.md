[meanlib](../index.md) / [org.team2471.frc.lib.units](./index.md)

## Package org.team2471.frc.lib.units

### Types

| Name | Summary |
|---|---|
| [Angle](-angle/index.md) | `class Angle` |
| [AngularAcceleration](-angular-acceleration/index.md) | `class AngularAcceleration` |
| [AngularVelocity](-angular-velocity/index.md) | `class AngularVelocity` |
| [Length](-length/index.md) | `class Length` |
| [LinearAcceleration](-linear-acceleration/index.md) | `class LinearAcceleration` |
| [LinearVelocity](-linear-velocity/index.md) | `class LinearVelocity` |
| [Time](-time/index.md) | `class Time` |

### Extensions for External Classes

| Name | Summary |
|---|---|
| [kotlin.Number](kotlin.-number/index.md) |  |

### Properties

| Name | Summary |
|---|---|
| [asCm](as-cm.md) | `val `[`Length`](-length/index.md)`.asCm: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html) |
| [asDegrees](as-degrees.md) | `val `[`Angle`](-angle/index.md)`.asDegrees: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html) |
| [asFeet](as-feet.md) | `val `[`Length`](-length/index.md)`.asFeet: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html) |
| [asMeters](as-meters.md) | `val `[`Length`](-length/index.md)`.asMeters: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html) |
| [lengthPerDs](length-per-ds.md) | `val `[`LinearAcceleration`](-linear-acceleration/index.md)`.lengthPerDs: `[`LinearVelocity`](-linear-velocity/index.md)<br>`val `[`LinearVelocity`](-linear-velocity/index.md)`.lengthPerDs: `[`Length`](-length/index.md) |
| [perDs](per-ds.md) | `val `[`AngularVelocity`](-angular-velocity/index.md)`.perDs: `[`AngularAcceleration`](-angular-acceleration/index.md)<br>`val `[`LinearVelocity`](-linear-velocity/index.md)`.perDs: `[`LinearAcceleration`](-linear-acceleration/index.md)<br>`val `[`Length`](-length/index.md)`.perDs: `[`LinearVelocity`](-linear-velocity/index.md) |
| [perSecond](per-second.md) | `val `[`AngularVelocity`](-angular-velocity/index.md)`.perSecond: `[`AngularAcceleration`](-angular-acceleration/index.md)<br>`val `[`Angle`](-angle/index.md)`.perSecond: `[`AngularVelocity`](-angular-velocity/index.md)<br>`val `[`LinearVelocity`](-linear-velocity/index.md)`.perSecond: `[`LinearAcceleration`](-linear-acceleration/index.md)<br>`val `[`Length`](-length/index.md)`.perSecond: `[`LinearVelocity`](-linear-velocity/index.md) |
| [velocityPerDs](velocity-per-ds.md) | `val `[`AngularAcceleration`](-angular-acceleration/index.md)`.velocityPerDs: `[`AngularVelocity`](-angular-velocity/index.md) |

### Functions

| Name | Summary |
|---|---|
| [div](div.md) | `operator fun `[`AngularVelocity`](-angular-velocity/index.md)`.div(time: `[`Time`](-time/index.md)`): `[`AngularAcceleration`](-angular-acceleration/index.md)<br>`operator fun `[`Angle`](-angle/index.md)`.div(time: `[`Time`](-time/index.md)`): `[`AngularVelocity`](-angular-velocity/index.md)<br>`operator fun `[`LinearVelocity`](-linear-velocity/index.md)`.div(time: `[`Time`](-time/index.md)`): `[`LinearAcceleration`](-linear-acceleration/index.md)<br>`operator fun `[`Length`](-length/index.md)`.div(time: `[`Time`](-time/index.md)`): `[`LinearVelocity`](-linear-velocity/index.md) |
| [times](times.md) | `operator fun `[`AngularAcceleration`](-angular-acceleration/index.md)`.times(time: `[`Time`](-time/index.md)`): `[`AngularVelocity`](-angular-velocity/index.md)<br>`operator fun `[`AngularVelocity`](-angular-velocity/index.md)`.times(time: `[`Time`](-time/index.md)`): `[`Angle`](-angle/index.md)<br>`operator fun `[`LinearAcceleration`](-linear-acceleration/index.md)`.times(time: `[`Time`](-time/index.md)`): `[`LinearVelocity`](-linear-velocity/index.md)<br>`operator fun `[`LinearVelocity`](-linear-velocity/index.md)`.times(time: `[`Time`](-time/index.md)`): `[`Length`](-length/index.md) |
