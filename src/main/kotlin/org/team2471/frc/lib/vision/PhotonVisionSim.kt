package org.team2471.frc.lib.vision

import edu.wpi.first.apriltag.AprilTagFieldLayout
import edu.wpi.first.math.geometry.*
import edu.wpi.first.networktables.NetworkTable
import edu.wpi.first.networktables.NetworkTableEntry
import org.littletonrobotics.junction.Logger
import org.photonvision.PhotonCamera
import org.photonvision.PhotonPoseEstimator
import org.photonvision.simulation.PhotonCameraSim
import org.photonvision.simulation.VisionSystemSim
import org.team2471.frc.lib.math.*
import org.team2471.frc.lib.motion.following.SwerveDrive
import org.team2471.frc.lib.motion_profiling.MotionCurve
import org.team2471.frc.lib.units.*
import org.team2471.frc.lib.util.RobotMode
import org.team2471.frc.lib.util.robotMode


/**
 *
 * This is the IO layer for a simulated PhotonVision camera. Very similar to PhotonVisionCamera with a few differences. It contains most of the logic for filtering, calculating standard deviations, and getting a pose from the camera.
 *
 * @author Thatcher Moore
 * @param inputTable the network table instance that receives the data from the camera
 * @param outputTable the network table instance to output data to
 * @param name the name of the camera (ex. limelight-shooter)
 * @param aprilTagFieldLayout The layout of the apriltags on the current playing field
 * @param robotToCamera the transformation from the center of the robot to the camera in meters and following WPI conventions. Look [here](https://docs.wpilib.org/en/stable/docs/software/basic-programming/coordinate-system.html)
 * @param singleTagStrategy The pose estimation strategy to use when only a single target is seen. See a list of strategies [here](https://docs.photonvision.org/en/latest/docs/programming/photonlib/robot-pose-estimator.html)
 * @param multiTagStrategy The pose estimation strategy to use when more than one target is seen. See a list of strategies [here](https://docs.photonvision.org/en/latest/docs/programming/photonlib/robot-pose-estimator.html)
 */
    class PhotonVisionSim(
    private val inputTable: NetworkTable,
    private val outputTable: NetworkTable,
    val name: String,
    private val robotToCamera: Transform3d,
    private val aprilTagFieldLayout: AprilTagFieldLayout,
    private val singleTagStrategy: PhotonPoseEstimator.PoseStrategy = PhotonPoseEstimator.PoseStrategy.CLOSEST_TO_REFERENCE_POSE,
    private val multiTagStrategy: PhotonPoseEstimator.PoseStrategy = PhotonPoseEstimator.PoseStrategy.MULTI_TAG_PNP_ON_COPROCESSOR,
): CameraIO {
    val camera = PhotonVisionCamera(
        inputTable,
        outputTable,
        name,
        robotToCamera,
        aprilTagFieldLayout,
        singleTagStrategy,
        multiTagStrategy
    )

    val cameraSim = PhotonCameraSim(camera.photonCam, cameraProperties).apply {
        enableRawStream(true)
        enableProcessedStream(true)
        enableDrawWireframe(true)
    }

    val visionSystemSim = VisionSystemSim(name).apply {
        addAprilTags(aprilTagFieldLayout)
        addCamera(cameraSim, Transform3d(robotToCamera.translation, Rotation3d(robotToCamera.rotation.x, robotToCamera.rotation.y, robotToCamera.rotation.z)))
    }

    override var latestResults: MutableList<CameraResult> = mutableListOf()
        get() = camera.latestResults
    override var latestGlobalPoses: MutableList<GlobalPose> = mutableListOf()
        get() = camera.latestGlobalPoses

    override fun reset(inputs: CameraIO.CameraIOInputs) {
        camera.reset(inputs)
    }

    override fun update(inputs: CameraIO.CameraIOInputs, currentPos: Vector2L, currentHeading: Angle, headingRate: Angle) {
        visionSystemSim.update(currentPos.asMeters.toPose2d(currentHeading))
        camera.update(inputs, currentPos, currentHeading, headingRate)
    }

    override fun updateInputs(inputs: CameraIO.CameraIOInputs) {
        camera.updateInputs(inputs)
    }
}