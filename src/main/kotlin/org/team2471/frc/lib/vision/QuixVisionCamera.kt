package org.team2471.frc.lib.vision

import edu.wpi.first.math.Matrix
import edu.wpi.first.math.geometry.Transform3d
import edu.wpi.first.math.numbers.N1
import edu.wpi.first.math.numbers.N3
import edu.wpi.first.math.numbers.N8
import org.photonvision.simulation.PhotonCameraSim
import java.util.*

interface QuixVisionCamera {
    /** Returns the simulated camera object.  */
    val cameraSim: PhotonCameraSim

    fun updateInputs()

    /** Returns the latest measurement.  */
    val latestMeasurement: PipelineVisionPacket

    /** Select the active pipeline index.  */
    fun setPipelineIndex(index: Int)

    /** Get the active pipeline config.  */
    val pipelineConfig: PipelineConfig

    /** Returns the robot-to-camera transform.  */
    val transform: Transform3d

    val cameraMatrix: Optional<Matrix<N3, N3>>

    val distCoeffs: Optional<Matrix<N8, N1>>

    /** Returns the type of fiducials this camera is tracking.  */
    val fiducialType: Fiducial.Type

    var allDataPopulated: Boolean
}
