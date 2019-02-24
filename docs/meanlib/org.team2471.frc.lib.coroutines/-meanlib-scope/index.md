[meanlib](../../index.md) / [org.team2471.frc.lib.coroutines](../index.md) / [MeanlibScope](./index.md)

# MeanlibScope

`object ~~MeanlibScope~~ : CoroutineScope`
**Deprecated:** Use the MeanlibDispatcher instead

### Properties

| Name | Summary |
|---|---|
| [coroutineContext](coroutine-context.md) | `val coroutineContext: `[`CoroutineContext`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.coroutines/-coroutine-context/index.html) |

### Extension Functions

| Name | Summary |
|---|---|
| [parallel](../kotlinx.coroutines.-coroutine-scope/parallel.md) | `suspend fun CoroutineScope.parallel(vararg blocks: suspend () -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Runs each block in [blocks](../kotlinx.coroutines.-coroutine-scope/parallel.md#org.team2471.frc.lib.coroutines$parallel(kotlinx.coroutines.CoroutineScope, kotlin.Array((kotlin.SuspendFunction0((kotlin.Unit)))))/blocks) in a child coroutine, and suspends until all of them have completed. |
