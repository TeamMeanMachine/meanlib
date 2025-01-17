package org.team2471.frc.lib.units

import edu.wpi.first.math.geometry.Rotation2d
import edu.wpi.first.units.Units
import edu.wpi.first.util.struct.StructSerializable
import kotlin.math.IEEErem
import kotlin.math.absoluteValue

@JvmInline
value class Angle(val asDegrees: Double): StructSerializable {
    operator fun plus(other: Angle) = Angle(asDegrees + other.asDegrees)

    operator fun minus(other: Angle) = Angle(asDegrees - other.asDegrees)

    operator fun times(factor: Double) = Angle(asDegrees * factor)

    operator fun div(factor: Double) = Angle(asDegrees / factor)

    operator fun rem(other: Angle) = Angle(asDegrees % other.asDegrees)

    operator fun unaryPlus() = this

    operator fun unaryMinus() = Angle(-asDegrees)

    operator fun compareTo(other: Angle) = asDegrees.compareTo(other.asDegrees)

    override fun toString() = "$asDegrees degrees"

    fun sin() = sin(this)

    fun cos() = cos(this)

    fun tan() = tan(this)

    fun wrap() = Angle(asDegrees.IEEErem(360.0))

    fun unWrap(nearByAngle: Angle) : Angle {
        return nearByAngle + (this - nearByAngle).wrap()
    }

    fun absoluteValue(): Angle {
        return Angle(asDegrees.absoluteValue)
    }

    companion object {

        val struct: AngleStruct = AngleStruct()

        @JvmStatic
        fun sin(angle: Angle) = Math.sin(angle.asRadians)

        @JvmStatic
        fun cos(angle: Angle) = Math.cos(angle.asRadians)

        @JvmStatic
        fun tan(angle: Angle) = Math.tan(angle.asRadians)

        @JvmStatic
        fun asin(value: Double) = Angle(Math.toDegrees(Math.asin(value)))

        @JvmStatic
        fun acos(value: Double) = Angle(Math.toDegrees(Math.acos(value)))

        @JvmStatic
        fun atan(value: Double) = Angle(Math.toDegrees(Math.atan(value)))

        @JvmStatic
        fun atan2(y: Double, x: Double) = Angle(Math.toDegrees(Math.atan2(y, x)))

        @JvmStatic
        fun atan2(y: Length, x: Length) = Angle(Math.toDegrees(Math.atan2(y.asInches, x.asInches)))
    }
}

// constructors
inline val Number.radians get() = Angle(Math.toDegrees(this.toDouble()))
inline val Number.degrees get() = Angle(this.toDouble())
inline val Number.rotations get() = Angle(this.toDouble() * 360.0)
// destructors
inline val Angle.asRadians get() = Math.toRadians(asDegrees)
inline val Angle.asRotations get() = asDegrees / 360.0

inline val Angle.asRotation2d: Rotation2d get() = Rotation2d.fromDegrees(asDegrees)
inline val Rotation2d.asAngle get() = Angle(degrees)
inline val Angle.asWPIUnit get() = Units.Degrees.of(asDegrees)
