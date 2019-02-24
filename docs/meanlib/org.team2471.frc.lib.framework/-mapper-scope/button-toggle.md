[meanlib](../../index.md) / [org.team2471.frc.lib.framework](../index.md) / [MapperScope](index.md) / [buttonToggle](./button-toggle.md)

# buttonToggle

`fun buttonToggle(button: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)`, body: suspend CoroutineScope.() -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)

Toggles the execution of [body](button-toggle.md#org.team2471.frc.lib.framework.MapperScope$buttonToggle(kotlin.Int, kotlin.SuspendFunction1((kotlinx.coroutines.CoroutineScope, kotlin.Unit)))/body) for each press of [button](button-toggle.md#org.team2471.frc.lib.framework.MapperScope$buttonToggle(kotlin.Int, kotlin.SuspendFunction1((kotlinx.coroutines.CoroutineScope, kotlin.Unit)))/button). When toggled off, [body](button-toggle.md#org.team2471.frc.lib.framework.MapperScope$buttonToggle(kotlin.Int, kotlin.SuspendFunction1((kotlinx.coroutines.CoroutineScope, kotlin.Unit)))/body) is
cancelled.

