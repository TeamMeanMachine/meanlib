package org.team2471.frc.lib.math

import edu.wpi.first.math.geometry.Pose2d
import edu.wpi.first.math.geometry.Rotation2d
import edu.wpi.first.math.geometry.Transform2d
import edu.wpi.first.math.geometry.Translation2d
import edu.wpi.first.math.geometry.Twist2d
import kotlin.math.*

@JvmName("squareOf")
fun square(x: Double): Double = x.square()

fun Double.square(): Double = this * this

fun Double.squareWithSign() = (this.square()).withSign(this)

fun Double.cube() = this * this * this

fun Double.squareRootWithSign() = sqrt(abs(this)).withSign(this)

fun Double.log10() = log10(this)

fun average(vararg x: Double) = x.sum() / x.size

fun lerp(min: Double, max: Double, k: Double) = min + (max - min) * k

fun Double.deadband(tolerance: Double): Double =
    if (abs(this) < tolerance) {
        0.0
    } else {
        (this - tolerance.withSign(this)) / (1.0 - tolerance)
    }

/** Divide [Translation2d] by its magnitude, making it have a magnitude of 1 while preserving its angle. */
fun Translation2d.normalize(): Translation2d {
    val mag = norm
    return if (mag != 0.0) this.div(mag) else this
}

fun Translation2d.toPose2d(heading: Rotation2d = Rotation2d()): Pose2d {
    return Pose2d(this, heading)
}

fun Translation2d.mirrorXAxis() = Translation2d(-x, y)
fun Translation2d.mirrorYAxis() = Translation2d(x, -y)

fun Transform2d.mirrorXAxis() = Transform2d(-x, y, rotation)
fun Transform2d.mirrorYAxis() = Transform2d(x, -y, rotation)

fun Translation2d.coerceInDynamic(oneLimit: Translation2d, twoLimit: Translation2d): Translation2d =
    Translation2d(this.x.coerceInDynamic(oneLimit.x, twoLimit.x), this.y.coerceInDynamic(oneLimit.y, twoLimit.y))

/** doesn't work with negative values of n */
infix fun Double.mod(n: Double) = if (this < 0) {
    (this % n + n) % n
} else {
    this % n
}

fun Double.round(digits: Int): Double {
    return round(this, digits)
}
@JvmName("roundToDigit")
fun round(number: Double, digits: Int): Double {
    if (!number.isNaN()) {
        val modulo = 10.0.pow(digits.toDouble())
        return (number * modulo).roundToInt() / modulo
    }
    else {
        return number
    }
}

fun linearMap(inLo: Double, inHi: Double, outLo: Double, outHi: Double, inAlpha: Double): Double {
    return (inAlpha - inLo) / (inHi - inLo) * (outHi - outLo) + outLo
}

fun cubicMap(inLo: Double, inHi: Double, outLo: Double, outHi: Double, inAlpha: Double): Double {
    val x = (inAlpha - inLo) / (inHi - inLo)
    val cubic = (3 - 2 * x) * x * x
    return cubic * (outHi - outLo) + outLo
}

fun windRelativeAngles(angle1: Double, angle2: Double): Double {
    val diff = angle1 - angle2
    val absDiff = abs(diff)
    return if (absDiff > 180.0) {
        angle2 + 360.0 * sign(diff) * floor((absDiff / 360.0) + 0.5)
    } else {
        angle2
    }
}

/**
 * Finds the closest point along a line defined by [linePointOne] and [linePointTwo] to the provided [referencePoint]
 */
fun findClosestPointOnLine(linePointOne: Translation2d, linePointTwo: Translation2d, referencePoint: Translation2d): Translation2d {
    val lineAngle = (linePointTwo - linePointOne).angle
    val rotatedReferencePoint = referencePoint.rotateBy(-lineAngle)
    val rotatedPointOne = linePointOne.rotateBy(-lineAngle)
    val rotatedPointTwo = linePointTwo.rotateBy(-lineAngle)
    val closestXPoint = rotatedReferencePoint.x.coerceInDynamic(rotatedPointOne.x, rotatedPointTwo.x)
    val coercedTranslation = Translation2d(closestXPoint, rotatedPointOne.y)
    val closestPointOnLine = coercedTranslation.rotateBy(lineAngle)

    return closestPointOnLine
}

fun Double.coerceInDynamic(oneLimit: Double, twoLimit: Double) = this.coerceIn(min(oneLimit, twoLimit), max(oneLimit, twoLimit))

fun interpTo(from: Double, to: Double, speed: Double, dt: Double = 0.02): Double {
    return from + (to - from) * dt * speed
}

fun epsilonEquals(a: Double, b: Double, epsilon: Double): Boolean {
    return (a - epsilon <= b) && (a + epsilon >= b)
}

fun epsilonEquals(a: Double, b: Double): Boolean {
    return epsilonEquals(a, b, 1e-5)
}

fun epsilonEquals(twist: Twist2d, other: Twist2d): Boolean {
    return epsilonEquals(twist.dx, other.dx) &&
            epsilonEquals(twist.dy, other.dy) &&
            epsilonEquals(twist.dtheta, other.dtheta)
}

@JvmName("epsilonEqualsOf")
fun Twist2d.epsilonEquals(other: Twist2d) = epsilonEquals(this, other)

fun Double.epsonEquals(other: Double) = epsilonEquals(this, other)
