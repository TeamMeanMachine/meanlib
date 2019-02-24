[meanlib](../index.md) / [org.team2471.frc.lib.coroutines](./index.md)

## Package org.team2471.frc.lib.coroutines

### Types

| Name | Summary |
|---|---|
| [MeanlibScope](-meanlib-scope/index.md) | `object ~~MeanlibScope~~ : CoroutineScope` |
| [PeriodicScope](-periodic-scope/index.md) | `class PeriodicScope` |

### Extensions for External Classes

| Name | Summary |
|---|---|
| [kotlinx.coroutines.CoroutineScope](kotlinx.coroutines.-coroutine-scope/index.md) |  |

### Properties

| Name | Summary |
|---|---|
| [MeanlibDispatcher](-meanlib-dispatcher.md) | `val MeanlibDispatcher: CoroutineDispatcher`<br>A [CoroutineDispatcher](#) for use on the roboRIO's limited number of CPU cores. |

### Functions

| Name | Summary |
|---|---|
| [delay](delay.md) | `suspend fun delay(time: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Suspends the coroutine for [time](delay.md#org.team2471.frc.lib.coroutines$delay(kotlin.Double)/time) seconds.`suspend fun delay(time: `[`Time`](../org.team2471.frc.lib.units/-time/index.md)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [halt](halt.md) | `suspend fun halt(): `[`Nothing`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-nothing/index.html)<br>Suspends the coroutine forever. |
| [periodic](periodic.md) | `suspend fun periodic(period: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)` = 0.02, watchOverrun: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = false, body: `[`PeriodicScope`](-periodic-scope/index.md)`.() -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Runs the provided [body](periodic.md#org.team2471.frc.lib.coroutines$periodic(kotlin.Double, kotlin.Boolean, kotlin.Function1((org.team2471.frc.lib.coroutines.PeriodicScope, kotlin.Unit)))/body) of code periodically per [period](periodic.md#org.team2471.frc.lib.coroutines$periodic(kotlin.Double, kotlin.Boolean, kotlin.Function1((org.team2471.frc.lib.coroutines.PeriodicScope, kotlin.Unit)))/period) seconds. |
| [suspendUntil](suspend-until.md) | `suspend fun suspendUntil(pollingRate: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)` = 20, condition: () -> `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Suspends until [condition](suspend-until.md#org.team2471.frc.lib.coroutines$suspendUntil(kotlin.Int, kotlin.Function0((kotlin.Boolean)))/condition) evaluates to true. |
