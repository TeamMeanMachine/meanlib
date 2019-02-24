[meanlib](../../index.md) / [org.team2471.frc.lib.framework](../index.md) / [Subsystem](./index.md)

# Subsystem

`open class Subsystem`

An individually requirable component of your robot.

Optionally a [defaultFunction](#) may be provided. If the [defaultFunction](#) is provided, the [Subsystem](./index.md)
is enabled, and there are no coroutines using this subsystem, a new coroutine will be launched that
calls the [defaultFunction](#). This fulfills the same role as default commands in the wpilib command
system.

Note that coroutines calling the [defaultFunction](#) implicitly requires this [Subsystem](./index.md), so unless
the [defaultFunction](#) is called elsewhere in your code, it is not necessary to [use](../use.md) this [Subsystem](./index.md)
inside of it.

Subsystems are disabled by default, so [enable](enable.md) must be called before the subsystem can be used.

**See Also**

[use](../use.md)

### Constructors

| [&lt;init&gt;](-init-.md) | `Subsystem(name: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, defaultFunction: suspend () -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)` = null)`<br>An individually requirable component of your robot. |

### Properties

| [isEnabled](is-enabled.md) | `var isEnabled: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)<br>Whether or not the [Subsystem](./index.md) is enabled. |
| [name](name.md) | `val name: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>The name of your Subsystem, required for debugging purposes. |

### Functions

| [cancelActive](cancel-active.md) | `fun cancelActive(): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Cancels any active coroutine that is currently using the [Subsystem](./index.md). |
| [default](default.md) | `open suspend fun default(): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>An optional function that is run whenever the subsystem is enabled and unused. |
| [disable](disable.md) | `fun disable(): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Disables the [Subsystem](./index.md). |
| [enable](enable.md) | `fun enable(): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Enables the [Subsystem](./index.md). |
| [reset](reset.md) | `open fun reset(): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>An optionally overloadable method. This method is automatically run whenever any [use](../use.md) call completes, regardless of if it completed or canceled. |

