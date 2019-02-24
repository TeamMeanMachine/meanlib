[meanlib](../../index.md) / [org.team2471.frc.lib.motion_profiling](../index.md) / [Path2DCurve](./index.md)

# Path2DCurve

`open class Path2DCurve`

### Constructors

| [&lt;init&gt;](-init-.md) | `Path2DCurve()` |

### Functions

| [addPointAfter](add-point-after.md) | `open fun addPointAfter(vec: `[`Vector2`](../../org.team2471.frc.lib.math/-vector2/index.md)`, after: `[`Path2DPoint`](../-path2-d-point/index.md)`): `[`Path2DPoint`](../-path2-d-point/index.md) |
| [addPointAngleAndMagnitudeToEnd](add-point-angle-and-magnitude-to-end.md) | `open fun addPointAngleAndMagnitudeToEnd(x: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`, y: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`, angle: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`, magnitude: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [addPointToEnd](add-point-to-end.md) | `open fun addPointToEnd(x: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`, y: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`): `[`Path2DPoint`](../-path2-d-point/index.md)<br>`open fun addPointToEnd(x: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`, y: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`, xTangent: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`, yTangent: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [getHeadPoint](get-head-point.md) | `open fun getHeadPoint(): `[`Path2DPoint`](../-path2-d-point/index.md) |
| [getLength](get-length.md) | `open fun getLength(): `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html) |
| [getPositionAtDistance](get-position-at-distance.md) | `open fun getPositionAtDistance(distance: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`): `[`Vector2`](../../org.team2471.frc.lib.math/-vector2/index.md) |
| [getTailPoint](get-tail-point.md) | `open fun getTailPoint(): `[`Path2DPoint`](../-path2-d-point/index.md) |
| [getTangentAtDistance](get-tangent-at-distance.md) | `open fun getTangentAtDistance(distance: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`): `[`Vector2`](../../org.team2471.frc.lib.math/-vector2/index.md) |
| [onPositionChanged](on-position-changed.md) | `open fun onPositionChanged(): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [removePoint](remove-point.md) | `open fun removePoint(path2DPoint: `[`Path2DPoint`](../-path2-d-point/index.md)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |

