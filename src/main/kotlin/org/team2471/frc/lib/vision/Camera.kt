package org.team2471.frc.lib.vision

import edu.wpi.first.apriltag.AprilTagFieldLayout
import edu.wpi.first.math.geometry.Transform3d
import edu.wpi.first.networktables.NetworkTable
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.littletonrobotics.junction.Logger
import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.math.Vector2L
import org.team2471.frc.lib.units.Angle
import org.team2471.frc.lib.util.RobotMode

/**
 * **NOTE:**
 * **THE UPDATE FUNCTION *MUST* BE CALLED *EVERY* FRAME FOR THIS TO WORK**
 *
 * A class that represents a physical camera on the robot and wraps the IO layers. They contain all the logic for calculating poses and standard deviations and this just instantiates and wraps their functions. Each instance of this class launches a new coroutine and logs data.
 *
 * @param inputTable the network table instance that receives the data from the camera
 * @param outputTable the network table instance to output data to
 * @param name the name of the camera (ex. limelight-shooter)
 * @param aprilTagFieldLayout The layout of the AprilTags on the current playing field
 * @param robotToCamera the transformation from the center of the robot to the camera in meters and following WPI conventions. Look [here](https://docs.wpilib.org/en/stable/docs/software/basic-programming/coordinate-system.html)
 * @param robotMode determines whether to run PhotonVision simulation
 * @param cameraType whether the camera is running PhotonVision or Limelight software
 *
 * @property latestPoses contains a list of all pose updates that have occurred since the last frame. May be empty or contain multiple results.
 *
 * @author Thatcher Moore
 */
class Camera(
    val inputTable: NetworkTable,
    val outputTable: NetworkTable,
    val name: String,
    val aprilTagFieldLayout: AprilTagFieldLayout,
    val robotToCamera: Transform3d,
    val robotMode: RobotMode = RobotMode.REAL,
    val cameraType: CameraType = CameraType.PHOTONVISION,
) {

    /**
     *  A list of GlobalPoses that contains all pose updates that have occurred since the last frame. May be empty or contain multiple results.
     *
     * @see update
     */
    val latestPoses: List<GlobalPose>
        get() = io.latestGlobalPoses

    // Instantiates the IO layer, which depends on whether the camera is PhotonVision or Limelight or if the robot is simulated or not.
    private val io: CameraIO = when (robotMode) {
        RobotMode.REAL, RobotMode.REPLAY -> {
            when (cameraType) {
                CameraType.PHOTONVISION -> PhotonVisionCamera(
                    inputTable,
                    outputTable,
                    name,
                    robotToCamera,
                    aprilTagFieldLayout
                )

                CameraType.LIMELIGHT -> LimelightCamera(
                    inputTable,
                    outputTable,
                    name,
                    robotToCamera
                )
            }
        }

        RobotMode.SIM -> {
            when (cameraType) {
                CameraType.PHOTONVISION -> PhotonVisionSim(
                    inputTable,
                    outputTable,
                    name,
                    robotToCamera,
                    aprilTagFieldLayout
                )

                CameraType.LIMELIGHT -> EmptyCamera()
            }
        }
    }

    // Instantiates the inputs
    private val inputs = CameraIO.CameraIOInputs(name)

    init {
        @OptIn(DelicateCoroutinesApi::class)
        GlobalScope.launch {
            periodic {
                // Updates and logs the inputs
                io.updateInputs(inputs)
                Logger.processInputs("Cameras/", inputs)
            }
        }
    }

    /**
     * Resets the camera, which, for PhotonVision, rechecks whether the camera exists. On Limelight this does nothing.
     */
    fun reset() {
        io.reset(inputs)
    }

    /**
     * **THIS IS ESSENTIAL FOR THE CAMERA TO WORK!**
     *
     * Updates the IO layer, which grabs data from the coprocessor, processes it, calculates standard deviations, and updates the latestPoses.
     *
     * @see latestPoses
     */
    fun update(currentPos: Vector2L, currentHeading: Angle, headingRate: Angle) {
        io.update(inputs, currentPos, currentHeading, headingRate)
    }
}