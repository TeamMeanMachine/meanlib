package org.team2471.frc.lib.vision

import edu.wpi.first.apriltag.AprilTagFieldLayout
import edu.wpi.first.math.geometry.*
import edu.wpi.first.networktables.NetworkTable
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.littletonrobotics.junction.Logger
import org.photonvision.PhotonPoseEstimator
import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.coroutines.suspendUntil
import org.team2471.frc.lib.math.*
import org.team2471.frc.lib.units.*
import org.team2471.frc.lib.util.MeanLogger
import org.team2471.frc.lib.util.RobotMode
import org.team2471.frc.lib.util.Timer
import org.team2471.frc.lib.util.calculateAverage
import kotlin.math.pow
import kotlin.math.sqrt

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
    val cameraIntrinsics: CameraIntrinsics = CameraIntrinsics.GenericCamera,
    val robotMode: RobotMode = RobotMode.REAL
) {

    /**
     *  A list of GlobalPoses that contains all pose updates that have occurred since the last frame. May be empty or contain multiple results.
     *
     * @see update
     */
    val latestPoses: List<GlobalPose>
        get() = io.latestGlobalPoses

    val hasResults: Boolean
        get() = io.latestResults.isNotEmpty()

    val isConnected: Boolean
        get() = inputs.isConnected

    // Instantiates the IO layer, which depends on whether the camera is PhotonVision or Limelight or if the robot is simulated or not.
    private val io: CameraIO = when (robotMode) {
        RobotMode.REAL, RobotMode.REPLAY -> {
            println("not sim adslkjfjhalksdjfhalksjdghlkjadfshg;lksajdf;lasdjkf;")
            when (cameraIntrinsics.type) {
                CameraType.PHOTONVISION -> PhotonVisionCamera(
                    inputTable,
                    outputTable,
                    name,
                    robotToCamera,
                    aprilTagFieldLayout,
                    singleTagStrategy = PhotonPoseEstimator.PoseStrategy.LOWEST_AMBIGUITY
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
            println("Is sim adslkjfjhalksdjfhalksjdghlkjadfshg;lksajdf;lasdjkf;")
            when (cameraIntrinsics.type) {
                CameraType.PHOTONVISION -> PhotonVisionSim(
                    inputTable,
                    outputTable,
                    name,
                    robotToCamera,
                    aprilTagFieldLayout,
                    cameraIntrinsics.simCameraProperties,
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
     * This version exists only to be compatible with MeanLib units.
     *
     * @see latestPoses
     */
    fun update(currentPos: Vector2L, currentHeading: Angle, headingRate: Angle) {
        update(currentPos.asMeters.toPose2d(currentHeading), headingRate.asRotation2d)
    }

    /**
     * **THIS IS ESSENTIAL FOR THE CAMERA TO WORK!**
     *
     * Updates the IO layer, which grabs data from the coprocessor, processes it, calculates standard deviations, and updates the latestPoses.
     *
     * @see latestPoses
     */
    fun update(currentPose: Pose2d, headingRatePerSecond: Rotation2d) {
        io.update(inputs, currentPose, headingRatePerSecond)
    }

    suspend fun stdDevTest(): Pair<Double, Vector2L>? {
        val positions: MutableList<Vector2L> = mutableListOf()

        suspendUntil(30) { io.latestResults.isNotEmpty() }
        val latestResult = io.latestResults.last()

        val tagNum = latestResult.numTags
        println("Tag number: $tagNum")

        var averageTagArea = latestResult.avgTagArea
        var tagAreaSamples = 1

        var averagePos = latestResult.pose.translation.asVector2().meters
        var averagePosSamples = 1

        positions.add(latestResult.pose.translation.asVector2().meters)

        val t = Timer().apply { start() }


        periodic {
            if (positions.size >= 100 || t.get() > 25.0) {
                stop()
            }

            for (result in io.latestResults) {
                if (result.numTags == tagNum) {
                    tagAreaSamples ++
                    averagePosSamples ++
                    averageTagArea = calculateAverage(averageTagArea, result.avgTagArea, tagAreaSamples)
                    averagePos = Vector2L(
                        calculateAverage(averagePos.x.asMeters, result.pose.x, averagePosSamples).meters,
                        calculateAverage(averagePos.y.asMeters, result.pose.y, averagePosSamples).meters
                    )
//                    println("${result.pose.translation}")
                }
            }
            MeanLogger.recordOutput("AveragePose", averagePos, 0.0.degrees)
        }

        if (positions.isEmpty()) {
            return null
        } else {

            // IDK what to call it
            var stdDevThingey = Vector2(0.0, 0.0)
            for (position in positions) {
                stdDevThingey += Vector2((position.x.asMeters - averagePos.x.asMeters).pow(2), (position.y.asMeters - averagePos.y.asMeters).pow(2))
            }

            val stdDevM = Vector2(sqrt(stdDevThingey.x / positions.size), sqrt(stdDevThingey.y / positions.size))

            return Pair(averageTagArea, stdDevM.meters)
        }
    }
}