@file:Suppress("UNUSED")

package org.team2471.frc.lib.units

import edu.wpi.first.math.geometry.Rotation2d
import edu.wpi.first.math.geometry.Translation2d
import edu.wpi.first.math.geometry.struct.Translation2dStruct
import edu.wpi.first.units.DistanceUnit
import edu.wpi.first.units.LinearAccelerationUnit
import edu.wpi.first.units.LinearVelocityUnit
import edu.wpi.first.units.Measure
import edu.wpi.first.units.Unit
import edu.wpi.first.units.VelocityUnit
import edu.wpi.first.util.struct.StructSerializable

/**
 * Unit Translation2D
 *
 * Preserves the type of unit that it got constructed with. Inherits from WPILib Translation2D so can be used as a drop in replacement.
 * */
@Suppress("UNCHECKED_CAST")
class UTranslation2d<U : Unit>(x: Measure<U>, y: Measure<U>): Translation2d(x.baseUnitMagnitude(), y.baseUnitMagnitude()), StructSerializable {
    private val unit = x.unit().baseUnit

    val x: Measure<U>
        get() = unit.of(super.getX()) as Measure<U>
    val y: Measure<U>
        get() = unit.of(super.getY()) as Measure<U>

    val norm: Measure<U>
        get() = unit.of(super.getNorm()) as Measure<U>

    private fun fromTranslation2d(translation2d: Translation2d) = UTranslation2d(unit.of(translation2d.x) as Measure<U>, unit.of(translation2d.y) as Measure<U>)
    fun translation2d() = Translation2d(super.getX(), super.getY())

    operator fun plus(other: UTranslation2d<U>) = UTranslation2d(x + other.x, y + other.y)
    operator fun minus(other: UTranslation2d<U>) = UTranslation2d(x - other.x, y - other.y)

    override operator fun div(by: Double) = UTranslation2d(x / by, y / by)
//    operator fun div(by: Measure<U>) = Translation2d((x / by).magnitude(), (y / by).magnitude())

    override operator fun times(by: Double) = UTranslation2d(x * by, y * by)
//    operator fun times(by: Measure<U>) = Translation2d((x * by).magnitude(), (y * by).magnitude())

    override fun rotateBy(other: Rotation2d): UTranslation2d<U> = fromTranslation2d(super.rotateBy(other))

    fun getDistance(other: UTranslation2d<U>) = (this - other).norm
    fun interpolate(endValue: UTranslation2d<U>, t: Double): UTranslation2d<U> = fromTranslation2d(super.interpolate(endValue, t))
    fun nearest(translations: List<UTranslation2d<U>>): UTranslation2d<U> = fromTranslation2d(super.nearest(translations))
    fun rotateAround(other: UTranslation2d<U>, rot: Rotation2d): UTranslation2d<U> = fromTranslation2d(super.rotateAround(other, rot))

    override fun unaryMinus(): UTranslation2d<U> = fromTranslation2d(super.unaryMinus())

    companion object {
        @JvmField
        val struct: Translation2dStruct = Translation2d.struct // Use the Translation2D struct
    }
}

// Distance UTranslation2d constructors
inline val Translation2d.feet: UTranslation2d<DistanceUnit> get() = UTranslation2d(x.feet, y.feet)
inline val Translation2d.inches: UTranslation2d<DistanceUnit> get() = UTranslation2d(x.inches, y.inches)
inline val Translation2d.meters: UTranslation2d<DistanceUnit> get() = UTranslation2d(x.meters, y.meters)
inline val Translation2d.centimeters: UTranslation2d<DistanceUnit> get() = UTranslation2d(x.centimeters, y.centimeters)
inline val Translation2d.millimeters: UTranslation2d<DistanceUnit> get() = UTranslation2d(x.centimeters, y.centimeters)

// Distance UTranslation2d destructors
inline val UTranslation2d<DistanceUnit>.asFeet: Translation2d get() = Translation2d(x.asFeet, y.asFeet)
inline val UTranslation2d<DistanceUnit>.asInches: Translation2d get() = Translation2d(x.asInches, y.asInches)
inline val UTranslation2d<DistanceUnit>.asMeters: Translation2d get() = Translation2d(x.asMeters, y.asMeters)
inline val UTranslation2d<DistanceUnit>.asCentimeters: Translation2d get() = Translation2d(x.asCentimeters, y.asCentimeters)
inline val UTranslation2d<DistanceUnit>.asMillimeters: Translation2d get() = Translation2d(x.asMillimeters, y.asMillimeters)

// Velocity UTranslation2d constructors
@get:JvmName("UTranslation2dDistancePerSecond")
inline val UTranslation2d<DistanceUnit>.perSecond: UTranslation2d<LinearVelocityUnit> get() = UTranslation2d(x.perSecond, y.perSecond)
inline val Translation2d.feetPerSecond: UTranslation2d<LinearVelocityUnit> get() = UTranslation2d(x.feetPerSecond, y.feetPerSecond)
inline val Translation2d.inchesPerSecond: UTranslation2d<LinearVelocityUnit> get() = UTranslation2d(x.inchesPerSecond, y.inchesPerSecond)
inline val Translation2d.metersPerSecond: UTranslation2d<LinearVelocityUnit> get() = UTranslation2d(x.metersPerSecond, y.metersPerSecond)

// Velocity UTranslation2d destructors
inline val UTranslation2d<LinearVelocityUnit>.asFeetPerSecond: Translation2d get() = Translation2d(this.x.asFeetPerSecond, this.y.asFeetPerSecond)
inline val UTranslation2d<LinearVelocityUnit>.asInchesPerSecond: Translation2d get() = Translation2d(this.x.asInchesPerSecond, this.y.asInchesPerSecond)
inline val UTranslation2d<LinearVelocityUnit>.asMetersPerSecond: Translation2d get() = Translation2d(this.x.asMetersPerSecond, this.y.asMetersPerSecond)

// Acceleration UTranslation2d constructors
inline val UTranslation2d<DistanceUnit>.perSecondPerSecond: UTranslation2d<LinearAccelerationUnit> get() = UTranslation2d(x.perSecondPerSecond, y.perSecondPerSecond)
@get:JvmName("UTranslation2dLinearVelocityPerSecond")
inline val UTranslation2d<LinearVelocityUnit>.perSecond: UTranslation2d<LinearAccelerationUnit> get() = UTranslation2d(this.x.perSecond, this.y.perSecond)
inline val Translation2d.feetPerSecondPerSecond: UTranslation2d<LinearAccelerationUnit> get() = UTranslation2d(x.feetPerSecondPerSecond, y.feetPerSecondPerSecond)
inline val Translation2d.inchesPerSecondPerSecond: UTranslation2d<LinearAccelerationUnit> get() = UTranslation2d(x.inchesPerSecondPerSecond, y.inchesPerSecondPerSecond)
inline val Translation2d.metersPerSecondPerSecond: UTranslation2d<LinearAccelerationUnit> get() = UTranslation2d(x.metersPerSecondPerSecond, y.metersPerSecondPerSecond)

// Acceleration UTranslation2d destructors
inline val UTranslation2d<LinearAccelerationUnit>.asFeetPerSecondPerSecond: Translation2d get() = Translation2d(this.x.asFeetPerSecondPerSecond, this.y.asFeetPerSecondPerSecond)
inline val UTranslation2d<LinearAccelerationUnit>.asInchesPerSecondPerSecond: Translation2d get() = Translation2d(this.x.asInchesPerSecondPerSecond, this.y.asInchesPerSecondPerSecond)
inline val UTranslation2d<LinearAccelerationUnit>.asMetersPerSecondPerSecond: Translation2d get() = Translation2d(this.x.asMetersPerSecondPerSecond, this.y.asMetersPerSecondPerSecond)

// Jerk UTranslation2d constructors
@get:JvmName("UTranslation2dLinearAccelerationPerSecond")
inline val UTranslation2d<LinearAccelerationUnit>.perSecond: UTranslation2d<VelocityUnit<LinearAccelerationUnit>> get() = UTranslation2d(this.x.perSecond, this.y.perSecond)

// Jerk UTranslation2d destructors
inline val UTranslation2d<VelocityUnit<LinearAccelerationUnit>>.asFeetJerk: Translation2d get() = Translation2d(this.x.asFeetJerk, this.y.asFeetJerk)
inline val UTranslation2d<VelocityUnit<LinearAccelerationUnit>>.asInchesJerk: Translation2d get() = Translation2d(this.x.asInchesJerk, this.y.asInchesJerk)
inline val UTranslation2d<VelocityUnit<LinearAccelerationUnit>>.asMetersJerk: Translation2d get() = Translation2d(this.x.asMetersJerk, this.y.asMetersJerk)