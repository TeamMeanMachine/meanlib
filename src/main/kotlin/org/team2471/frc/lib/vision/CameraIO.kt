package org.team2471.frc.lib.vision

import org.littletonrobotics.junction.LogTable
import org.littletonrobotics.junction.inputs.LoggableInputs
import org.team2471.frc.lib.math.Vector2L
import org.team2471.frc.lib.units.Angle

interface CameraIO {
    class CameraIOInputs(val name: String) : LoggableInputs {

        var isConnected: Boolean = false
        var cameraResults: ArrayList<CameraResult> = ArrayList(3)

        override fun toLog(table: LogTable) {
            table.put("$name/isConnected", isConnected)
            table.put("$name/cameraResult", *cameraResults.toTypedArray())
        }

        override fun fromLog(table: LogTable) {
            isConnected = table.get("$name/isConnected", isConnected)
            cameraResults = arrayListOf(*table.get("$name/cameraResult", *cameraResults.toTypedArray()))
        }
    }

    var latestResults: MutableList<CameraResult>
    var latestGlobalPoses: MutableList<GlobalPose>

    fun updateInputs(inputs: CameraIOInputs) {}
    fun update(inputs: CameraIOInputs, currentPos: Vector2L, currentHeading: Angle, headingRate: Angle) {}
    fun reset(inputs: CameraIOInputs) {}
}

/**
 * An empty camera. Used during simulation because limelights cannot currently be simulated.
 *
 * @author Thatcher Moore
 */
class EmptyCamera : CameraIO {
    override var latestResults: MutableList<CameraResult> = mutableListOf()
    override var latestGlobalPoses: MutableList<GlobalPose> = mutableListOf()
}