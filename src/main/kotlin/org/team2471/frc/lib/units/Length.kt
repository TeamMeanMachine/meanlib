package org.team2471.frc.lib.units

inline class Length(val asInches: Double) {
    operator fun plus(other: Length) = Length(asInches + other.asInches)

    operator fun minus(other: Length) = Length(asInches - other.asInches)

    operator fun times(factor: Double) = Length(asInches * factor)

    operator fun div(factor: Double) = Length(asInches / factor)

    operator fun rem(other: Length) = Length(asInches % other.asInches)

    operator fun unaryMinus() = Length(-asInches)

    operator fun unaryPlus() = this

    operator fun compareTo(other: Length) = asInches.compareTo(other.asInches)

    override fun toString() = "$asInches inches"
}

// constructors
inline val Number.inches get() = Length(this.toDouble())
inline val Number.feet get() = Length(this.toDouble() * 12.0)
inline val Number.meters get() = Length(this.toDouble() * 39.37008)
inline val Number.cm get() = Length(this.toDouble() * 0.3937008)

// destructors
inline val Length.asFeet get() = asInches / 12.0
inline val Length.asMeters get() = asInches / 39.37008
inline val Length.asCm get() = asInches / 0.3937008
