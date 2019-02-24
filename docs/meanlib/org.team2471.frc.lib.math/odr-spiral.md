[meanlib](../index.md) / [org.team2471.frc.lib.math](index.md) / [odrSpiral](./odr-spiral.md)

# odrSpiral

`fun odrSpiral(s: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`, cDot: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`, initialCurvature: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`): `[`Triple`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-triple/index.html)`<`[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`, `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`, `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`>`

Approximate one point of the "standard" spiral (curvature at start is 0).

### Parameters

`s` - run-length along spiral

`cDot` - first derivative of curvature [1/m2](1/m2)

`initialCurvature` - curvature at start

**Return**
triple of three double values containing x, y, and tangent direction

