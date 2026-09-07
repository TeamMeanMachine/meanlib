package org.team2471.frc.lib.logging

import org.wpilib.networktables.NetworkTableInstance
import org.wpilib.telemetry.TelemetryTable
import org.wpilib.tunable.Tunable
import org.wpilib.tunable.TunableConfig
import org.wpilib.tunable.TunableTable
import org.wpilib.tunable.Tunables

fun <T> TelemetryTable.getTunable(name: String, initialValue: T, persistent: Boolean = false, tunableConfig: TunableConfig = TunableConfig(), onTune: (Tunable<T>) -> Unit = {}): Tunable<T> {
    val tunableTable = Tunables.getTable(this.path)
    return tunableTable.getTunable(name, initialValue, persistent, tunableConfig, onTune)
}

inline fun <T> TunableTable.getTunable(name: String, initialValue: T, persistent: Boolean = false, tunableConfig: TunableConfig = TunableConfig(), crossinline onTune: (Tunable<T>) -> Unit = {}): Tunable<T> {
    try {
        lateinit var tunable: Tunable<T>
        val config = tunableConfig.withProperty("persistent", persistent.toString()).withOnTune { onTune(tunable) } // Configure persistence and onTune action

        var persistentValue: T? = null
        val ntEntry = NetworkTableInstance.getDefault().getTable("Tunables").getEntry(this.path.substring(1) + name)
        if (persistent) {
            try {
                persistentValue = ntEntry.value.value as T?
//                println("Setting persistent value for ${ntEntry.name.substring(1)}: $persistentValue")
            } catch (_: Exception) {
                println("Failed to get persistent value for $name ${ntEntry.name}")
            }
        }

        tunable = Tunable.createConfig(persistentValue ?: initialValue, config)

        this.publish(name, tunable)// Publish the tunable to the table
        return tunable
    } catch (e: Exception) {
        println("Failed to create tunable $name")
        println(e)
        throw e
    }
}

inline fun <reified T> TelemetryTable.getTunable(name: String, noinline getter: () -> T, noinline setter: (T) -> Unit): Tunable<T> {
    val tunableTable = Tunables.getTable(this.path)
    return tunableTable.getTunable(name, getter, setter)
}

inline fun <reified T> TunableTable.getTunable(name: String, noinline getter: () -> T, noinline setter: (T) -> Unit): Tunable<T> {
    val tunable = Tunable.create(getter, setter, T::class.java)
    this.publish(name, tunable)
    return tunable
}

