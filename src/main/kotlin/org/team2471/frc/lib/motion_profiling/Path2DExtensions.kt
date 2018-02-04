package org.team2471.frc.lib.motion_profiling

import edu.wpi.first.networktables.NetworkTableInstance

private val pathsTable = NetworkTableInstance.getDefault().getTable("Shared Paths")

fun Path2D.writeToNetworkTables() {
    pathsTable.getEntry(name).forceSetString(toJsonString())
}

fun pathFromNetworkTables(name: String): Path2D? {
    val jsonString = pathsTable.getEntry(name).getString(null) ?: return null
    return Path2D.fromJsonString(jsonString)
}
