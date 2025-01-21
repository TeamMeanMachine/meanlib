package org.team2471.frc.lib.vision

import edu.wpi.first.networktables.NetworkTableInstance
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.math.Vector2L
import org.team2471.frc.lib.units.*

class Quest() {
    val table = NetworkTableInstance.getDefault().getTable("questnav")

    private val batteryPercentEntry = table.getDoubleTopic("batteryPercent").subscribe(0.0)
    private val frameCountEntry = table.getIntegerTopic("frameCount").subscribe(0)
    private val timestampEntry = table.getDoubleTopic("timestamp").subscribe(0.0)
    private val anglesEntry = table.getFloatArrayTopic("eulerAngles").subscribe(floatArrayOf(0.0f, 0.0f, 0.0f))
    private val positionsEntry = table.getFloatArrayTopic("position").subscribe(floatArrayOf(0.0f, 0.0f, 0.0f))
    private val isConnectedEntry = table.getEntry("isConnected")

    var isConnected: Boolean = false
        private set(value) {
            field = value
            isConnectedEntry.setBoolean(value)
        }


    var pitch: Angle = 0.0.degrees
        get() = anglesEntry.get()[0].degrees + field
        set(value) { field += value - pitch}

    var yaw: Angle
        get() = -anglesEntry.get()[1].degrees + yawOffset
        set(value) { yawOffset += value - yaw }

    private var yawOffset = 0.0.degrees

    var roll: Angle = 0.0.degrees
        get() = anglesEntry.get()[2].degrees + field
        set(value) { field += value - roll }


    var position: Vector2L = Vector2L(0.0.inches, 0.0.inches)
        get() {
            val x = positionsEntry.get()[2].meters
            val y = -positionsEntry.get()[0].meters

            return (Vector2L(x, y) + field).rotate(yawOffset) - oculusOffset.rotate(yaw)
        }
        set(value) { field += (value - position).rotate(-yawOffset) }

    val oculusOffset = Vector2L(11.0.inches, 3.0.inches)

    var height: Length = 0.0.meters
        get() = positionsEntry.get()[1].meters + field
        set(value) { field += value - height }


    val batteryPercent: Double get() = batteryPercentEntry.get()
    val frameCount: Int get() = frameCountEntry.get().toInt()
    val timestamp: Double get() = timestampEntry.get()
    private var prevTimestamp = timestamp

    init {
        @OptIn(DelicateCoroutinesApi::class)
        GlobalScope.launch { //isConnected loop
            periodic(0.5) {
                val ts = timestamp
                isConnected = ts != prevTimestamp
                prevTimestamp = ts
            }
        }
    }
}