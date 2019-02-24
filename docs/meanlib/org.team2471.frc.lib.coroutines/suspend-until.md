[meanlib](../index.md) / [org.team2471.frc.lib.coroutines](index.md) / [suspendUntil](./suspend-until.md)

# suspendUntil

`inline suspend fun suspendUntil(pollingRate: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)` = 20, condition: () -> `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)

Suspends until [condition](suspend-until.md#org.team2471.frc.lib.coroutines$suspendUntil(kotlin.Int, kotlin.Function0((kotlin.Boolean)))/condition) evaluates to true.

### Parameters

`pollingRate` - The time between each check, in milliseconds