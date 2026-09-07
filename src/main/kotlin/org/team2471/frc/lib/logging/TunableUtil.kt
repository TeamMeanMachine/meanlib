package org.team2471.frc.lib.logging

import org.wpilib.networktables.NetworkTableInstance
import org.wpilib.telemetry.TelemetryTable
import org.wpilib.tunable.Tunable
import org.wpilib.tunable.TunableConfig
import org.wpilib.tunable.TunableTable
import org.wpilib.tunable.Tunables

fun <T> TelemetryTable.createTunable(name: String, initialValue: T, persistent: Boolean = false): Tunable<T> {
    val tunableTable = Tunables.getTable(this.path)
    return tunableTable.createTunable(name, initialValue, persistent)
}

fun <T> TunableTable.createTunable(name: String, initialValue: T, persistent: Boolean = false): Tunable<T> {
    // Create a tunable with a name and a persistent property
    var persistentValue: T? = null
    val ntEntry = NetworkTableInstance.getDefault().getTable("Tunables").getEntry(this.path.substring(1) + name)
    if (persistent) {
        try {
            persistentValue = ntEntry.value.value as T?
            println("Setting persistent value for ${ntEntry.name}: $persistentValue")
        } catch (_: Exception) {
            println("Failed to get persistent value for $name ${ntEntry.name}")
        }
    }
    val tunable = Tunable.createConfig(persistentValue ?: initialValue, TunableConfig().withProperty("persistent", persistent.toString()))
    // Publish the tunable to the table
    this.publish(name, tunable)
    return tunable
}
