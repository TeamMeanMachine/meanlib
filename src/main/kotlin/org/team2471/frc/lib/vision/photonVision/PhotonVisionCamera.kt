package org.team2471.frc.lib.vision.photonVision

import edu.wpi.first.math.Matrix
import edu.wpi.first.math.geometry.Transform3d
import edu.wpi.first.math.numbers.N1
import edu.wpi.first.math.numbers.N3
import edu.wpi.first.math.numbers.N8
import edu.wpi.first.wpilibj.Timer
import org.team2471.frc.lib.util.isReal
import org.team2471.frc.lib.util.isSim
import org.team2471.frc.lib.vision.Fiducial
import org.team2471.frc.lib.vision.PipelineConfig
import org.team2471.frc.lib.vision.PipelineVisionPacket
import org.team2471.frc.lib.vision.QuixVisionCamera
import org.team2471.frc.lib.vision.QuixVisionSim
import org.ejml.simple.SimpleMatrix
import org.littletonrobotics.junction.LogTable
import org.littletonrobotics.junction.Logger
import org.littletonrobotics.junction.inputs.LoggableInputs
import org.photonvision.PhotonCamera
import org.photonvision.simulation.PhotonCameraSim
import org.photonvision.targeting.PhotonPipelineResult
import java.util.Optional

class PhotonVisionCamera(
    cameraName: String,
    override val transform: Transform3d,
    private val pipelineConfigs: Array<PipelineConfig>
) : QuixVisionCamera {
    override val cameraSim: PhotonCameraSim =
        PhotonCameraSim(PhotonCamera(cameraName), pipelineConfigs[0].simCameraProp)

    private val loggingName: String = "Cameras/PhotonVisionCamera [$cameraName]"
    private val camera: PhotonCamera = if (isReal) PhotonCamera(cameraName) else cameraSim.camera

    private val inputs = PhotonCameraInputs()

    override var allDataPopulated: Boolean = false


    class PhotonCameraInputs : LoggableInputs {
        // TODO: Monitor performance and consider not logging the whole PhotonPipelineResult.
        var pipelineIndex: Int = 0
        var latestResult: PhotonPipelineResult = PhotonPipelineResult()
        var cameraMatrix: Optional<Matrix<N3, N3>> = Optional.empty<Matrix<N3, N3>>()
        var distCoeffs: Optional<Matrix<N8, N1>> = Optional.empty<Matrix<N8, N1>>()
        var allDataPopulated: Boolean = false

        override fun toLog(table: LogTable) {
            table.put("PipelineIndex", pipelineIndex)
//            table.put("LatestResult", latestResult)  // Commented out bc loop cycles where large
            table.put("CameraMatrixIsPresent", cameraMatrix.isPresent)
            if (cameraMatrix.isPresent) table.put("CameraMatrixData", cameraMatrix.get().data)
            table.put("DistCoeffsIsPresent", distCoeffs.isPresent)
            if (distCoeffs.isPresent) table.put("DistCoeffsData", distCoeffs.get().data)
        }

        override fun fromLog(table: LogTable) {
            var allDataPop = true
            pipelineIndex = table.get("PipelineIndex", pipelineIndex)
            latestResult = table.get("LatestResult", latestResult)
            cameraMatrix =
                if (table.get("CameraMatrixIsPresent", false)) {
                    Optional.of<Matrix<N3, N3>>(
                        Matrix<N3, N3>(
                            SimpleMatrix(3, 3, true, *table.get("CameraMatrixData", DoubleArray(9)))
                        )
                    )
                } else {
                    allDataPop = false
                    Optional.empty<Matrix<N3, N3>>()
                }
            distCoeffs =
                if (table.get("DistCoeffsIsPresent", false)) {
                    Optional.of<Matrix<N8, N1>>(
                        Matrix<N8, N1>(
                            SimpleMatrix(8, 1, true, *table.get("DistCoeffsData", DoubleArray(8)))
                        )
                    )
                } else {
                    allDataPop = false
                    Optional.empty<Matrix<N8, N1>>()
                }
            allDataPopulated = allDataPop
        }
    }

    init {
        if (isSim) setPipelineIndex(0)
        QuixVisionSim.addCamera(this)
    }

    override fun updateInputs() {
        inputs.pipelineIndex = camera.pipelineIndex
        // TODO: Handle all results, not just the latest.
        inputs.latestResult = camera.allUnreadResults.lastOrNull() ?: PhotonPipelineResult()
        // Only update these once, since they shouldn't be changing.
        if (inputs.cameraMatrix.isEmpty) {
            val cameraMatrix = camera.cameraMatrix
            if (cameraMatrix.isPresent) inputs.cameraMatrix = cameraMatrix
        }
        if (inputs.distCoeffs.isEmpty) {
            val distCoeffs = camera.distCoeffs
            if (distCoeffs.isPresent) inputs.distCoeffs = distCoeffs
        }
        allDataPopulated = inputs.allDataPopulated
//        Logger.processInputs(loggingName, inputs)
    }

    override fun setPipelineIndex(index: Int) {
        if (index > pipelineConfigs.size) {
            println("Invalid pipeline index: $index")
            return
        }
        camera.pipelineIndex = index
    }

    override val pipelineConfig: PipelineConfig
        get() = pipelineConfigs[inputs.pipelineIndex]

    override val cameraMatrix: Optional<Matrix<N3, N3>>
        get() = inputs.cameraMatrix

    override val distCoeffs: Optional<Matrix<N8, N1>>
        get() = inputs.distCoeffs

    override val fiducialType: Fiducial.Type
        get() = pipelineConfig.fiducialType

    override val latestMeasurement: PipelineVisionPacket
        get() {
            val startTimestamp = Timer.getFPGATimestamp()
            val result = inputs.latestResult
            val hasTargets = result.hasTargets()
            if (!hasTargets) {
                return PipelineVisionPacket(false, null, null, -1.0)
            }

            val endTimestamp = Timer.getFPGATimestamp()
            Logger.recordOutput("$loggingName/GetLatestMeasurementMs", (endTimestamp - startTimestamp) * 1000.0)

            return PipelineVisionPacket(
                hasTargets,
                result.getBestTarget(),
                result.getTargets(),
                result.timestampSeconds - 0.03
            )
        }
}