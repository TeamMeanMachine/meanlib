[meanlib](../../index.md) / [org.team2471.frc.lib.coroutines](../index.md) / [kotlinx.coroutines.CoroutineScope](index.md) / [parallel](./parallel.md)

# parallel

`inline suspend fun CoroutineScope.parallel(vararg blocks: suspend () -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)

Runs each block in [blocks](parallel.md#org.team2471.frc.lib.coroutines$parallel(kotlinx.coroutines.CoroutineScope, kotlin.Array((kotlin.SuspendFunction0((kotlin.Unit)))))/blocks) in a child coroutine, and suspends until all of them have completed.

If one child is cancelled, the remaining children are stopped, and the exception is propagated upwards.

