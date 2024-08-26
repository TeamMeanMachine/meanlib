package org.team2471.frc.lib.vision

import edu.wpi.first.apriltag.AprilTagFieldLayout
import edu.wpi.first.math.geometry.*
import edu.wpi.first.networktables.NetworkTable
import edu.wpi.first.networktables.NetworkTableInstance
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.littletonrobotics.junction.Logger
import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.math.Vector2L
import org.team2471.frc.lib.motion.following.SwerveDrive
import org.team2471.frc.lib.units.*
import org.team2471.frc.lib.util.RobotMode

class Camera(
    val inputTable: NetworkTable,
    val outputTable: NetworkTable,
    val name: String,
    val aprilTagFieldLayout: AprilTagFieldLayout,
    val robotToCamera: Transform3d,
    val robotMode: RobotMode = RobotMode.REAL,
    val isPhotonCamera: Boolean = false
) {

    val io = if (isPhotonCamera) PhotonVisionCamera(inputTable, outputTable, name, robotToCamera, aprilTagFieldLayout) else LimelightCamera(inputTable, outputTable, name, robotToCamera)
    val inputs = CameraIO.CameraIOInputs(name)

    init {
        GlobalScope.launch {
            periodic {
                io.updateInputs(inputs)
                Logger.processInputs("Cameras/", inputs)
            }
        }
    }

    fun reset() {
        io.reset(inputs)
    }

    fun getEstimatedGlobalPose(currentPos: Vector2L, currentHeading: Angle, lookupPose: (Double) -> SwerveDrive.Pose?): GlobalPose {
        return io.getEstimatedGlobalPose(inputs, currentPos, currentHeading, lookupPose)
    }
}