@file:Suppress("NOTHING_TO_INLINE")

package org.team2471.frc.lib.units

inline class LinearAcceleration(val velocityPerSecond: LinearVelocity) {
    operator fun plus(other: LinearAcceleration) = LinearAcceleration(velocityPerSecond + other.velocityPerSecond)

    operator fun minus(other: LinearAcceleration) = LinearAcceleration(velocityPerSecond - other.velocityPerSecond)

    operator fun times(factor: Double) = LinearAcceleration(velocityPerSecond * factor)

    operator fun div(factor: Double) = LinearAcceleration(velocityPerSecond / factor)

    operator fun unaryPlus() = this

    operator fun unaryMinus() = LinearAcceleration(-velocityPerSecond)

    operator fun compareTo(other: LinearAcceleration) = velocityPerSecond.compareTo(other.velocityPerSecond)

    override fun toString() = "$velocityPerSecond per second"
}

// constructors
inline val LinearVelocity.perSecond get() = LinearAcceleration(this)
inline val LinearVelocity.perDs get() = LinearAcceleration(this * 10.0)

inline operator fun LinearVelocity.div(time: Time) = LinearAcceleration(this / time.asSeconds)

// destructors
inline val LinearAcceleration.lengthPerDs get() = velocityPerSecond / 10.0

inline operator fun LinearAcceleration.times(time: Time) = velocityPerSecond / time.asSeconds
