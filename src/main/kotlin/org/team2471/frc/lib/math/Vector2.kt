package org.team2471.frc.lib.math

import com.team254.lib.util.Interpolable
import edu.wpi.first.math.geometry.Pose2d
import edu.wpi.first.math.geometry.Rotation2d
import edu.wpi.first.math.geometry.Translation2d
import org.team2471.frc.lib.units.*
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.max
import kotlin.math.min

data class Vector2(var x: Double, var y: Double) : Interpolable<Vector2> {
    val length: Double get() = Math.sqrt(dot(this))
    val angle: Angle get() = Math.atan2(y, x).radians
    val angleAsRadians: Double get() = Math.atan2(y, x)
    val angleAsDegrees: Double get() = Math.toDegrees(Math.atan2(y, x))

    override fun toString(): String {
        return "(${round(x, 7)}, ${round(y, 7)})"
    }

    fun rotate(angle: Angle): Vector2 {
        val c = angle.cos()
        val s = angle.sin()
        return Vector2(x * c - y * s, x * s + y * c)
    }

    fun rotateRadians(radians: Double): Vector2 = rotate(radians.radians)
    fun rotateDegrees(degrees: Double): Vector2 = rotate(degrees.degrees)

    fun round(decimalPlaces: Int = 0): Vector2 {
        return Vector2(BigDecimal(this.x).setScale(decimalPlaces, RoundingMode.HALF_EVEN).toDouble(), BigDecimal(this.y).setScale(decimalPlaces, RoundingMode.HALF_EVEN).toDouble())
    }

    operator fun unaryPlus() = this * 1.0

    operator fun unaryMinus() = this * -1.0

    operator fun plus(b: Vector2) = Vector2(x + b.x, y + b.y)

    operator fun minus(b: Vector2) = Vector2(x - b.x, y - b.y)

    operator fun times(scalar: Double) = Vector2(x * scalar, y * scalar)

    operator fun div(scalar: Double) = Vector2(x / scalar, y / scalar)

    fun dot(b: Vector2) = (x * b.x) + (y * b.y)

    fun normalize() = this / length

    fun flipXAndY() = Vector2(y, x)

    fun perpendicular() = Vector2(y, -x)

    fun mirrorXAxis() = Vector2(-x, y)

    fun mirrorYAxis() = Vector2(x, -y)

    fun reflectAcrossField(middle: Double = 28.78645833333333) = Vector2(middle * 2 - x, y)

    fun distance(other: Vector2) = Math.hypot(x - other.x, y - other.y)

    fun set(other: Vector2) {
        x = other.x
        y = other.y
    }

    fun set(X: Double, Y: Double) {
        x = X
        y = Y
    }

    fun coerceIn(otherMin: Vector2, otherMax: Vector2) {
        set(
            this.x.coerceIn(otherMin.x, otherMax.x),
            this.y.coerceIn(otherMin.y, otherMax.y)
        )
    }

    fun coerceInDynamic(oneLimit: Vector2, twoLimit: Vector2) {
        set(
            this.x.coerceIn(min(oneLimit.x, twoLimit.x), max(oneLimit.x, twoLimit.y)),
            this.y.coerceIn(min(oneLimit.y, twoLimit.y), max(oneLimit.y, twoLimit.y))
        )
    }

    fun getClosestPoint(vararg points: Vector2): Vector2 {
        var closestPoint = points.first()
        var delta = this - closestPoint
        var minDistance = delta.x * delta.x + delta.y * delta.y
        for (point in points) {
            delta = this - point
            val distance = delta.x * delta.x + delta.y * delta.y
            if (distance < minDistance) {
                closestPoint = point
                minDistance = distance
            }
        }

        return closestPoint
    }

    override fun interpolate(other: Vector2, x: Double): Vector2 {
        return when {
            x <= 0.0 -> this
            x >= 1.0 -> other
            else -> Vector2(x * (other.x - this.x) + this.x, x * (other.y - this.y) + this.y)
        }
    }
}

fun Translation2d.asVector2() = Vector2(this.x, this.y)

fun Vector2.toTranslation2d(): Translation2d = Translation2d(this.x, this.y)

fun Vector2.toPose2d(heading: Double): Pose2d = Pose2d(this.toTranslation2d(), Rotation2d.fromRadians(heading))

fun Vector2.toPose2d(heading: Angle): Pose2d = Pose2d(this.toTranslation2d(), Rotation2d.fromDegrees(heading.asDegrees))
