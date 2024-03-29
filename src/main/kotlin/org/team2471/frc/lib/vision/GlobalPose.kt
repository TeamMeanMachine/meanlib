package org.team2471.frc.lib.vision

import org.team2471.frc.lib.math.Vector2L
import org.team2471.frc.lib.units.Angle

data class GlobalPose (
    var pose: Vector2L,
    val rotation: Angle,
    val stDev: Double,
    val timestampSeconds: Double
)