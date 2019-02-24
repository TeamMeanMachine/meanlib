[meanlib](../../index.md) / [org.team2471.frc.lib.actuators](../index.md) / [MotorController](index.md) / [current](./current.md)

# current

`val current: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)

The current being drawn by the [MotorController](index.md).
Note that this will only work if the [MotorController](index.md) is a Talon SRX. Attempts to use this method on
non-Talons will result in an [IllegalStateException](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-illegal-state-exception/index.html).

**See Also**

[CTREMotorController.getMotorOutputPercent](#)

