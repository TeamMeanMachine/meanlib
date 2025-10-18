@file:Suppress("UNUSED")

package org.team2471.frc.lib.units

import edu.wpi.first.math.geometry.Rotation2d
import edu.wpi.first.units.*
import edu.wpi.first.units.Units.*
import edu.wpi.first.units.measure.*
import kotlin.math.*

//Unit Conversions

//Distance
inline val Measure<DistanceUnit>.asInches: Double get() = `in`(Inches)
inline val Measure<DistanceUnit>.asFeet: Double get() = `in`(Feet)
inline val Measure<DistanceUnit>.asMeters: Double get() = `in`(Meters)
inline val Measure<DistanceUnit>.asCentimeters: Double get() = `in`(Centimeters)
inline val Measure<DistanceUnit>.asMillimeters: Double get() = `in`(Millimeter)

inline val Double.inches: Distance get() = Inches.of(this)
inline val Double.feet: Distance get() = Feet.of(this)
inline val Double.meters: Distance get() = Meters.of(this)
inline val Double.centimeters: Distance get() = Centimeters.of(this)
inline val Double.millimeters: Distance get() = Millimeters.of(this)


//Angle
inline val Angle.asDegrees: Double get() = `in`(Degrees)
inline val Angle.asRotations: Double get() = `in`(Rotations)
inline val Angle.asRadians: Double get() = `in`(Radians)
inline val Angle.asRotation2d: Rotation2d get() = Rotation2d(this)

inline val Double.degrees: Angle get() = Degrees.of(this)
inline val Double.rotations: Angle get() = Rotations.of(this)
inline val Double.radians: Angle get() = Radians.of(this)


//Time
inline val Time.asSeconds: Double get() = `in`(Seconds)
inline val Time.asMinutes: Double get() = `in`(Minutes)
inline val Time.asMilliseconds: Double get() = `in`(Milliseconds)
inline val Time.asMicroseconds: Double get() = `in`(Microseconds)

inline val Double.seconds: Time get() = Seconds.of(this)
inline val Double.milliseconds: Time get() = Milliseconds.of(this)
inline val Double.microseconds: Time get() = Microseconds.of(this)
inline val Double.minutes: Time get() = Minutes.of(this)


//Linear Velocity
inline val Measure<LinearVelocityUnit>.asInchesPerSecond: Double get() = `in`(InchesPerSecond)
inline val Measure<LinearVelocityUnit>.asFeetPerSecond: Double get() = `in`(FeetPerSecond)
inline val Measure<LinearVelocityUnit>.asMetersPerSecond: Double get() = `in`(MetersPerSecond)

inline val Measure<DistanceUnit>.perSecond: LinearVelocity get() = InchesPerSecond.of(this.asInches)

inline val Double.inchesPerSecond: LinearVelocity get() = InchesPerSecond.of(this)
inline val Double.feetPerSecond: LinearVelocity get() = FeetPerSecond.of(this)
inline val Double.metersPerSecond: LinearVelocity get() = MetersPerSecond.of(this)


//Angular Velocity
inline val AngularVelocity.asDegreesPerSecond: Double get() = `in`(DegreesPerSecond)
inline val AngularVelocity.asRotationsPerSecond: Double get() = `in`(RotationsPerSecond)
inline val AngularVelocity.asRPM: Double get() = `in`(RPM)
inline val AngularVelocity.asRadiansPerSecond: Double get() = `in`(RadiansPerSecond)

inline val Angle.perSecond: AngularVelocity get() = DegreesPerSecond.of(this.asDegrees)

inline val Double.degreesPerSecond: AngularVelocity get() = DegreesPerSecond.of(this)
inline val Double.rotationsPerSecond: AngularVelocity get() = RotationsPerSecond.of(this)
inline val Double.rpm: AngularVelocity get() = RPM.of(this)
inline val Double.radiansPerSecond: AngularVelocity get() = RadiansPerSecond.of(this)


//Linear Acceleration
inline val Measure<LinearAccelerationUnit>.asInchesPerSecondPerSecond: Double get() = `in`(InchesPerSecond.per(Second))
inline val Measure<LinearAccelerationUnit>.asFeetPerSecondPerSecond: Double get() = `in`(FeetPerSecondPerSecond)
inline val Measure<LinearAccelerationUnit>.asMetersPerSecondPerSecond: Double get() = `in`(MetersPerSecondPerSecond)
inline val Measure<LinearAccelerationUnit>.asGs: Double get() = `in`(Gs)

inline val Measure<DistanceUnit>.perSecondPerSecond: LinearAcceleration get() = FeetPerSecondPerSecond.of(this.asFeet)
inline val Measure<LinearVelocityUnit>.perSecond: LinearAcceleration get() = FeetPerSecondPerSecond.of(this.asFeetPerSecond)

inline val Double.inchesPerSecondPerSecond: LinearAcceleration get() = InchesPerSecond.per(Second).of(this)
inline val Double.feetPerSecondPerSecond: LinearAcceleration get() = FeetPerSecondPerSecond.of(this)
inline val Double.metersPerSecondPerSecond: LinearAcceleration get() = MetersPerSecondPerSecond.of(this)
inline val Double.Gs: LinearAcceleration get() = Units.Gs.of(this)

//Linear Jerk
inline val Measure<LinearAccelerationUnit>.perSecond: Velocity<LinearAccelerationUnit> get() = MetersPerSecondPerSecond.per(Second).of(this.asMetersPerSecondPerSecond)

inline val Measure<VelocityUnit<LinearAccelerationUnit>>.asInchesJerk: Double get() = `in`(MetersPerSecondPerSecond.per(Second))
inline val Measure<VelocityUnit<LinearAccelerationUnit>>.asFeetJerk: Double get() = `in`(InchesPerSecond.per(Second).per(Second))
inline val Measure<VelocityUnit<LinearAccelerationUnit>>.asMetersJerk: Double get() = `in`(FeetPerSecondPerSecond.per(Second))


//Angular Acceleration
inline val AngularAcceleration.asDegreesPerSecondPerSecond: Double get() = `in`(DegreesPerSecondPerSecond)
inline val AngularAcceleration.asRotationsPerSecondPerSecond: Double get() = `in`(RotationsPerSecondPerSecond)
inline val AngularAcceleration.asRadiansPerSecondPerSecond: Double get() = `in`(RadiansPerSecondPerSecond)

inline val Angle.perSecondPerSecond: AngularAcceleration get() = DegreesPerSecondPerSecond.of(this.asDegrees)
inline val AngularVelocity.perSecond: AngularAcceleration get() = DegreesPerSecondPerSecond.of(this.asDegreesPerSecond)

inline val Double.degreesPerSecondPerSecond: AngularAcceleration get() = DegreesPerSecondPerSecond.of(this)
inline val Double.rotationsPerSecondPerSecond: AngularAcceleration get() = RotationsPerSecondPerSecond.of(this)
inline val Double.radiansPerSecondPerSecond: AngularAcceleration get() = RadiansPerSecondPerSecond.of(this)


//Mass
inline val Mass.asKilograms: Double get() = `in`(Kilograms)
inline val Mass.asGrams: Double get() = `in`(Grams)
inline val Mass.asPounds: Double get() = `in`(Pounds)
inline val Mass.asOunces: Double get() = `in`(Ounces)

inline val Double.kilograms: Mass get() = Kilograms.of(this)
inline val Double.grams: Mass get() = Grams.of(this)
inline val Double.pounds: Mass get() = Pounds.of(this)
inline val Double.ounces: Mass get() = Ounces.of(this)


//The Force
inline val Force.asNewtons: Double get() = `in`(Newtons)
inline val Force.asOuncesForce: Double get() = `in`(OuncesForce)
inline val Force.asPoundsForce: Double get() = `in`(PoundsForce)

inline val Double.newtons: Force get() = Newtons.of(this)
inline val Double.ouncesForce: Force get() = OuncesForce.of(this)
inline val Double.poundsForce: Force get() = PoundsForce.of(this)


//Torque
inline val Torque.asNewtonMeters: Double get() = `in`(NewtonMeters)
inline val Torque.asPoundFeet: Double get() = `in`(PoundFeet)
inline val Torque.asOunceInches: Double get() = `in`(OunceInches)

inline val Double.newtonMeters: Torque get() = NewtonMeters.of(this)
inline val Double.poundFeet: Torque get() = PoundFeet.of(this)
inline val Double.ounceInches: Torque get() = OunceInches.of(this)


//MOI
inline val Double.kilogramSquareMeters: MomentOfInertia get() = KilogramSquareMeters.of(this)

inline val MomentOfInertia.asKilogramSquareMeters: Double get() = `in`(KilogramSquareMeters)


//Voltage
inline val Double.volts: Voltage get() = Volts.of(this)

inline val Voltage.asVolts: Double get() = `in`(Volts)


//Current
inline val Current.asAmps: Double get() = `in`(Amps)

inline val Double.amps: Current get() = Amps.of(this)

//Temperature
inline val Double.celsius: Temperature get() = Celsius.of(this)
inline val Double.fahrenheit: Temperature get() = Fahrenheit.of(this)

inline val Temperature.asCelsius: Double get() = `in`(Celsius)
inline val Temperature.asFahrenheit: Double get() = `in`(Fahrenheit)


//Other
inline val Double.voltsPerSecond: Velocity<VoltageUnit> get() = Volts.per(Second).of(this)

inline val Velocity<VoltageUnit>.asVoltsPerSecond: Double get() = `in`(Volts.per(Second))


//Formulas
fun Measure<LinearVelocityUnit>.toAngular(radius: Measure<DistanceUnit>) = RadiansPerSecond.of(this.asMetersPerSecond / radius.asMeters)!!
fun AngularVelocity.toLinear(radius: Measure<DistanceUnit>) = MetersPerSecond.of(this.asRadiansPerSecond * radius.asMeters)!!


/**Converts a [Double] in hertz into an equivalent [Time] unit.*/
fun Double.hertzToTime() = if (this == 0.0) 0.0.seconds else (1.0 / this).seconds

@JvmName("sinOf")
fun sin(angle: Angle) = sin(angle.asRadians)
@JvmName("cosOf")
fun cos(angle: Angle) = cos(angle.asRadians)
@JvmName("tanOf")
fun tan(angle: Angle) = tan(angle.asRadians)
fun Angle.sin() = sin(this)
fun Angle.cos() = cos(this)
fun Angle.tan() = tan(this)

fun asin(value: Double) = kotlin.math.asin(value).radians
fun acos(value: Double) = kotlin.math.acos(value).radians
fun atan(value: Double) = kotlin.math.atan(value).radians

fun atan2(y: Measure<DistanceUnit>, x: Measure<DistanceUnit>) = atan2(y.asInches, x.asInches).radians

fun Angle.wrap() = asDegrees.IEEErem(360.0).degrees
fun Angle.unWrap(nearByAngle: Angle): Angle = nearByAngle + (this - nearByAngle).wrap()

fun Rotation2d.wrap() = measure.wrap().asRotation2d
fun Rotation2d.unWrap(nearByAngle: Angle) = measure.unWrap(nearByAngle).asRotation2d

fun Angle.absoluteValue() = asDegrees.absoluteValue.degrees
fun Rotation2d.absoluteValue() = degrees.absoluteValue.degrees.asRotation2d
fun Measure<DistanceUnit>.absoluteValue() = asFeet.absoluteValue.feet
fun AngularVelocity.absoluteValue() = asDegreesPerSecond.absoluteValue.degreesPerSecond
fun Measure<LinearVelocityUnit>.absoluteValue() = asFeetPerSecond.absoluteValue.feetPerSecond
fun AngularAcceleration.absoluteValue() = asDegreesPerSecondPerSecond.absoluteValue.degreesPerSecondPerSecond
fun Measure<LinearAccelerationUnit>.absoluteValue() = asFeetPerSecondPerSecond.absoluteValue.feetPerSecondPerSecond

//String
fun Angle.toReadableString() = "$asDegrees degrees"
@JvmName("DistanceToReadableString")
fun Measure<DistanceUnit>.toReadableString() = "$asFeet feet"
fun AngularVelocity.toReadableString() = "$asDegreesPerSecond degrees/second"
@JvmName("LinearVelocityToReadableString")
fun Measure<LinearVelocityUnit>.toReadableString() = "$asFeetPerSecond feet/second"
fun AngularAcceleration.toReadableString() = "$asDegreesPerSecondPerSecond degrees/second^2"
@JvmName("LinearAccelerationToReadableString")
fun Measure<LinearAccelerationUnit>.toReadableString() = "$asFeetPerSecondPerSecond feet/second^2"
fun Time.toReadableString() = "$asSeconds seconds"
fun Voltage.toReadableString() = "$asVolts volts"

