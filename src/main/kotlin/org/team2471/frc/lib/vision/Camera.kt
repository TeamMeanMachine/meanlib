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
 * A class that represents a physical camera on the robot. This is mainly a pass through for the IO layers. They have all the logic and this just contains one and implements their functions.
 *
 * @author Thatcher Moore
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
    val cameraType: CameraType = CameraType.PHOTONVISION
) {

    // Instantiates the IO layer, which depends on whether the camera is Photonvision or Limelight or if the robot is simulated or not.
    val io: CameraIO = when (robotMode) {
        RobotMode.REAL,  RobotMode.REPLAY-> {
            when (cameraType) {
                CameraType.PHOTONVISION -> PhotonVisionCamera(inputTable, outputTable, name, robotToCamera, aprilTagFieldLayout)
                CameraType.LIMELIGHT -> LimelightCamera(inputTable, outputTable, name, robotToCamera)
            }
        }
        RobotMode.SIM -> {
            when (cameraType) {
                CameraType.PHOTONVISION -> PhotonVisionSim(inputTable, outputTable, name, robotToCamera, aprilTagFieldLayout)
                CameraType.LIMELIGHT -> EmptyCamera()
            }
        }
    }

    // Instantiates the inputs
    val inputs = CameraIO.CameraIOInputs(name)

    init {
        GlobalScope.launch {
            periodic {
                // Updates and logs the inputs
                io.updateInputs(inputs)
                Logger.processInputs("Cameras/", inputs)
            }
        }
    }

    // Resets the cameras. Only primarily used for the PhotonVision ones
    fun reset() {
        io.reset(inputs)
    }

    // Gets the estimated global pose based on the current position, current heading, and a lookup function that gives a history of drive positions.
    // Just a pass through for the IO layer
    fun getEstimatedGlobalPose(currentPos: Vector2L, currentHeading: Angle, headingRate: Angle, lookupPose: (Double) -> SwerveDrive.Pose?): GlobalPose {
        return io.getEstimatedGlobalPose(inputs, currentPos, currentHeading, headingRate, lookupPose)
    }

    fun get2DTarget(tagID: Int): Target2D {
        return io.get2DTarget(tagID)
    }
}