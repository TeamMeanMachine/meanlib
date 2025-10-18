package org.team2471.frc.lib.localization

import edu.wpi.first.math.geometry.Pose2d
import edu.wpi.first.networktables.*
import edu.wpi.first.util.struct.StructBuffer
import org.team2471.frc.lib.vision.Fiducial
import org.team2471.frc.lib.vision.QuixVisionCamera
import org.littletonrobotics.junction.AutoLog
import org.littletonrobotics.junction.Logger
import java.util.*
import java.util.concurrent.atomic.AtomicReference

/**
 * The NTManager class is responsible for managing network table interactions related to robot
 * localization. It handles publishing and subscribing to various topics such as measurements,
 * targets, and camera information. It also maintains the latest pose estimate and updates inputs
 * accordingly.
 */
class NTManager {
    private val localizerTable: NetworkTable

    // Combined Robot to DS odometry and vision measurements
    private val measurementsPub: DoubleArrayPublisher

    // Robot to DS targets
    private val targetPub: StructArrayPublisher<Fiducial?>

    // Robot to Camera transforms and intrinsics
    private val cameraInfosPublisher: StructArrayPublisher<CameraInfo?>

    // Use an AtomicReference to make updating the value thread-safe
    private val latestPoseEstimateReference = AtomicReference<PoseEstimate>()

    private val inputs = PoseEstimateInputsAutoLogged()

    @AutoLog
    open class PoseEstimateInputs {
        @JvmField var id: Int = 0
        @JvmField var pose: Pose2d? = null
        @JvmField var hasVision: Boolean = false
    }

    /**
     * Manages the NetworkTables (NT) interactions for localization. This class sets up publishers and
     * subscribers for various localization-related topics.
     *
     *
     * It initializes the following publishers:
     *
     *
     *  * Measurements publisher for double array topic "measurements"
     *  * Targets publisher for struct array topic "targets" with Fiducial structure
     *  * Cameras publisher for struct array topic "cameras" with CameraInfo structure
     *
     *
     *
     * It also sets up a listener for the "estimates" topic to update the latest pose estimate.
     *
     *
     * Usage:
     *
     * <pre>`NTManager ntManager = new NTManager();
    `</pre> *
     */
    init {
        val inst = NetworkTableInstance.getDefault()
        localizerTable = inst.getTable("localizer")
        measurementsPub = localizerTable.getDoubleArrayTopic("measurements").publish(PubSubOption.sendAll(true))
        targetPub = localizerTable.getStructArrayTopic<Fiducial?>("targets", Fiducial.struct)
            .publish(PubSubOption.sendAll(true))
        cameraInfosPublisher = localizerTable.getStructArrayTopic<CameraInfo?>("cameras", CameraInfo.struct)
            .publish(PubSubOption.sendAll(true))

        // Setup listener for when the estimate is updated.
        val estimatesSub = localizerTable.getStructTopic("estimates", PoseEstimate.struct)
            .subscribe(PoseEstimate(), PubSubOption.sendAll(true))
        val poseEstimateStructBuffer = StructBuffer.create(PoseEstimate.struct)
        inst.addListener(estimatesSub, EnumSet.of<NetworkTableEvent.Kind>(NetworkTableEvent.Kind.kValueAll)) { event: NetworkTableEvent ->
            latestPoseEstimateReference.set(poseEstimateStructBuffer.read(event.valueData.value.getRaw()))
        }
    }

    /**
     * Publishes a measurement to the network table.
     *
     * @param measurement The measurement to be published.
     * @param id The identifier for the measurement.
     */
    fun publishMeasurement(measurement: Measurement, id: Int) {
        measurementsPub.set(measurement.toArray(id))
        NetworkTableInstance.getDefault().flush()
    }

    /**
     * Publishes an array of fiducial targets to the network table.
     *
     * @param targets An array of Fiducial objects representing the targets to be published.
     */
    fun publishTargets(targets: Array<Fiducial>) {
        targetPub.set(targets)
    }

    var camerasPublished = false
    /**
     * Publishes information about a list of cameras to the network table.
     *
     * @param cameras An ArrayList of QuixVisionCamera objects representing the cameras to be
     * published. Each camera's transform, camera matrix, and distortion coefficients will be used
     * to create a CameraInfo object, which will then be published.
     */
    fun publishCameras(cameras: List<QuixVisionCamera>) {
        if (camerasPublished) return
        var allCamerasPublished = true
        val array = cameras.map {
            if (!it.allDataPopulated) allCamerasPublished = false
            CameraInfo(it.transform, it.cameraMatrix, it.distCoeffs)
        }.toTypedArray()

        cameraInfosPublisher.set(array)
        camerasPublished = allCamerasPublished
    }

    /**
     * Updates the input values with the latest pose estimate if available.
     *
     *
     * This method retrieves the latest pose estimate and updates the input values with the ID,
     * pose, and vision status from the estimate. If the latest pose estimate is null, the input
     * values are not updated. The updated inputs are then processed by the Logger.
     */
    fun updateInputs() {
        val latestEstimate = latestPoseEstimateReference.get()
        if (latestEstimate != null) {
            inputs.id = latestEstimate.id
            inputs.pose = latestEstimate.pose
            inputs.hasVision = latestEstimate.hasVision
        }
        Logger.processInputs("Inputs/NTManager", inputs)
    }

    /**
     * Retrieves the latest pose estimate from NT.
     *
     * @return A [PoseEstimate] object containing the latest pose information.
     */
    val latestPoseEstimate: PoseEstimate
        get() = PoseEstimate(inputs.id, inputs.pose, inputs.hasVision)
}
