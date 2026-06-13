package org.team2471.frc.lib.vision

import org.team2471.frc.lib.units.asRotation2d
import org.team2471.frc.lib.units.degrees
import org.photonvision.simulation.SimCameraProperties
import org.wpilib.math.linalg.MatBuilder
import org.wpilib.math.linalg.Matrix
import org.wpilib.math.numbers.N1
import org.wpilib.math.numbers.N3
import org.wpilib.math.numbers.N8

/** Describes a given vision camera pipeline configuration.  */
class PipelineConfig(
    val fiducialType: Fiducial.Type = Fiducial.Type.APRILTAG,
// pixels
    val imageWidth: Int = 1280,
    // pixels
    val imageHeight: Int = 800,

    //used for sim only
    val simCameraProp: SimCameraProperties = SimCameraProperties().apply {
        setCalibration(resWidth, resHeight, 70.2.degrees.asRotation2d)
        setCalibError(0.001, 0.005) // Values from docs. Should change
        fps = 20.0
        avgLatencyMs = 20.0
        latencyStdDevMs = 3.0
    },
    val camIntrinsics: Matrix<N3, N3> = simCameraProp.intrinsics,
    // used for sim only
    val distCoeffs: Matrix<N8, N1> = simCameraProp.distCoeffs
)