[meanlib](../../index.md) / [org.team2471.frc.lib.actuators](../index.md) / [MotorController](index.md) / [config](./config.md)

# config

`inline fun config(timeoutMs: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)` = Int.MAX_VALUE, body: `[`ConfigScope`](-config-scope/index.md)`.() -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)`): `[`MotorController`](index.md)

Configures the [MotorController](index.md) with instructions specified in the [body](config.md#org.team2471.frc.lib.actuators.MotorController$config(kotlin.Int, kotlin.Function1((org.team2471.frc.lib.actuators.MotorController.ConfigScope, kotlin.Unit)))/body).

### Parameters

`timeoutMs` - the timeout to use on various motor functions

`body` - the function which configures this [MotorController](index.md)