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

/**
 * A class that represents a physical camera on the robot
 *
 * @param inputTable the network table instance that receives the data from the camera
 * @param outputTable the network table instance to output data to
 * @param name the name of the camera (ex. limelight-shooter)
 * @param aprilTagFieldLayout The layout of the apriltags on the current playing field
 * @param robotToCamera the transformation from the center of the robot to the camera in meters and following WPI conventions. Look [here](https://docs.wpilib.org/en/stable/docs/software/basic-programming/coordinate-system.html)
 * @param robotMode currently unused. May be used for simulation in the future.
 * @param isPhotonCamera whether the camera is running Photonvision or Limelight software.
 */
class Camera(
    val inputTable: NetworkTable,
    val outputTable: NetworkTable,
    val name: String,
    val aprilTagFieldLayout: AprilTagFieldLayout,
    val robotToCamera: Transform3d,
    val robotMode: RobotMode = RobotMode.REAL,
    val isPhotonCamera: Boolean = false
) {

    // Instantiates the IO layer, which depends on whether the camera is Photonvision or Limelight.
    val io = if (isPhotonCamera) PhotonVisionCamera(inputTable, outputTable, name, robotToCamera, aprilTagFieldLayout) else LimelightCamera(inputTable, outputTable, name, robotToCamera)
    // Instantiates the inputs
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