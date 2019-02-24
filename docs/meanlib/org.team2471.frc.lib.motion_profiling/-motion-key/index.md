[meanlib](../../index.md) / [org.team2471.frc.lib.motion_profiling](../index.md) / [MotionKey](./index.md)

# MotionKey

`open class MotionKey`

### Types

| Name | Summary |
|---|---|
| [SlopeMethod](-slope-method/index.md) | `class SlopeMethod` |

### Constructors

| Name | Summary |
|---|---|
| [&lt;init&gt;](-init-.md) | `MotionKey()` |

### Functions

| Name | Summary |
|---|---|
| [areCoefficientsDirty](are-coefficients-dirty.md) | `open fun areCoefficientsDirty(): `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) |
| [areTangentsDirty](are-tangents-dirty.md) | `open fun areTangentsDirty(): `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) |
| [getAngle](get-angle.md) | `open fun getAngle(): `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html) |
| [getM_nextKey](get-m_next-key.md) | `open fun getM_nextKey(): `[`MotionKey`](./index.md) |
| [getM_prevKey](get-m_prev-key.md) | `open fun getM_prevKey(): `[`MotionKey`](./index.md) |
| [getMagnitude](get-magnitude.md) | `open fun getMagnitude(): `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html) |
| [getMotionCurve](get-motion-curve.md) | `open fun getMotionCurve(): `[`MotionCurve`](../-motion-curve/index.md) |
| [getNextAngle](get-next-angle.md) | `open fun getNextAngle(): `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html) |
| [getNextAngleAndMagnitude](get-next-angle-and-magnitude.md) | `open fun getNextAngleAndMagnitude(): `[`Vector2`](../../org.team2471.frc.lib.vector/-vector2/index.md) |
| [getNextKey](get-next-key.md) | `open fun getNextKey(): `[`MotionKey`](./index.md) |
| [getNextMagnitude](get-next-magnitude.md) | `open fun getNextMagnitude(): `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html) |
| [getNextSlopeMethod](get-next-slope-method.md) | `open fun getNextSlopeMethod(): `[`SlopeMethod`](-slope-method/index.md) |
| [getNextTangent](get-next-tangent.md) | `open fun getNextTangent(): `[`Vector2`](../../org.team2471.frc.lib.vector/-vector2/index.md) |
| [getPrevAngle](get-prev-angle.md) | `open fun getPrevAngle(): `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html) |
| [getPrevAngleAndMagnitude](get-prev-angle-and-magnitude.md) | `open fun getPrevAngleAndMagnitude(): `[`Vector2`](../../org.team2471.frc.lib.vector/-vector2/index.md) |
| [getPrevKey](get-prev-key.md) | `open fun getPrevKey(): `[`MotionKey`](./index.md) |
| [getPrevMagnitude](get-prev-magnitude.md) | `open fun getPrevMagnitude(): `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html) |
| [getPrevSlopeMethod](get-prev-slope-method.md) | `open fun getPrevSlopeMethod(): `[`SlopeMethod`](-slope-method/index.md) |
| [getPrevTangent](get-prev-tangent.md) | `open fun getPrevTangent(): `[`Vector2`](../../org.team2471.frc.lib.vector/-vector2/index.md) |
| [getTime](get-time.md) | `open fun getTime(): `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html) |
| [getTimeAndValue](get-time-and-value.md) | `open fun getTimeAndValue(): `[`Vector2`](../../org.team2471.frc.lib.vector/-vector2/index.md) |
| [getValue](get-value.md) | `open fun getValue(): `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html) |
| [getXCoefficients](get-x-coefficients.md) | `open fun getXCoefficients(): `[`CubicCoefficients1D`](../-cubic-coefficients1-d/index.md) |
| [getYCoefficients](get-y-coefficients.md) | `open fun getYCoefficients(): `[`CubicCoefficients1D`](../-cubic-coefficients1-d/index.md) |
| [isMarkbeginOrEndKeysToZeroSlope](is-markbegin-or-end-keys-to-zero-slope.md) | `open fun isMarkbeginOrEndKeysToZeroSlope(): `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) |
| [onPositionChanged](on-position-changed.md) | `open fun onPositionChanged(): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [setAngle](set-angle.md) | `open fun setAngle(angle: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [setCoefficientsDirty](set-coefficients-dirty.md) | `open fun setCoefficientsDirty(bCoefficientsDirty: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [setMagnitude](set-magnitude.md) | `open fun setMagnitude(magnitude: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [setMarkBeginOrEndKeysToZeroSlope](set-mark-begin-or-end-keys-to-zero-slope.md) | `open fun setMarkBeginOrEndKeysToZeroSlope(m_setBeginOrEndKeysToZeroSlope: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [setMotionCurve](set-motion-curve.md) | `open fun setMotionCurve(m_motionCurve: `[`MotionCurve`](../-motion-curve/index.md)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [setNextAngle](set-next-angle.md) | `open fun setNextAngle(angle: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [setNextAngleAndMagnitude](set-next-angle-and-magnitude.md) | `open fun setNextAngleAndMagnitude(m_nextAngleAndMagnitude: `[`Vector2`](../../org.team2471.frc.lib.vector/-vector2/index.md)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [setNextKey](set-next-key.md) | `open fun setNextKey(m_nextKey: `[`MotionKey`](./index.md)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [setNextMagnitude](set-next-magnitude.md) | `open fun setNextMagnitude(magnitude: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [setNextSlopeMethod](set-next-slope-method.md) | `open fun setNextSlopeMethod(slopeMethod: `[`SlopeMethod`](-slope-method/index.md)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [setNextTangent](set-next-tangent.md) | `open fun setNextTangent(m_NextTangent: `[`Vector2`](../../org.team2471.frc.lib.vector/-vector2/index.md)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [setPrevAngle](set-prev-angle.md) | `open fun setPrevAngle(angle: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [setPrevAngleAndMagnitude](set-prev-angle-and-magnitude.md) | `open fun setPrevAngleAndMagnitude(m_prevAngleAndMagnitude: `[`Vector2`](../../org.team2471.frc.lib.vector/-vector2/index.md)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [setPrevKey](set-prev-key.md) | `open fun setPrevKey(m_prevKey: `[`MotionKey`](./index.md)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [setPrevMagnitude](set-prev-magnitude.md) | `open fun setPrevMagnitude(magnitude: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [setPrevSlopeMethod](set-prev-slope-method.md) | `open fun setPrevSlopeMethod(slopeMethod: `[`SlopeMethod`](-slope-method/index.md)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [setPrevTangent](set-prev-tangent.md) | `open fun setPrevTangent(m_PrevTangent: `[`Vector2`](../../org.team2471.frc.lib.vector/-vector2/index.md)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [setTangentsDirty](set-tangents-dirty.md) | `open fun setTangentsDirty(bTangentsDirty: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [setTime](set-time.md) | `open fun setTime(time: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [setTimeAndValue](set-time-and-value.md) | `open fun setTimeAndValue(m_timeAndValue: `[`Vector2`](../../org.team2471.frc.lib.vector/-vector2/index.md)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [setValue](set-value.md) | `open fun setValue(value: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
