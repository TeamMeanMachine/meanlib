package org.team2471.frc.lib.units

inline class Time(val asSeconds: Double) {
    operator fun plus(other: Time) = Time(asSeconds + other.asSeconds)

    operator fun minus(other: Time) = Time(asSeconds - other.asSeconds)

    operator fun times(factor: Double) = Time(asSeconds * factor)

    operator fun div(factor: Double) = Time(asSeconds / factor)

    operator fun unaryPlus() = this

    operator fun unaryMinus() = Time(-asSeconds)

    operator fun compareTo(other: Time) = asSeconds.compareTo(other.asSeconds)

    override fun toString(): String = "$asSeconds seconds"
}

// constructors
inline val Number.seconds get() = Time(this.toDouble())