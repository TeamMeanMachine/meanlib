package org.team2471.frc.lib.localization

import edu.wpi.first.math.interpolation.Interpolatable
import edu.wpi.first.math.kinematics.ChassisSpeeds

class InterpolatableChassisSpeeds(
    vxMetersPerSecond: Double,
    vyMetersPerSecond: Double,
    omegaRadiansPerSecond: Double
): ChassisSpeeds(vxMetersPerSecond, vyMetersPerSecond, omegaRadiansPerSecond),
    Interpolatable<InterpolatableChassisSpeeds> {

    override fun interpolate(endValue: InterpolatableChassisSpeeds, t: Double): InterpolatableChassisSpeeds {
        if (t < 0) {
            return this
        }
        if (t >= 1) {
            return endValue
        }
        return InterpolatableChassisSpeeds(
            (1.0 - t) * vxMetersPerSecond + t * endValue.vxMetersPerSecond,
            (1.0 - t) * vyMetersPerSecond + t * endValue.vyMetersPerSecond,
            (1.0 - t) * omegaRadiansPerSecond + t * endValue.omegaRadiansPerSecond
        )
    }

    companion object {
        fun fromChassisSpeeds(cs: ChassisSpeeds): InterpolatableChassisSpeeds {
            return InterpolatableChassisSpeeds(
                cs.vxMetersPerSecond, cs.vyMetersPerSecond, cs.omegaRadiansPerSecond
            )
        }
    }
}