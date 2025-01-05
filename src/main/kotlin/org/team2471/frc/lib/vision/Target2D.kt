package org.team2471.frc.lib.vision

import edu.wpi.first.math.geometry.Rotation3d
import edu.wpi.first.util.struct.StructSerializable
import org.photonvision.targeting.PhotonTrackedTarget
import org.team2471.frc.lib.math.Vector2
import org.team2471.frc.lib.units.Angle
import org.team2471.frc.lib.units.degrees
import org.team2471.frc.lib.util.calculateAverage

data class Target2D(val fiducialID: Int, val yaw: Angle, val pitch: Angle, val pixelCoords: Vector2, val robotToCamera: Rotation3d? = null): StructSerializable {
    companion object {
        val EmptyTarget2D = Target2D(100, 0.0.degrees, 0.0.degrees, Vector2(0.0, 0.0))
    }
}

fun PhotonTrackedTarget.toTarget2D(): Target2D {
    return Target2D(
        this.fiducialId,
        this.yaw.degrees,
        this.pitch.degrees,
        Vector2(
            this.detectedCorners.map { it.x }.calculateAverage(),
            this.detectedCorners.map { it.y }.calculateAverage()
        )
    )
}