[meanlib](../../index.md) / [org.team2471.frc.lib.data](../index.md) / [InterpolatableCircularBuffer](./index.md)

# InterpolatableCircularBuffer

`class InterpolatableCircularBuffer : CircularBuffer`

### Constructors

| Name | Summary |
|---|---|
| [&lt;init&gt;](-init-.md) | `InterpolatableCircularBuffer(size: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)`)` |

### Functions

| Name | Summary |
|---|---|
| [get](get.md) | `operator fun get(index: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)`): `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)<br>Get the element at the provided index relative to the start of the buffer.`operator fun get(index: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`): `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)<br>Alias for [interpolate](interpolate.md) |
| [interpolate](interpolate.md) | `fun interpolate(index: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`): `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)<br>Get the linear interpolation of elements between provided index relative to the start of the buffer. |
