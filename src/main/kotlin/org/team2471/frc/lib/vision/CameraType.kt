package org.team2471.frc.lib.vision

import edu.wpi.first.math.geometry.Rotation2d
import org.photonvision.simulation.SimCameraProperties
import org.team2471.frc.lib.units.Angle
import org.team2471.frc.lib.units.asRotation2d
import org.team2471.frc.lib.units.degrees
import org.team2471.frc.lib.units.radians
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt

data class CameraIntrinsics(val type: CameraType, val processor: Processor, val resWidth: Double, val resHeight: Double, val horizontalFOV: Angle) {
    val diagonalFOV = horizontalFOV * sqrt(resWidth.pow(2) + resHeight.pow(2)) / resWidth

    val simCameraProperties: SimCameraProperties
        get() = SimCameraProperties().apply {
        setCalibration(resWidth, resHeight, diagonalFOV.asRotation2d)
        setCalibError(0.0001, 0.001) // Values from docs. Should change
        fps = processor.avgFPS
        avgLatencyMs = processor.avgLatencyMs
        latencyStdDevMs = processor.latencyStdDevMs
    }

    init {
        println("eoasdfsjalrkdsgjahdsla dlafjkhdalkdgjh ${diagonalFOV}")
    }

    companion object {
        val GenericCamera = CameraIntrinsics(CameraType.PHOTONVISION, Processor.OrangePi5B, 1280.0, 720.0, 75.0.degrees)
    }
}

fun CameraIntrinsics(type: CameraType, processor: Processor, lens: Lens, resWidth: Double, resHeight: Double): CameraIntrinsics {
    return CameraIntrinsics(type, processor, resWidth, resHeight, lens.horizontalFOV)
}

data class Lens(val horizontalFOV: Angle) {
    companion object {
        val OV9281 = Lens(70.2.degrees)
    }
}

// TODO: Upgrade so performance scales with resolution
data class Processor(val avgFPS: Double, val avgLatencyMs: Double, val latencyStdDevMs: Double) {
    // Verify these at some point
    companion object {
        val OrangePi5B = Processor(40.0, 20.0, 3.0)
    }
}

enum class CameraType {
    LIMELIGHT,
    PHOTONVISION
}