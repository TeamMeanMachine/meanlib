package org.team2471.frc.lib.vision

import edu.wpi.first.math.geometry.Pose2d
import org.littletonrobotics.junction.LogTable
import org.littletonrobotics.junction.inputs.LoggableInputs
import org.team2471.frc.lib.math.Vector2L
import org.team2471.frc.lib.motion.following.SwerveDrive
import org.team2471.frc.lib.units.Angle

interface CameraIO {
    class CameraIOInputs(val name: String): LoggableInputs {

        var isConnected: Boolean = false
        var cameraResult: CameraResult = CameraResult.EmptyCameraResult

        override fun toLog(table: LogTable) {
            table.put("$name/isConnected", isConnected)
            table.put("$name/cameraResult", cameraResult.toArray())
        }

        override fun fromLog(table: LogTable) {
            isConnected = table.get("$name/isConnected", isConnected)
            cameraResult = CameraResult.fromArray(table.get("$name/cameraResult", cameraResult.toArray()))
        }
    }

    fun updateInputs(inputs: CameraIOInputs) {}
    fun reset(inputs: CameraIOInputs) {}
    fun getEstimatedGlobalPose(inputs: CameraIOInputs, currentPos: Vector2L, currentHeading: Angle, headingRate: Angle, lookupPose: (Double) -> SwerveDrive.Pose?): GlobalPose
    fun getTagRelativePose(tagID: Int): Pose2d?
}

/**
    An empty camera. Used during simulation because limelights cannot currently be simulated.
    @author Thatcher Moore
 */
class EmptyCamera(): CameraIO {
    override fun getEstimatedGlobalPose(
        inputs: CameraIO.CameraIOInputs,
        currentPos: Vector2L,
        currentHeading: Angle,
        headingRate: Angle,
        lookupPose: (Double) -> SwerveDrive.Pose?
    ): GlobalPose {
        return GlobalPose.EmptyGlobalPose
    }

    override fun getTagRelativePose(tagID: Int): Pose2d {
        return Pose2d()
    }
}