[meanlib](../../index.md) / [org.team2471.frc.lib.control](../index.md) / [PDController](./index.md)

# PDController

`class PDController`

A PD controller for closed loop system control.

### Parameters

`p` - the proportional gain of the system

`d` - the differential gain of the system

### Constructors

| Name | Summary |
|---|---|
| [&lt;init&gt;](-init-.md) | `PDController(p: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`, d: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`)`<br>A PD controller for closed loop system control. |

### Properties

| Name | Summary |
|---|---|
| [d](d.md) | `var d: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)<br>the differential gain of the system |
| [p](p.md) | `var p: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)<br>the proportional gain of the system |

### Functions

| Name | Summary |
|---|---|
| [update](update.md) | `fun update(error: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`): `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)<br>Updates the [PDController](./index.md) with a new [error](update.md#org.team2471.frc.lib.control.PDController$update(kotlin.Double)/error), and returns an output. |
