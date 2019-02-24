[meanlib](../index.md) / [org.team2471.frc.lib.framework](index.md) / [use](./use.md)

# use

`suspend fun <R> use(vararg subsystems: `[`Subsystem`](-subsystem/index.md)`, cancelConflicts: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = true, body: suspend CoroutineScope.() -> `[`R`](use.md#R)`): `[`R`](use.md#R)

Attempts to run the provided [body](use.md#org.team2471.frc.lib.framework$use(kotlin.Array((org.team2471.frc.lib.framework.Subsystem)), kotlin.Boolean, kotlin.SuspendFunction1((kotlinx.coroutines.CoroutineScope, org.team2471.frc.lib.framework.use.R)))/body) with exclusive access to all provided [subsystems](use.md#org.team2471.frc.lib.framework$use(kotlin.Array((org.team2471.frc.lib.framework.Subsystem)), kotlin.Boolean, kotlin.SuspendFunction1((kotlinx.coroutines.CoroutineScope, org.team2471.frc.lib.framework.use.R)))/subsystems). This fulfills
the same role as a Command in wpilib's command system.

If [cancelConflicts](use.md#org.team2471.frc.lib.framework$use(kotlin.Array((org.team2471.frc.lib.framework.Subsystem)), kotlin.Boolean, kotlin.SuspendFunction1((kotlinx.coroutines.CoroutineScope, org.team2471.frc.lib.framework.use.R)))/cancelConflicts) is set to false and one of the [subsystems](use.md#org.team2471.frc.lib.framework$use(kotlin.Array((org.team2471.frc.lib.framework.Subsystem)), kotlin.Boolean, kotlin.SuspendFunction1((kotlinx.coroutines.CoroutineScope, org.team2471.frc.lib.framework.use.R)))/subsystems) is being used by another coroutine,
an exception will be thrown and the provided [body](use.md#org.team2471.frc.lib.framework$use(kotlin.Array((org.team2471.frc.lib.framework.Subsystem)), kotlin.Boolean, kotlin.SuspendFunction1((kotlinx.coroutines.CoroutineScope, org.team2471.frc.lib.framework.use.R)))/body) will not be invoked. Otherwise all coroutines requiring
any of [subsystems](use.md#org.team2471.frc.lib.framework$use(kotlin.Array((org.team2471.frc.lib.framework.Subsystem)), kotlin.Boolean, kotlin.SuspendFunction1((kotlinx.coroutines.CoroutineScope, org.team2471.frc.lib.framework.use.R)))/subsystems) will be cancelled and completed before the [body](use.md#org.team2471.frc.lib.framework$use(kotlin.Array((org.team2471.frc.lib.framework.Subsystem)), kotlin.Boolean, kotlin.SuspendFunction1((kotlinx.coroutines.CoroutineScope, org.team2471.frc.lib.framework.use.R)))/body) is invoked.

Use calls are re-entrant, meaning if a coroutine is using subsystems A and B calls [use](./use.md) with subsystems B and C,
the code inside the nested [use](./use.md) call's [body](use.md#org.team2471.frc.lib.framework$use(kotlin.Array((org.team2471.frc.lib.framework.Subsystem)), kotlin.Boolean, kotlin.SuspendFunction1((kotlinx.coroutines.CoroutineScope, org.team2471.frc.lib.framework.use.R)))/body) will effectively be using subsystems A, B, and C, instead of
cancelling itself.

