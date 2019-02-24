[meanlib](../../index.md) / [org.team2471.frc.lib.math](../index.md) / [Line](./index.md)

# Line

`data class Line`

### Constructors

| Name | Summary |
|---|---|
| [&lt;init&gt;](-init-.md) | `Line(pointA: `[`Point`](../-point/index.md)`, pointB: `[`Point`](../-point/index.md)`)` |

### Properties

| Name | Summary |
|---|---|
| [intercept](intercept.md) | `val intercept: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html) |
| [pointA](point-a.md) | `val pointA: `[`Point`](../-point/index.md) |
| [pointB](point-b.md) | `val pointB: `[`Point`](../-point/index.md) |
| [slope](slope.md) | `val slope: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html) |

### Functions

| Name | Summary |
|---|---|
| [get](get.md) | `operator fun get(x: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`): `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html) |
| [minus](minus.md) | `operator fun minus(vec: `[`Vector2`](../-vector2/index.md)`): `[`Line`](./index.md) |
| [plus](plus.md) | `operator fun plus(vec: `[`Vector2`](../-vector2/index.md)`): `[`Line`](./index.md) |
| [pointInLine](point-in-line.md) | `fun pointInLine(point: `[`Point`](../-point/index.md)`): `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) |
| [pointInSegment](point-in-segment.md) | `fun pointInSegment(point: `[`Point`](../-point/index.md)`): `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) |
