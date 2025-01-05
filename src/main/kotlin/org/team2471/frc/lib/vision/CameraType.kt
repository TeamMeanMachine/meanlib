package org.team2471.frc.lib.vision

import java.util.BitSet

enum class CameraType {
    LIMELIGHT,
    PHOTONVISION;

    fun toByte(): Byte {
        return when (this) {
            LIMELIGHT -> 0
            PHOTONVISION -> 1
        }
    }

    companion object {
        fun fromByte(b: Byte): CameraType {
            return when (b) {
                0.toByte() -> LIMELIGHT
                1.toByte() -> PHOTONVISION
                else -> LIMELIGHT
            }
        }
    }
}