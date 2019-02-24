[meanlib](../../index.md) / [org.team2471.frc.lib.framework](../index.md) / [Subsystem](index.md) / [cancelActive](./cancel-active.md)

# cancelActive

`fun cancelActive(): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)

Cancels any active coroutine that is currently using the [Subsystem](index.md).

If a [use](../use.md) call requires more than one [Subsystem](index.md), calling [cancelActive](./cancel-active.md) on any of them will have the
same effect.

