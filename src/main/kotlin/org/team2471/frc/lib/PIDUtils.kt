package org.team2471.frc.lib

import edu.wpi.first.wpilibj.PIDSource
import edu.wpi.first.wpilibj.PIDSourceType

inline fun displacementPIDSource(crossinline source: () -> Double) = object : PIDSource {
    override fun getPIDSourceType(): PIDSourceType = PIDSourceType.kDisplacement

    override fun setPIDSourceType(pidSource: PIDSourceType?) = Unit // will never be used

    override fun pidGet(): Double = source()
}

inline fun ratePIDSource(crossinline source: () -> Double) = object : PIDSource {
    override fun getPIDSourceType(): PIDSourceType = PIDSourceType.kRate

    override fun setPIDSourceType(pidSource: PIDSourceType?) = Unit // will never be used

    override fun pidGet(): Double = source()
}
