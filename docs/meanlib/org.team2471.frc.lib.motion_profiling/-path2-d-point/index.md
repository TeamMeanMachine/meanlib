[meanlib](../../index.md) / [org.team2471.frc.lib.motion_profiling](../index.md) / [Path2DPoint](./index.md)

# Path2DPoint

`open class Path2DPoint`

### Types

| [PointType](-point-type/index.md) | `class PointType` |
| [SlopeMethod](-slope-method/index.md) | `class SlopeMethod` |

### Constructors

| [&lt;init&gt;](-init-.md) | `Path2DPoint(x: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`, y: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`)`<br>`Path2DPoint()` |

### Properties

| [STEPS](-s-t-e-p-s.md) | `static val STEPS: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) |

### Functions

| [areCoefficientsDirty](are-coefficients-dirty.md) | `open fun areCoefficientsDirty(): `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) |
| [areTangentsDirty](are-tangents-dirty.md) | `open fun areTangentsDirty(): `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) |
| [getNextAngle](get-next-angle.md) | `open fun getNextAngle(): `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html) |
| [getNextAngleAndMagnitude](get-next-angle-and-magnitude.md) | `open fun getNextAngleAndMagnitude(): `[`Vector2`](../../org.team2471.frc.lib.math/-vector2/index.md) |
| [getNextMagnitude](get-next-magnitude.md) | `open fun getNextMagnitude(): `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html) |
| [getNextPoint](get-next-point.md) | `open fun getNextPoint(): `[`Path2DPoint`](./index.md) |
| [getNextSlopeMethod](get-next-slope-method.md) | `open fun getNextSlopeMethod(): `[`SlopeMethod`](-slope-method/index.md) |
| [getNextTangent](get-next-tangent.md) | `open fun getNextTangent(): `[`Vector2`](../../org.team2471.frc.lib.math/-vector2/index.md) |
| [getPath2DCurve](get-path2-d-curve.md) | `open fun getPath2DCurve(): `[`Path2DCurve`](../-path2-d-curve/index.md) |
| [getPosition](get-position.md) | `open fun getPosition(): `[`Vector2`](../../org.team2471.frc.lib.math/-vector2/index.md) |
| [getPositionAtDistance](get-position-at-distance.md) | `open fun getPositionAtDistance(distance: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`): `[`Vector2`](../../org.team2471.frc.lib.math/-vector2/index.md) |
| [getPrevAngle](get-prev-angle.md) | `open fun getPrevAngle(): `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html) |
| [getPrevAngleAndMagnitude](get-prev-angle-and-magnitude.md) | `open fun getPrevAngleAndMagnitude(): `[`Vector2`](../../org.team2471.frc.lib.math/-vector2/index.md) |
| [getPrevMagnitude](get-prev-magnitude.md) | `open fun getPrevMagnitude(): `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html) |
| [getPrevPoint](get-prev-point.md) | `open fun getPrevPoint(): `[`Path2DPoint`](./index.md) |
| [getPrevSlopeMethod](get-prev-slope-method.md) | `open fun getPrevSlopeMethod(): `[`SlopeMethod`](-slope-method/index.md) |
| [getPrevTangent](get-prev-tangent.md) | `open fun getPrevTangent(): `[`Vector2`](../../org.team2471.frc.lib.math/-vector2/index.md) |
| [getSegmentLength](get-segment-length.md) | `open fun getSegmentLength(): `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html) |
| [getTangentAtDistance](get-tangent-at-distance.md) | `open fun getTangentAtDistance(distance: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`): `[`Vector2`](../../org.team2471.frc.lib.math/-vector2/index.md) |
| [getXCoefficients](get-x-coefficients.md) | `open fun getXCoefficients(): `[`CubicCoefficients1D`](../-cubic-coefficients1-d/index.md) |
| [getYCoefficients](get-y-coefficients.md) | `open fun getYCoefficients(): `[`CubicCoefficients1D`](../-cubic-coefficients1-d/index.md) |
| [onPositionChanged](on-position-changed.md) | `open fun onPositionChanged(): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [setCoefficientsDirty](set-coefficients-dirty.md) | `open fun setCoefficientsDirty(bCoefficientsDirty: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [setNextAngleAndMagnitude](set-next-angle-and-magnitude.md) | `open fun setNextAngleAndMagnitude(nextAngleAndMagnitude: `[`Vector2`](../../org.team2471.frc.lib.math/-vector2/index.md)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [setNextPoint](set-next-point.md) | `open fun setNextPoint(nextPoint: `[`Path2DPoint`](./index.md)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [setNextSlopeMethod](set-next-slope-method.md) | `open fun setNextSlopeMethod(slopeMethod: `[`SlopeMethod`](-slope-method/index.md)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [setNextTangent](set-next-tangent.md) | `open fun setNextTangent(nextTangent: `[`Vector2`](../../org.team2471.frc.lib.math/-vector2/index.md)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [setPath2DCurve](set-path2-d-curve.md) | `open fun setPath2DCurve(path2DCurve: `[`Path2DCurve`](../-path2-d-curve/index.md)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [setPosition](set-position.md) | `open fun setPosition(position: `[`Vector2`](../../org.team2471.frc.lib.math/-vector2/index.md)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [setPrevAngleAndMagnitude](set-prev-angle-and-magnitude.md) | `open fun setPrevAngleAndMagnitude(prevAngleAndMagnitude: `[`Vector2`](../../org.team2471.frc.lib.math/-vector2/index.md)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [setPrevPoint](set-prev-point.md) | `open fun setPrevPoint(prevPoint: `[`Path2DPoint`](./index.md)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [setPrevSlopeMethod](set-prev-slope-method.md) | `open fun setPrevSlopeMethod(slopeMethod: `[`SlopeMethod`](-slope-method/index.md)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [setPrevTangent](set-prev-tangent.md) | `open fun setPrevTangent(prevTangent: `[`Vector2`](../../org.team2471.frc.lib.math/-vector2/index.md)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [setTangentsDirty](set-tangents-dirty.md) | `open fun setTangentsDirty(bTangentsDirty: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [toString](to-string.md) | `open fun toString(): `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |

