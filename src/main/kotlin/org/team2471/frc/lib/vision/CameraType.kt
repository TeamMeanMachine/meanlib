package org.team2471.frc.lib.vision

import edu.wpi.first.math.geometry.Rotation2d
import org.photonvision.simulation.SimCameraProperties
import org.team2471.frc.lib.units.Angle
import org.team2471.frc.lib.units.asRotation2d
import org.team2471.frc.lib.units.degrees
import kotlin.math.atan2

data class CameraIntrinsics(val type: CameraType, val processor: Processor, val resWidth: Double, val resHeight: Double, val horizontalFOV: Angle) {
    val diagonalFOV = atan2(horizontalFOV.asDegrees * (resHeight / resWidth), horizontalFOV.asDegrees).degrees

    val simCameraProperties: SimCameraProperties = SimCameraProperties().apply {
        setCalibration(resWidth, resHeight, diagonalFOV.asRotation2d)
        setCalibError(0.2, 0.8) // Values from docs. Should change
        fps = processor.avgFPS
        avgLatencyMs = processor.avgLatencyMs
        latencyStdDevMs = processor.latencyStdDevMs
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