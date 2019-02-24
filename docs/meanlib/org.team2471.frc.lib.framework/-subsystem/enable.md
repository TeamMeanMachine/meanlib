[meanlib](../../index.md) / [org.team2471.frc.lib.framework](../index.md) / [Subsystem](index.md) / [enable](./enable.md)

# enable

`fun enable(): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)

Enables the [Subsystem](index.md).

If the [Subsystem](index.md) was previously disabled, the [defaultFunction](#) will be run in a different coroutine, if it
was provided.

Note that enables are asynchronous, so it may take some time for the [Subsystem](index.md) to be enabled.

