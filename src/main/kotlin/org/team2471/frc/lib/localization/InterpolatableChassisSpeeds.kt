package org.team2471.frc.lib.localization

import edu.wpi.first.math.interpolation.Interpolatable
import edu.wpi.first.math.kinematics.ChassisSpeeds

class InterpolatableChassisSpeeds(
    vx: Double,
    vy: Double,
    omega: Double
): ChassisSpeeds(vx, vy, omega),
    Interpolatable<InterpolatableChassisSpeeds> {

    override fun interpolate(endValue: InterpolatableChassisSpeeds, t: Double): InterpolatableChassisSpeeds {
        if (t < 0) {
            return this
        }
        if (t >= 1) {
            return endValue
        }
        return InterpolatableChassisSpeeds(
            (1.0 - t) * vx + t * endValue.vx,
            (1.0 - t) * vy + t * endValue.vy,
            (1.0 - t) * omega + t * endValue.omega
        )
    }

    companion object {
        fun fromChassisSpeeds(cs: ChassisSpeeds): InterpolatableChassisSpeeds {
            return InterpolatableChassisSpeeds(
                cs.vx, cs.vy, cs.omega
            )
        }
    }
}