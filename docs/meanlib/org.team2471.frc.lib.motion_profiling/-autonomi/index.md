[meanlib](../../index.md) / [org.team2471.frc.lib.motion_profiling](../index.md) / [Autonomi](./index.md)

# Autonomi

`open class Autonomi`

### Constructors

| [&lt;init&gt;](-init-.md) | `Autonomi()` |

### Properties

| [drivetrainParameters](drivetrain-parameters.md) | `var drivetrainParameters: `[`DrivetrainParameters`](../../org.team2471.frc.lib.motion_profiling.following/-drivetrain-parameters/index.md) |
| [mapAutonomous](map-autonomous.md) | `var mapAutonomous: `[`MutableMap`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-mutable-map/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, `[`Autonomous`](../-autonomous/index.md)`>` |
| [robotParameters](robot-parameters.md) | `var robotParameters: `[`RobotParameters`](../../org.team2471.frc.lib.motion_profiling.following/-robot-parameters/index.md) |

### Functions

| [fromJsonString](from-json-string.md) | `open static fun fromJsonString(json: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`): `[`Autonomi`](./index.md) |
| [get](get.md) | `open fun get(name: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`): `[`Autonomous`](../-autonomous/index.md) |
| [getPath](get-path.md) | `open fun getPath(autoName: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, pathName: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`): `[`Path2D`](../-path2-d/index.md) |
| [publishToNetworkTables](publish-to-network-tables.md) | `open fun publishToNetworkTables(networkTableInstance: NetworkTableInstance): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [put](put.md) | `open fun put(autonomous: `[`Autonomous`](../-autonomous/index.md)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [toJsonString](to-json-string.md) | `open fun toJsonString(): `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |

