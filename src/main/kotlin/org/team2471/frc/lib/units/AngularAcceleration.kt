@file:Suppress("NOTHING_TO_INLINE")

package org.team2471.frc.lib.units

inline class AngularAcceleration(val velocityPerSecond: AngularVelocity) {
    operator fun plus(other: AngularAcceleration) = AngularAcceleration(velocityPerSecond + other.velocityPerSecond)

    operator fun minus(other: AngularAcceleration) = AngularAcceleration(velocityPerSecond - other.velocityPerSecond)

    operator fun times(factor: Double) = AngularAcceleration(velocityPerSecond * factor)

    operator fun div(factor: Double) = AngularAcceleration(velocityPerSecond / factor)

    operator fun rem(other: AngularAcceleration) = AngularAcceleration(velocityPerSecond % other.velocityPerSecond)

    operator fun unaryPlus() = this

    operator fun unaryMinus() = AngularAcceleration(-velocityPerSecond)

    operator fun compareTo(other: AngularAcceleration) = velocityPerSecond.compareTo(other.velocityPerSecond)

    override fun toString() = "$velocityPerSecond per second"
}

// constructors
inline val AngularVelocity.perSecond get() = AngularAcceleration(this)
inline val AngularVelocity.perDs get() = AngularAcceleration(this * 10.0)

inline operator fun AngularVelocity.div(time: Time) = AngularAcceleration(this / time.asSeconds)

// destructors
inline val AngularAcceleration.velocityPerDs get() = velocityPerSecond / 10.0

inline operator fun AngularAcceleration.times(time: Time) = velocityPerSecond / time.asSeconds
