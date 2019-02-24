[meanlib](../../index.md) / [org.team2471.frc.lib.framework](../index.md) / [Subsystem](index.md) / [disable](./disable.md)

# disable

`fun disable(): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)

Disables the [Subsystem](index.md).

If the [Subsystem](index.md) was previously enabled, the current coroutine using it will be canceled, if there is one.

Note that disables are asynchronous, so it may take some time for the [Subsystem](index.md) to be disabled.

