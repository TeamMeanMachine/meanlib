[meanlib](../../index.md) / [org.team2471.frc.lib.motion_profiling](../index.md) / [MotionProfileCurve](./index.md)

# MotionProfileCurve

`open class MotionProfileCurve : `[`MotionCurve`](../-motion-curve/index.md)

### Constructors

| [&lt;init&gt;](-init-.md) | `MotionProfileCurve(pidInterface: PIDInterface)`<br>`MotionProfileCurve(pidInterface: PIDInterface, animation: `[`MotionProfileAnimation`](../-motion-profile-animation/index.md)`)` |

### Functions

| [getOffset](get-offset.md) | `open fun getOffset(): `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html) |
| [play](play.md) | `open fun play(time: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [setOffset](set-offset.md) | `open fun setOffset(offset: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [stop](stop.md) | `open fun stop(): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |

### Inherited Functions

| [getDefaultValue](../-motion-curve/get-default-value.md) | `open fun getDefaultValue(): `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html) |
| [getDerivative](../-motion-curve/get-derivative.md) | `open fun getDerivative(time: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`): `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html) |
| [getHeadKey](../-motion-curve/get-head-key.md) | `open fun getHeadKey(): `[`MotionKey`](../-motion-key/index.md) |
| [getKey](../-motion-curve/get-key.md) | `open fun getKey(time: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`): `[`MotionKey`](../-motion-key/index.md) |
| [getLastAccessedKey](../-motion-curve/get-last-accessed-key.md) | `open fun getLastAccessedKey(): `[`MotionKey`](../-motion-key/index.md) |
| [getLength](../-motion-curve/get-length.md) | `open fun getLength(): `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html) |
| [getMaxValue](../-motion-curve/get-max-value.md) | `open fun getMaxValue(): `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html) |
| [getMinValue](../-motion-curve/get-min-value.md) | `open fun getMinValue(): `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html) |
| [getTailKey](../-motion-curve/get-tail-key.md) | `open fun getTailKey(): `[`MotionKey`](../-motion-key/index.md) |
| [getValue](../-motion-curve/get-value.md) | `open fun getValue(time: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`): `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html) |
| [onKeyPositionChanged](../-motion-curve/on-key-position-changed.md) | `open fun onKeyPositionChanged(key: `[`MotionKey`](../-motion-key/index.md)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [removeAllPoints](../-motion-curve/remove-all-points.md) | `open fun removeAllPoints(): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [removeKey](../-motion-curve/remove-key.md) | `open fun removeKey(key: `[`MotionKey`](../-motion-key/index.md)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [setDefaultValue](../-motion-curve/set-default-value.md) | `open fun setDefaultValue(defaultValue: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [setHeadKey](../-motion-curve/set-head-key.md) | `open fun setHeadKey(headKey: `[`MotionKey`](../-motion-key/index.md)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [setLastAccessedKey](../-motion-curve/set-last-accessed-key.md) | `open fun setLastAccessedKey(m_lastAccessedKey: `[`MotionKey`](../-motion-key/index.md)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [setMaxValue](../-motion-curve/set-max-value.md) | `open fun setMaxValue(m_maxValue: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [setMinValue](../-motion-curve/set-min-value.md) | `open fun setMinValue(m_minValue: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [setTailKey](../-motion-curve/set-tail-key.md) | `open fun setTailKey(tailKey: `[`MotionKey`](../-motion-key/index.md)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [storeValue](../-motion-curve/store-value.md) | `open fun storeValue(time: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`, value: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`): `[`MotionKey`](../-motion-key/index.md) |
| [storeValueSlopeAndMagnitude](../-motion-curve/store-value-slope-and-magnitude.md) | `open fun storeValueSlopeAndMagnitude(time: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`, value: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`, slope: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`, magnitude: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`): `[`MotionKey`](../-motion-key/index.md) |

