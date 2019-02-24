[meanlib](../../index.md) / [org.team2471.frc.lib.math](../index.md) / [Point](./index.md)

# Point

`data class Point`

### Constructors

| Name | Summary |
|---|---|
| [&lt;init&gt;](-init-.md) | `Point(x: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`, y: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`)` |

### Properties

| Name | Summary |
|---|---|
| [x](x.md) | `val x: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html) |
| [y](y.md) | `val y: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html) |

### Functions

| Name | Summary |
|---|---|
| [closestPoint](closest-point.md) | `fun closestPoint(firstPoint: `[`Point`](./index.md)`, vararg additionalPoints: `[`Point`](./index.md)`): `[`Point`](./index.md) |
| [distance](distance.md) | `fun distance(b: `[`Point`](./index.md)`): `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html) |
| [div](div.md) | `operator fun div(scalar: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`): `[`Point`](./index.md) |
| [minus](minus.md) | `operator fun minus(b: `[`Point`](./index.md)`): `[`Point`](./index.md)<br>`operator fun minus(vec: `[`Vector2`](../-vector2/index.md)`): `[`Point`](./index.md) |
| [plus](plus.md) | `operator fun plus(b: `[`Point`](./index.md)`): `[`Point`](./index.md)<br>`operator fun plus(vec: `[`Vector2`](../-vector2/index.md)`): `[`Point`](./index.md) |
| [times](times.md) | `operator fun times(scalar: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`): `[`Point`](./index.md) |
| [unaryMinus](unary-minus.md) | `operator fun unaryMinus(): `[`Point`](./index.md) |
| [unaryPlus](unary-plus.md) | `operator fun unaryPlus(): `[`Point`](./index.md) |
| [vectorTo](vector-to.md) | `fun vectorTo(b: `[`Point`](./index.md)`): `[`Vector2`](../-vector2/index.md) |

### Companion Object Properties

| Name | Summary |
|---|---|
| [ORIGIN](-o-r-i-g-i-n.md) | `val ORIGIN: `[`Point`](./index.md) |
