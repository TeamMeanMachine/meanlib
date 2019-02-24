[meanlib](../../../index.md) / [org.team2471.frc.lib.actuators](../../index.md) / [MotorController](../index.md) / [ConfigScope](index.md) / [feedbackCoefficient](./feedback-coefficient.md)

# feedbackCoefficient

`var feedbackCoefficient: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)

A coefficient applied to the attached encoder's raw value in order to convert it into a
desired unit of measurement. For example, if 7126 encoder ticks equals 1 foot of drive
distance on your drivetrain, [feedbackCoefficient](./feedback-coefficient.md) should be set to `1.0/7126.0`.

