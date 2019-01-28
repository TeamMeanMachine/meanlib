package org.team2471.frc.lib.vector

@Deprecated("Use math.Vector2. Will be removed after 2019", level = DeprecationLevel.WARNING)
data class Vector2(var x: Double, var y: Double) {

    operator fun set(x: Double, y: Double) {
        this.x = x
        this.y = y
    }

    fun rotateRadians(radians: Double) {
        val c = Math.cos(radians)
        val s = Math.sin(radians)
        set(x * c - y * s, x * s + y * c)
    }

    fun rotateDegrees(degrees: Double) = rotateRadians(Math.toRadians(degrees))

    override fun toString(): String {
        return "<$x, $y>"
    }

    companion object {
        fun add(firstVector: Vector2, secondVector: Vector2): Vector2 {
            return Vector2(firstVector.x + secondVector.x, firstVector.y + secondVector.y)
        }

        fun subtract(firstVector: Vector2, secondVector: Vector2): Vector2 {
            return Vector2(firstVector.x - secondVector.x, firstVector.y - secondVector.y)
        }

        fun multiply(firstVector: Vector2, factor: Double): Vector2 {
            return Vector2(firstVector.x * factor, firstVector.y * factor)
        }

        fun divide(firstVector: Vector2, quotient: Double): Vector2 {
            return Vector2(firstVector.x / quotient, firstVector.y / quotient)
        }

        fun perpendicular(vector: Vector2): Vector2 {
            return Vector2(vector.y, -vector.x)
        }

        fun length(vector: Vector2): Double {
            return Math.sqrt(dot(vector, vector))
        }

        fun angle(vector: Vector2): Double {
            return Math.atan2(vector.x, vector.y)
        }

        fun normalize(vector: Vector2): Vector2 {
            return divide(vector, length(vector))
        }

        fun dot(vecA: Vector2, vecB: Vector2): Double {
            return vecA.x * vecB.x + vecA.y * vecB.y
        }
    }

    operator fun times(number: Double): Vector2 = Vector2(x * number, y * number)

    operator fun div(number: Double): Vector2 = Vector2(x / number, y / number)

    operator fun plus(vecB: Vector2): Vector2 = Vector2(x + vecB.x, y + vecB.y)

    operator fun minus(vecB: Vector2): Vector2 = Vector2(x - vecB.x, y - vecB.y)

    operator fun unaryMinus(): Vector2 = Vector2(-x, -y)

}
