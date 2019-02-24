[meanlib](../../index.md) / [org.team2471.frc.lib.framework](../index.md) / [Subsystem](index.md) / [&lt;init&gt;](./-init-.md)

# &lt;init&gt;

`Subsystem(name: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, defaultFunction: suspend () -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)` = null)`

An individually requirable component of your robot.

Optionally a [defaultFunction](#) may be provided. If the [defaultFunction](#) is provided, the [Subsystem](index.md)
is enabled, and there are no coroutines using this subsystem, a new coroutine will be launched that
calls the [defaultFunction](#). This fulfills the same role as default commands in the wpilib command
system.

Note that coroutines calling the [defaultFunction](#) implicitly requires this [Subsystem](index.md), so unless
the [defaultFunction](#) is called elsewhere in your code, it is not necessary to [use](../use.md) this [Subsystem](index.md)
inside of it.

Subsystems are disabled by default, so [enable](enable.md) must be called before the subsystem can be used.

**See Also**

[use](../use.md)

