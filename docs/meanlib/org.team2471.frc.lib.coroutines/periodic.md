[meanlib](../index.md) / [org.team2471.frc.lib.coroutines](index.md) / [periodic](./periodic.md)

# periodic

`inline suspend fun periodic(period: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)` = 0.02, watchOverrun: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = false, crossinline body: `[`PeriodicScope`](-periodic-scope/index.md)`.() -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)

Runs the provided [body](periodic.md#org.team2471.frc.lib.coroutines$periodic(kotlin.Double, kotlin.Boolean, kotlin.Function1((org.team2471.frc.lib.coroutines.PeriodicScope, kotlin.Unit)))/body) of code periodically per [period](periodic.md#org.team2471.frc.lib.coroutines$periodic(kotlin.Double, kotlin.Boolean, kotlin.Function1((org.team2471.frc.lib.coroutines.PeriodicScope, kotlin.Unit)))/period) seconds.

The provided [body](periodic.md#org.team2471.frc.lib.coroutines$periodic(kotlin.Double, kotlin.Boolean, kotlin.Function1((org.team2471.frc.lib.coroutines.PeriodicScope, kotlin.Unit)))/body) loop will continue to loop until [PeriodicScope.stop](-periodic-scope/stop.md) is called, or an exception is thrown.
Note that if [PeriodicScope.stop](-periodic-scope/stop.md) is called the body will continue to run to the end of the loop. If your
intention is to exit the code early, insert a return after calling [PeriodicScope.stop](-periodic-scope/stop.md).

The [period](periodic.md#org.team2471.frc.lib.coroutines$periodic(kotlin.Double, kotlin.Boolean, kotlin.Function1((org.team2471.frc.lib.coroutines.PeriodicScope, kotlin.Unit)))/period) parameter defaults to 0.02 seconds, or 20 milliseconds.

If the [body](periodic.md#org.team2471.frc.lib.coroutines$periodic(kotlin.Double, kotlin.Boolean, kotlin.Function1((org.team2471.frc.lib.coroutines.PeriodicScope, kotlin.Unit)))/body) takes longer than the [period](periodic.md#org.team2471.frc.lib.coroutines$periodic(kotlin.Double, kotlin.Boolean, kotlin.Function1((org.team2471.frc.lib.coroutines.PeriodicScope, kotlin.Unit)))/period) to complete, a warning is printed. This can
be disabled by setting the [watchOverrun](periodic.md#org.team2471.frc.lib.coroutines$periodic(kotlin.Double, kotlin.Boolean, kotlin.Function1((org.team2471.frc.lib.coroutines.PeriodicScope, kotlin.Unit)))/watchOverrun) parameter to false.

