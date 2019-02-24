[meanlib](../../index.md) / [org.team2471.frc.lib.framework](../index.md) / [MapperScope](index.md) / [buttonHold](./button-hold.md)

# buttonHold

`fun buttonHold(button: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)`, body: suspend CoroutineScope.() -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)

Executes [body](button-hold.md#org.team2471.frc.lib.framework.MapperScope$buttonHold(kotlin.Int, kotlin.SuspendFunction1((kotlinx.coroutines.CoroutineScope, kotlin.Unit)))/body) while [button](button-hold.md#org.team2471.frc.lib.framework.MapperScope$buttonHold(kotlin.Int, kotlin.SuspendFunction1((kotlinx.coroutines.CoroutineScope, kotlin.Unit)))/button) is pressed. When released, [body](button-hold.md#org.team2471.frc.lib.framework.MapperScope$buttonHold(kotlin.Int, kotlin.SuspendFunction1((kotlinx.coroutines.CoroutineScope, kotlin.Unit)))/body) is cancelled.

