[meanlib](../../index.md) / [org.team2471.frc.lib.motion_profiling](../index.md) / [MotionCurve](./index.md)

# MotionCurve

`open class MotionCurve`

### Types

| [ExtrapolationMethods](-extrapolation-methods/index.md) | `class ExtrapolationMethods` |

### Constructors

| [&lt;init&gt;](-init-.md) | `MotionCurve()` |

### Functions

| [getDefaultValue](get-default-value.md) | `open fun getDefaultValue(): `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html) |
| [getDerivative](get-derivative.md) | `open fun getDerivative(time: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`): `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html) |
| [getHeadKey](get-head-key.md) | `open fun getHeadKey(): `[`MotionKey`](../-motion-key/index.md) |
| [getKey](get-key.md) | `open fun getKey(time: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`): `[`MotionKey`](../-motion-key/index.md) |
| [getLastAccessedKey](get-last-accessed-key.md) | `open fun getLastAccessedKey(): `[`MotionKey`](../-motion-key/index.md) |
| [getLength](get-length.md) | `open fun getLength(): `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html) |
| [getMaxValue](get-max-value.md) | `open fun getMaxValue(): `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html) |
| [getMinValue](get-min-value.md) | `open fun getMinValue(): `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html) |
| [getTailKey](get-tail-key.md) | `open fun getTailKey(): `[`MotionKey`](../-motion-key/index.md) |
| [getValue](get-value.md) | `open fun getValue(time: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`): `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html) |
| [onKeyPositionChanged](on-key-position-changed.md) | `open fun onKeyPositionChanged(key: `[`MotionKey`](../-motion-key/index.md)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [removeAllPoints](remove-all-points.md) | `open fun removeAllPoints(): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [removeKey](remove-key.md) | `open fun removeKey(key: `[`MotionKey`](../-motion-key/index.md)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [setDefaultValue](set-default-value.md) | `open fun setDefaultValue(defaultValue: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [setHeadKey](set-head-key.md) | `open fun setHeadKey(headKey: `[`MotionKey`](../-motion-key/index.md)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [setLastAccessedKey](set-last-accessed-key.md) | `open fun setLastAccessedKey(m_lastAccessedKey: `[`MotionKey`](../-motion-key/index.md)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [setMaxValue](set-max-value.md) | `open fun setMaxValue(m_maxValue: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [setMinValue](set-min-value.md) | `open fun setMinValue(m_minValue: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [setTailKey](set-tail-key.md) | `open fun setTailKey(tailKey: `[`MotionKey`](../-motion-key/index.md)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [storeValue](store-value.md) | `open fun storeValue(time: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`, value: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`): `[`MotionKey`](../-motion-key/index.md) |
| [storeValueSlopeAndMagnitude](store-value-slope-and-magnitude.md) | `open fun storeValueSlopeAndMagnitude(time: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`, value: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`, slope: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`, magnitude: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`): `[`MotionKey`](../-motion-key/index.md) |

### Inheritors

| [MotionProfileCurve](../-motion-profile-curve/index.md) | `open class MotionProfileCurve : `[`MotionCurve`](./index.md) |

