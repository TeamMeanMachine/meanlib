package org.team2471.frc.lib.vision.limelight

import org.team2471.frc.lib.vision.Fiducial
import org.team2471.frc.lib.vision.PipelineConfig
import org.team2471.frc.lib.vision.PipelineVisionPacket
import org.team2471.frc.lib.vision.QuixVisionCamera
import org.littletonrobotics.junction.LogTable
import org.littletonrobotics.junction.Logger
import org.littletonrobotics.junction.MeanLogger
import org.littletonrobotics.junction.inputs.LoggableInputs
import org.photonvision.PhotonCamera
import org.photonvision.simulation.PhotonCameraSim
import org.photonvision.targeting.PhotonPipelineResult
import org.photonvision.targeting.PhotonTrackedTarget
import org.photonvision.targeting.TargetCorner
import org.wpilib.math.geometry.Transform3d
import org.wpilib.math.linalg.Matrix
import org.wpilib.math.numbers.N1
import org.wpilib.math.numbers.N3
import org.wpilib.math.numbers.N8
import org.wpilib.networktables.NetworkTableInstance
import org.wpilib.system.RobotController
import org.wpilib.system.Timer
import java.util.Optional

class LimelightCamera(
    override val cameraName: String,
    override val transform: Transform3d,
    override val cameraMatrix: Optional<Matrix<N3, N3>>,
    override val distCoeffs: Optional<Matrix<N8, N1>>
): QuixVisionCamera {
    private val pipelineConfigs = arrayOf(PipelineConfig())
    override val cameraSim: PhotonCameraSim =
        PhotonCameraSim(PhotonCamera(cameraName), pipelineConfigs[0].simCameraProp)

    private val loggingName: String = "Inputs/LimelightCamera [$cameraName]"

    private val inputs = LimelightCameraInputs()

    // TODO: make work
    override val isConnected: Boolean = false

    class LimelightCameraInputs : LoggableInputs {
        var latestResult: PhotonPipelineResult = PhotonPipelineResult()

        override fun toLog(table: LogTable) {
//            table.put("Latest Result", latestResult) //TODO: UNCOMMENT WHEN ADVANTAGEKIT 2027 RELEASES
        }

        override fun fromLog(table: LogTable) {
//            latestResult = table.get("Latest Result", latestResult) //TODO: UNCOMMENT WHEN ADVANTAGEKIT 2027 RELEASES
        }
    }

    override fun updateInputs() {
        val corners = NetworkTableInstance.getDefault().getTable(cameraName).getEntry("tcornxy").getDoubleArray(doubleArrayOf())
        val fiducials = NetworkTableInstance.getDefault().getTable(cameraName).getEntry("rawfiducials").getDoubleArray(doubleArrayOf())

        if (corners.size >= 8 && fiducials.size >= 6) {
            val targets = mutableListOf<PhotonTrackedTarget>()
            for (i in 0..(corners.size / 8)-1) {
//                targets.add( //TODO: UNCOMMENT WHEN PHOTONVISION 2027 RELEASES
//                    PhotonTrackedTarget(
//                        0.0, //camToTag[4],
//                        0.0, //camToTag[3],
//                        0.0, //LimelightHelpers.getTA(cameraName),
//                        0.0, //camToTag[5],
//                        fiducials[i * 6].toInt(), //LimelightHelpers.getFiducialID(cameraName).toInt(),
//                        0,
//                        0.0F,
//                        Transform3d(), //robotToTag,
//                        Transform3d(), //robotToTag,
//                        0.0,
//                        mutableListOf<TargetCorner>(
//                            TargetCorner(corners[(i * 8)], corners[(i * 8) + 1]),
//                            TargetCorner(corners[(i * 8) + 2], corners[(i * 8) + 3]),
//                            TargetCorner(corners[(i * 8) + 4], corners[(i * 8) + 5]),
//                            TargetCorner(corners[(i * 8) + 6], corners[(i * 8) + 7]),
//                        ),
//                        mutableListOf<TargetCorner>(
//                            TargetCorner(corners[(i * 8) + 0], corners[(i * 8) + 1]),
//                            TargetCorner(corners[(i * 8) + 2], corners[(i * 8) + 3]),
//                            TargetCorner(corners[(i * 8) + 4], corners[(i * 8) + 5]),
//                            TargetCorner(corners[(i * 8) + 6], corners[(i * 8) + 7]),
//                        )
//                    )
//                )
            }

            val tl = LimelightHelpers.getLatency_Pipeline(cameraName) * 1000
            val cl = LimelightHelpers.getLatency_Capture(cameraName) * 1000
            inputs.latestResult = PhotonPipelineResult(
                0.0.toLong(),
                (RobotController.getTime() - tl - cl).toLong(),
                (RobotController.getTime()),
                0.0.toLong(),
                targets
            )
        } else {
            inputs.latestResult = PhotonPipelineResult()
        }

        Logger.processInputs(loggingName, inputs)
    }

    override fun setPipelineIndex(index: Int) {}

    override val pipelineConfig: PipelineConfig
        get() = pipelineConfigs[0]

    override val fiducialType: Fiducial.Type
        get() = pipelineConfig.fiducialType

    override var allDataPopulated: Boolean = true

    override val latestMeasurement: PipelineVisionPacket
        get() {
            val startTimestamp = Timer.getMonotonicTimestamp()
            val result = inputs.latestResult
            val hasTargets = false//result.hasTargets() //TODO: UNCOMMENT WHEN PHOTONVISION 2027 RELEASES
            if (!hasTargets) {
                return PipelineVisionPacket(false, null, null, -1.0)
            }

            val endTimestamp = Timer.getMonotonicTimestamp()
            MeanLogger.recordOutput("$loggingName/GetLatestMeasurementMs", (endTimestamp - startTimestamp) * 1000.0)

            return PipelineVisionPacket(
                hasTargets,
                null/*result.getBestTarget()*/, //TODO: UNCOMMENT WHEN PHOTONVISION 2027 RELEASES
                null/*result.getTargets()*/, //TODO: UNCOMMENT WHEN PHOTONVISION 2027 RELEASES
                0.0//result.timestampSeconds - 0.03 //TODO: UNCOMMENT WHEN PHOTONVISION 2027 RELEASES
            )
        }
}