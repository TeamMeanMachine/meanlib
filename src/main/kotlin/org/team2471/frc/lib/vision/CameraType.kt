package org.team2471.frc.lib.vision

enum class CameraType {
    LIMELIGHT,
    PHOTONVISION;

    fun toDouble(): Double {
        return when (this) {
            LIMELIGHT -> 0.0
            PHOTONVISION -> 1.0
        }
    }

    companion object {
        fun fromDouble(d: Double): CameraType {
            return when (d) {
                0.0 -> LIMELIGHT
                1.0 -> PHOTONVISION
                else -> LIMELIGHT
            }
        }
    }
}