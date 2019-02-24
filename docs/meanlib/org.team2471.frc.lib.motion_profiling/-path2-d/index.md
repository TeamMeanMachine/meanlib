[meanlib](../../index.md) / [org.team2471.frc.lib.motion_profiling](../index.md) / [Path2D](./index.md)

# Path2D

`open class Path2D`

### Types

| [CurveType](-curve-type/index.md) | `class CurveType` |
| [RobotDirection](-robot-direction/index.md) | `class RobotDirection` |

### Constructors

| [&lt;init&gt;](-init-.md) | `Path2D()`<br>`Path2D(name: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`)` |

### Properties

| [curveType](curve-type.md) | `var curveType: `[`CurveType`](-curve-type/index.md) |
| [name](name.md) | `var name: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |

### Functions

| [addEasePoint](add-ease-point.md) | `open fun addEasePoint(time: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`, value: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [addEasePointSlopeAndMagnitude](add-ease-point-slope-and-magnitude.md) | `open fun addEasePointSlopeAndMagnitude(time: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`, value: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`, slope: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`, magnitude: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [addHeadingPoint](add-heading-point.md) | `open fun addHeadingPoint(time: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`, value: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [addPoint](add-point.md) | `open fun addPoint(x: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`, y: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`): `[`Path2DPoint`](../-path2-d-point/index.md) |
| [addPointAndTangent](add-point-and-tangent.md) | `open fun addPointAndTangent(x: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`, y: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`, xTangent: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`, yTangent: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [addPointAngleAndMagnitude](add-point-angle-and-magnitude.md) | `open fun addPointAngleAndMagnitude(x: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`, y: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`, angle: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`, magnitude: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [addVector2](add-vector2.md) | `open fun addVector2(point: `[`Vector2`](../../org.team2471.frc.lib.math/-vector2/index.md)`): `[`Path2DPoint`](../-path2-d-point/index.md) |
| [addVector2After](add-vector2-after.md) | `open fun addVector2After(point: `[`Vector2`](../../org.team2471.frc.lib.math/-vector2/index.md)`, after: `[`Path2DPoint`](../-path2-d-point/index.md)`): `[`Path2DPoint`](../-path2-d-point/index.md) |
| [fromJsonString](from-json-string.md) | `open static fun fromJsonString(jsonString: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`): `[`Path2D`](./index.md) |
| [getAccelerationAtEase](get-acceleration-at-ease.md) | `open fun getAccelerationAtEase(ease: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`): `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html) |
| [getAutonomous](get-autonomous.md) | `open fun getAutonomous(): `[`Autonomous`](../-autonomous/index.md) |
| [getCurvatureAtEase](get-curvature-at-ease.md) | `open fun getCurvatureAtEase(ease: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`): `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html) |
| [getCurveType](get-curve-type.md) | `open fun getCurveType(): `[`CurveType`](-curve-type/index.md) |
| [getDuration](get-duration.md) | `open fun getDuration(): `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html) |
| [getDurationWithSpeed](get-duration-with-speed.md) | `open fun getDurationWithSpeed(): `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html) |
| [getEaseCurve](get-ease-curve.md) | `open fun getEaseCurve(): `[`MotionCurve`](../-motion-curve/index.md) |
| [getHeadingCurve](get-heading-curve.md) | `open fun getHeadingCurve(): `[`MotionCurve`](../-motion-curve/index.md) |
| [getLength](get-length.md) | `open fun getLength(): `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html) |
| [getName](get-name.md) | `open fun getName(): `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
| [getPosition](get-position.md) | `open fun getPosition(time: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`): `[`Vector2`](../../org.team2471.frc.lib.math/-vector2/index.md) |
| [getPositionAtEase](get-position-at-ease.md) | `open fun getPositionAtEase(ease: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`): `[`Vector2`](../../org.team2471.frc.lib.math/-vector2/index.md) |
| [getRobotDirection](get-robot-direction.md) | `open fun getRobotDirection(time: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`): `[`Vector2`](../../org.team2471.frc.lib.math/-vector2/index.md)<br>`open fun getRobotDirection(): `[`RobotDirection`](-robot-direction/index.md) |
| [getSidePosition](get-side-position.md) | `open fun getSidePosition(time: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`, xOffset: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`): `[`Vector2`](../../org.team2471.frc.lib.math/-vector2/index.md) |
| [getSpeed](get-speed.md) | `open fun getSpeed(): `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html) |
| [getTangent](get-tangent.md) | `open fun getTangent(time: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`): `[`Vector2`](../../org.team2471.frc.lib.math/-vector2/index.md) |
| [getTangentAtEase](get-tangent-at-ease.md) | `open fun getTangentAtEase(ease: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`): `[`Vector2`](../../org.team2471.frc.lib.math/-vector2/index.md) |
| [getVelocityAtEase](get-velocity-at-ease.md) | `open fun getVelocityAtEase(ease: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`): `[`Vector2`](../../org.team2471.frc.lib.math/-vector2/index.md) |
| [getVelocityAtTime](get-velocity-at-time.md) | `open fun getVelocityAtTime(time: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`): `[`Vector2`](../../org.team2471.frc.lib.math/-vector2/index.md) |
| [getXYCurve](get-x-y-curve.md) | `open fun getXYCurve(): `[`Path2DCurve`](../-path2-d-curve/index.md) |
| [hasPoints](has-points.md) | `open fun hasPoints(): `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) |
| [isMirrored](is-mirrored.md) | `open fun isMirrored(): `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) |
| [removeAllEasePoints](remove-all-ease-points.md) | `open fun removeAllEasePoints(): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [removePoint](remove-point.md) | `open fun removePoint(path2DPoint: `[`Path2DPoint`](../-path2-d-point/index.md)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [setAutonomous](set-autonomous.md) | `open fun setAutonomous(autonomous: `[`Autonomous`](../-autonomous/index.md)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [setCurveType](set-curve-type.md) | `open fun setCurveType(curveType: `[`CurveType`](-curve-type/index.md)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [setDuration](set-duration.md) | `open fun setDuration(seconds: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [setMirrored](set-mirrored.md) | `open fun setMirrored(mirrored: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [setName](set-name.md) | `open fun setName(name: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [setRobotDirection](set-robot-direction.md) | `open fun setRobotDirection(robotDirection: `[`RobotDirection`](-robot-direction/index.md)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [setSpeed](set-speed.md) | `open fun setSpeed(speed: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [toJsonString](to-json-string.md) | `open fun toJsonString(): `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
| [toString](to-string.md) | `open fun toString(): `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |

### Extension Functions

| [writeToNetworkTables](../write-to-network-tables.md) | `fun `[`Path2D`](./index.md)`.writeToNetworkTables(): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |

