[meanlib](../../index.md) / [org.team2471.frc.lib.framework](../index.md) / [RobotProgram](./index.md)

# RobotProgram

`interface RobotProgram`

The core robot program to run. The methods in this interface can be overridden in order to
execute code in the specified mode.

### Functions

| Name | Summary |
|---|---|
| [autonomous](autonomous.md) | `open suspend fun autonomous(): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Called immediately after [enable](enable.md) when the robot's mode transitions to autonomous. |
| [disable](disable.md) | `open suspend fun disable(): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Called immediately when the robot becomes disabled. |
| [enable](enable.md) | `open suspend fun enable(): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Called immediately when the robot becomes enabled. This method must exit before [autonomous](autonomous.md), [teleop](teleop.md) or [test](test.md) will be called. |
| [teleop](teleop.md) | `open suspend fun teleop(): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Called immediately after [enable](enable.md) when the robot's mode transitions to teleoperated. |
| [test](test.md) | `open suspend fun test(): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Called immediately after [enable](enable.md) when the robot's mode transitions to test. |
