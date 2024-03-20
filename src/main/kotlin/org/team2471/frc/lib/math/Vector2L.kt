package org.team2471.frc.lib.math

import com.team254.lib.util.Interpolable
import edu.wpi.first.networktables.NetworkTableEntry
import edu.wpi.first.networktables.NetworkTableInstance
import org.team2471.frc.lib.units.*
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.*

data class Vector2L(var x: Length, var y: Length) : Interpolable<Vector2L> {
    val length: Length get() = sqrt(dot(this).asMeters).meters

    val angle: Angle get() = atan2(x.asMeters, y.asMeters).radians
    val angleAsRadians: Double get() = atan2(x.asMeters, y.asMeters)
    val angleAsDegrees: Double get() = atan2(x.asMeters, y.asMeters).radians.asDegrees

    override fun toString(): String {
        return "(${round(x.asFeet, 7)} feet, ${round(y.asFeet, 7)} feet)"
    }

    fun rotateRadians(radians: Double): Vector2L {
        val c = cos(radians)
        val s = sin(radians)
        return Vector2L(x * c - y * s, x * s + y * c)
    }

    fun rotateDegrees(degrees: Double): Vector2L = rotateRadians(Math.toRadians(degrees))

    operator fun unaryPlus() = this * 1.0

    operator fun unaryMinus() = this * -1.0

    operator fun plus(b: Vector2L) = Vector2L(x + b.x, y + b.y)

    operator fun minus(b: Vector2L) = Vector2L(x - b.x, y - b.y)

    operator fun times(scalar: Double) = Vector2L(x * scalar, y * scalar)

    operator fun div(scalar: Double) = Vector2L(x / scalar, y / scalar)

    fun dot(b: Vector2L) = ((x.asMeters * b.x.asMeters) + (y.asMeters * b.y.asMeters)).meters

    fun normalize() = (this.asMeters / length.asMeters).meters

    fun perpendicular() = Vector2L(y, -x)

    fun mirrorXAxis() = Vector2L(-x, y)

    fun mirrorYAxis() = Vector2L(x, -y)

    fun reflectAcrossField(middle: Length = 26.135.feet) = Vector2L(middle * 2.0 - x, y)

    fun distance(other: Vector2L) = hypot((x - other.x).asMeters, (y - other.y).asMeters).meters

    fun set(other: Vector2L) {
        x = other.x
        y = other.y
    }

    fun set(X: Length, Y: Length) {
        x = X
        y = Y
    }

    fun coerceIn(otherMin: Vector2L, otherMax: Vector2L) {
        set(
            this.x.asInches.coerceIn(otherMin.x.asInches, otherMax.x.asInches).inches,
            this.y.asInches.coerceIn(otherMin.y.asInches, otherMax.y.asInches).inches
        )
    }

    override fun interpolate(other: Vector2L, x: Double): Vector2L {
        return when {
            x <= 0.0 -> this
            x >= 1.0 -> other
            else -> Vector2L((x * (other.x.asFeet - this.x.asFeet) + this.x.asFeet).feet, (x * (other.y.asFeet - this.y.asFeet) + this.y.asFeet).feet)
        }
    }
}

// constructors
inline val Vector2.inches get() = Vector2L(this.x.inches, this.y.inches)
inline val Vector2.feet get() = Vector2L(this.x.feet, this.y.feet)
inline val Vector2.meters get() = Vector2L(this.x.meters, this.y.meters)
inline val Vector2.cm get() = Vector2L(this.x.cm, this.y.cm)

// destructors
inline val Vector2L.asInches get() = Vector2(this.x.asInches, this.y.asInches)
inline val Vector2L.asFeet get() = Vector2(this.x.asFeet, this.y.asFeet)
inline val Vector2L.asMeters get() = Vector2(this.x.asMeters, this.y.asMeters)
inline val Vector2L.asCm get() = Vector2(this.x.asCm, this.y.asCm)

fun NetworkTableEntry.setAdvantagePose(pos: Vector2L, rot: Angle = 0.0.degrees) {
    this.setDoubleArray(
        doubleArrayOf(
            pos.x.asMeters,
            pos.y.asMeters,
            rot.asDegrees
        )
    )
}

// untested
fun NetworkTableEntry.setAdvantagePoses(pos: Array<Vector2L>) {
    val rot = mutableListOf<Angle>()
    for (i in pos.indices) {
        rot.add(0.0.degrees)
    }
    this.setAdvantagePoses(pos, rot.toTypedArray())
}
fun NetworkTableEntry.setAdvantagePoses(pos: ArrayList<Vector2L>) {
    setAdvantagePoses(pos.toTypedArray())
}
// untested
fun NetworkTableEntry.setAdvantagePoses(pos: Array<Vector2L>, rot: Array<Angle>) {
    require(pos.size == rot.size)

    val array = arrayOfNulls<Double>(pos.size * 2 + rot.size)


    // Index in final array
    var i = 0

    // Index in rot/pos array
    for (j in rot.indices) {
        array[i] = pos[j].x.asMeters
        array[i + 1] = pos[j].y.asMeters
        array[i + 2] = rot[j].asDegrees

        i += 3
    }

    this.setDoubleArray(array)
}
