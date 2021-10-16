package org.team2471.frc.lib.motion_profiling

import org.team2471.frc.lib.motion_profiling.following.DrivetrainParameters.Companion.moshiAdapter
import org.team2471.frc.lib.motion_profiling.following.RobotParameters
import org.team2471.frc.lib.motion_profiling.following.DrivetrainParameters
import org.team2471.frc.lib.motion_profiling.following.SwerveParameters
import org.team2471.frc.lib.motion_profiling.Autonomous
import org.team2471.frc.lib.motion_profiling.Autonomi
import edu.wpi.first.networktables.NetworkTableInstance
import edu.wpi.first.networktables.NetworkTableEntry
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.lang.Exception
import java.util.LinkedHashMap

class Autonomi {
    var robotParameters = RobotParameters(0.0, 0.0)
    var drivetrainParameters: DrivetrainParameters = SwerveParameters(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, false)
    var mapAutonomous: MutableMap<String, Autonomous> = LinkedHashMap()
    operator fun get(name: String): Autonomous? {
        return mapAutonomous[name]
    }

    fun put(autonomous: Autonomous) {
        mapAutonomous[autonomous.name] = autonomous
    }

    fun getPath(autoName: String, pathName: String?): Path2D {
        val autonomous = get(autoName)
        return autonomous!![pathName]
    }

    fun toJsonString(): String {
        return jsonAdapter.toJson(this)
    }

    private fun fixUpTailAndPrevPointers() {
        for ((_, value) in mapAutonomous) {
            value.fixUpTailAndPrevPointers()
        }
    }

    fun publishToNetworkTables(networkTableInstance: NetworkTableInstance) {
        val json = toJsonString()
        val table = networkTableInstance.getTable("PathVisualizer")
        val entry = table.getEntry("Autonomi")
        entry.setString(json)
    }

    fun readFromNetworkTables(networkTableInstance: NetworkTableInstance): String {
        val table = networkTableInstance.getTable("PathVisualizer")
        val entry = table.getEntry("Autonomi")
        println(entry.getString("default"))
        return entry.getString("")
    }

    companion object {
        private val jsonAdapter = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .add(moshiAdapter)
            .build()
            .adapter(Autonomi::class.java).indent("\t")

        fun fromJsonString(json: String?): Autonomi? {
            val autonomi: Autonomi? = try {
                jsonAdapter.fromJson(json)
            } catch (e: Exception) {
                println("Constructing Autonomi class from json failed.")
                return null
            }
            autonomi?.fixUpTailAndPrevPointers()
            return autonomi
        }
    }
}