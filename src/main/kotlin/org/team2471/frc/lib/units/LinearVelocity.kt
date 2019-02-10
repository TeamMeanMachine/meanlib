@file:Suppress("NOTHING_TO_INLINE")

package org.team2471.frc.lib.units

inline class LinearVelocity(val lengthPerSecond: Length) {
    operator fun plus(other: LinearVelocity) = LinearVelocity(lengthPerSecond + other.lengthPerSecond)

    operator fun minus(other: LinearVelocity) = LinearVelocity(lengthPerSecond - other.lengthPerSecond)

    operator fun times(factor: Double) = LinearVelocity(lengthPerSecond * factor)

    operator fun div(factor: Double) = LinearVelocity(lengthPerSecond / factor)

    operator fun unaryPlus() = this

    operator fun unaryMinus() = LinearVelocity(-lengthPerSecond)

    operator fun compareTo(other: LinearVelocity) = lengthPerSecond.compareTo(other.lengthPerSecond)

    override fun toString() = "$lengthPerSecond per second"
}

// constructors
inline val Length.perSecond get() = LinearVelocity(this)
inline val Length.perDs get() = LinearVelocity(this * 10.0)

inline operator fun Length.div(time: Time) = LinearVelocity(this / time.asSeconds)

// destructors
inline val LinearVelocity.lengthPerDs get() = lengthPerSecond / 10.0

inline operator fun LinearVelocity.times(time: Time) = lengthPerSecond / time.asSeconds
