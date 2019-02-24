[meanlib](../../../index.md) / [org.team2471.frc.lib.actuators](../../index.md) / [MotorController](../index.md) / [ConfigScope](index.md) / [currentLimit](./current-limit.md)

# currentLimit

`fun currentLimit(continuousLimit: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)`, peakLimit: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)`, peakDuration: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)

Limits the current to a [continuousLimit](current-limit.md#org.team2471.frc.lib.actuators.MotorController.ConfigScope$currentLimit(kotlin.Int, kotlin.Int, kotlin.Int)/continuousLimit), [peakLimit](current-limit.md#org.team2471.frc.lib.actuators.MotorController.ConfigScope$currentLimit(kotlin.Int, kotlin.Int, kotlin.Int)/peakLimit) and [peakDuration](current-limit.md#org.team2471.frc.lib.actuators.MotorController.ConfigScope$currentLimit(kotlin.Int, kotlin.Int, kotlin.Int)/peakDuration).

### Parameters

`continuousLimit` - the continuous allowable current-draw

`peakLimit` - the peak allowable current

`peakDuration` - the peak allowable duration

**See Also**

[CTRETalonSRX.configContinuousCurrentLimit](#)

[CTRETalonSRX.configPeakCurrentLimit](#)

[CTRETalonSRX.configPeakCurrentDuration](#)

