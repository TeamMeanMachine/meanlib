@file:Suppress("UNUSED")

package org.team2471.frc.lib.units

import org.wpilib.math.geometry.Rotation2d
import org.wpilib.units.*
import org.wpilib.units.Units.*
import org.wpilib.units.Unit
import org.wpilib.units.measure.*
import kotlin.div
import kotlin.math.*

//Unit Conversions

//Distance
inline val Measure<DistanceUnit>.asInches: Double get() = `in`(Inches)
inline val Measure<DistanceUnit>.asFeet: Double get() = `in`(Feet)
inline val Measure<DistanceUnit>.asMeters: Double get() = `in`(Meters)
inline val Measure<DistanceUnit>.asCentimeters: Double get() = `in`(Centimeters)
inline val Measure<DistanceUnit>.asMillimeters: Double get() = `in`(Millimeters)

inline val Double.inches: Distance get() = Inches.of(this)
inline val Double.feet: Distance get() = Feet.of(this)
inline val Double.meters: Distance get() = Meters.of(this)
inline val Double.centimeters: Distance get() = Centimeters.of(this)
inline val Double.millimeters: Distance get() = Millimeters.of(this)


//Angle
inline val Measure<AngleUnit>.asDegrees: Double get() = `in`(Degrees)
inline val Measure<AngleUnit>.asRotations: Double get() = `in`(Rotations)
inline val Measure<AngleUnit>.asRadians: Double get() = `in`(Radians)

inline val Double.degrees: Angle get() = Degrees.of(this)
inline val Double.rotations: Angle get() = Rotations.of(this)
inline val Double.radians: Angle get() = Radians.of(this)

//Rotation2d
inline val Measure<AngleUnit>.asRotation2d: Rotation2d get() = Rotation2d.fromRadians(this.asRadians)


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

inline val Measure<DistanceUnit>.perSecond: LinearVelocity get() = MetersPerSecond.of(this.asMeters)

inline val Double.inchesPerSecond: LinearVelocity get() = InchesPerSecond.of(this)
inline val Double.feetPerSecond: LinearVelocity get() = FeetPerSecond.of(this)
inline val Double.metersPerSecond: LinearVelocity get() = MetersPerSecond.of(this)


//Angular Velocity
inline val Measure<AngularVelocityUnit>.asDegreesPerSecond: Double get() = `in`(DegreesPerSecond)
inline val Measure<AngularVelocityUnit>.asRotationsPerSecond: Double get() = `in`(RotationsPerSecond)
inline val Measure<AngularVelocityUnit>.asRPM: Double get() = `in`(RPM)
inline val Measure<AngularVelocityUnit>.asRadiansPerSecond: Double get() = `in`(RadiansPerSecond)

inline val Measure<AngleUnit>.perSecond: AngularVelocity get() = RadiansPerSecond.of(this.asRadians)

inline val Double.degreesPerSecond: AngularVelocity get() = DegreesPerSecond.of(this)
inline val Double.rotationsPerSecond: AngularVelocity get() = RotationsPerSecond.of(this)
inline val Double.rpm: AngularVelocity get() = RPM.of(this)
inline val Double.radiansPerSecond: AngularVelocity get() = RadiansPerSecond.of(this)


//Linear Acceleration
inline val Measure<LinearAccelerationUnit>.asInchesPerSecondPerSecond: Double get() = `in`(InchesPerSecond.per(Second))
inline val Measure<LinearAccelerationUnit>.asFeetPerSecondPerSecond: Double get() = `in`(FeetPerSecondPerSecond)
inline val Measure<LinearAccelerationUnit>.asMetersPerSecondPerSecond: Double get() = `in`(MetersPerSecondPerSecond)
inline val Measure<LinearAccelerationUnit>.asGs: Double get() = `in`(Gs)

inline val Measure<DistanceUnit>.perSecondPerSecond: LinearAcceleration get() = MetersPerSecondPerSecond.of(this.asMeters)
inline val Measure<LinearVelocityUnit>.perSecond: LinearAcceleration get() = MetersPerSecondPerSecond.of(this.asMetersPerSecond)

inline val Double.inchesPerSecondPerSecond: LinearAcceleration get() = InchesPerSecond.per(Second).of(this)
inline val Double.feetPerSecondPerSecond: LinearAcceleration get() = FeetPerSecondPerSecond.of(this)
inline val Double.metersPerSecondPerSecond: LinearAcceleration get() = MetersPerSecondPerSecond.of(this)
inline val Double.Gs: LinearAcceleration get() = Units.Gs.of(this)


//Angular Acceleration
inline val Measure<AngularAccelerationUnit>.asDegreesPerSecondPerSecond: Double get() = `in`(DegreesPerSecondPerSecond)
inline val Measure<AngularAccelerationUnit>.asRotationsPerSecondPerSecond: Double get() = `in`(RotationsPerSecondPerSecond)
inline val Measure<AngularAccelerationUnit>.asRadiansPerSecondPerSecond: Double get() = `in`(RadiansPerSecondPerSecond)

inline val Measure<AngleUnit>.perSecondPerSecond: AngularAcceleration get() = RadiansPerSecondPerSecond.of(this.asRadians)
inline val Measure<AngularVelocityUnit>.perSecond: AngularAcceleration get() = RadiansPerSecondPerSecond.of(this.asRadiansPerSecond)

inline val Double.degreesPerSecondPerSecond: AngularAcceleration get() = DegreesPerSecondPerSecond.of(this)
inline val Double.rotationsPerSecondPerSecond: AngularAcceleration get() = RotationsPerSecondPerSecond.of(this)
inline val Double.radiansPerSecondPerSecond: AngularAcceleration get() = RadiansPerSecondPerSecond.of(this)


//Linear Jerk
inline val Measure<VelocityUnit<LinearAccelerationUnit>>.asInchesPerSecondCubed: Double get() = `in`(InchesPerSecond.per(Second).per(Second))
inline val Measure<VelocityUnit<LinearAccelerationUnit>>.asFeetPerSecondCubed: Double get() = `in`(FeetPerSecondPerSecond.per(Second))
inline val Measure<VelocityUnit<LinearAccelerationUnit>>.asMetersPerSecondCubed: Double get() = `in`(MetersPerSecondPerSecond.per(Second))

@get:JvmName("getLinearPerSecondCubed")
inline val Measure<DistanceUnit>.perSecondCubed: Velocity<LinearAccelerationUnit> get() = MetersPerSecondPerSecond.per(Second).of(this.asMeters)
@get:JvmName("getLinearAccelerationPerSecond")
inline val Measure<LinearAccelerationUnit>.perSecond: Velocity<LinearAccelerationUnit> get() = MetersPerSecondPerSecond.per(Second).of(this.asMetersPerSecondPerSecond)

inline val Double.inchesPerSecondCubed: Velocity<LinearAccelerationUnit> get() = InchesPerSecond.per(Second).per(Second).of(this)
inline val Double.feetPerSecondCubed: Velocity<LinearAccelerationUnit> get() = FeetPerSecondPerSecond.per(Second).of(this)
inline val Double.metersPerSecondCubed: Velocity<LinearAccelerationUnit> get() = MetersPerSecondPerSecond.per(Second).of(this)


//Angular Jerk
inline val Measure<VelocityUnit<AngularAccelerationUnit>>.asDegreesPerSecondCubed: Double get() = `in`(DegreesPerSecondPerSecond.per(Second))
inline val Measure<VelocityUnit<AngularAccelerationUnit>>.asRotationsPerSecondCubed: Double get() = `in`(RotationsPerSecondPerSecond.per(Second))
inline val Measure<VelocityUnit<AngularAccelerationUnit>>.asRadiansPerSecondCubed: Double get() = `in`(RadiansPerSecondPerSecond.per(Second))

@get:JvmName("getAngularPerSecondCubed")
inline val Measure<AngleUnit>.perSecondCubed: Velocity<AngularAccelerationUnit> get() = RadiansPerSecondPerSecond.per(Second).of(this.asRadians)
@get:JvmName("getAngularAccelerationPerSecond")
inline val Measure<AngularAccelerationUnit>.perSecond: Velocity<AngularAccelerationUnit> get() = RadiansPerSecondPerSecond.per(Second).of(this.asRadiansPerSecondPerSecond)

inline val Double.degreesPerSecondCubed: Velocity<AngularAccelerationUnit> get() = DegreesPerSecondPerSecond.per(Second).of(this)
inline val Double.rotationsPerSecondCubed: Velocity<AngularAccelerationUnit> get() = RotationsPerSecondPerSecond.per(Second).of(this)
inline val Double.radiansPerSecondCubed: Velocity<AngularAccelerationUnit> get() = RadiansPerSecondPerSecond.per(Second).of(this)


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

// Energy

inline val Energy.asJoules: Double get() = `in`(Joules)
inline val Energy.asWattHours: Double get() = asJoules / 3600.0

inline val Double.joules: Energy get() = Joules.of(this)
inline val Double.wattHours: Energy get() = Joules.of(this * 3600.0)



//Voltage
inline val Double.volts: Voltage get() = Volts.of(this)

inline val Voltage.asVolts: Double get() = `in`(Volts)


//Current
inline val Current.asAmps: Double get() = `in`(Amps)

inline val Double.amps: Current get() = Amps.of(this)

// Power
inline val Power.asWatts: Double get() = `in`(Watts)
inline val Power.asHorsepower: Double get() = `in`(Horsepower)

inline val Double.watts: Power get() = Watts.of(this)
inline val Double.horsepower: Power get() = Horsepower.of(this)


//Temperature
inline val Double.celsius: Temperature get() = Celsius.of(this)
inline val Double.fahrenheit: Temperature get() = Fahrenheit.of(this)

inline val Temperature.asCelsius: Double get() = `in`(Celsius)
inline val Temperature.asFahrenheit: Double get() = `in`(Fahrenheit)


//Other
inline val Double.voltsPerSecond: Velocity<VoltageUnit> get() = Volts.per(Second).of(this)

inline val Velocity<VoltageUnit>.asVoltsPerSecond: Double get() = `in`(Volts.per(Second))


//Formulas
fun Measure<LinearVelocityUnit>.toAngular(radius: Measure<DistanceUnit>): AngularVelocity = RadiansPerSecond.of(this.asMetersPerSecond / radius.asMeters)
fun Measure<AngularVelocityUnit>.toLinear(radius: Measure<DistanceUnit>): LinearVelocity = MetersPerSecond.of(this.asRadiansPerSecond * radius.asMeters)


/**Converts a [Double] in hertz into an equivalent [Time] unit.*/
fun Double.hertzToTime() = if (this == 0.0) Double.POSITIVE_INFINITY.seconds else (1.0 / this).seconds

@JvmName("sinOf")
fun sin(angle: Measure<AngleUnit>) = sin(angle.asRadians)
@JvmName("cosOf")
fun cos(angle: Measure<AngleUnit>) = cos(angle.asRadians)
@JvmName("tanOf")
fun tan(angle: Measure<AngleUnit>) = tan(angle.asRadians)
fun Measure<AngleUnit>.sin() = sin(this)
fun Measure<AngleUnit>.cos() = cos(this)
fun Measure<AngleUnit>.tan() = tan(this)

fun asin(value: Double) = kotlin.math.asin(value).radians
fun acos(value: Double) = kotlin.math.acos(value).radians
fun atan(value: Double) = kotlin.math.atan(value).radians

fun atan2(y: Measure<DistanceUnit>, x: Measure<DistanceUnit>) = atan2(y.asMeters, x.asMeters).radians

fun Measure<AngleUnit>.wrap() = asDegrees.IEEErem(360.0).degrees
fun Measure<AngleUnit>.unWrap(nearByAngle: Angle): Angle = nearByAngle + (this - nearByAngle).wrap()

fun Rotation2d.wrap() = measure.wrap().asRotation2d
fun Rotation2d.unWrap(nearByAngle: Angle) = measure.unWrap(nearByAngle).asRotation2d

fun Rotation2d.absoluteValue(): Rotation2d = Rotation2d.fromRadians(radians.absoluteValue)
fun Angle.absoluteValue(): Angle = if (magnitude() < 0.0) -this else this
fun Distance.absoluteValue(): Distance = if (magnitude() < 0.0) -this else this
fun AngularVelocity.absoluteValue(): AngularVelocity = if (magnitude() < 0.0) -this else this
fun LinearVelocity.absoluteValue(): LinearVelocity = if (magnitude() < 0.0) -this else this
fun AngularAcceleration.absoluteValue(): AngularAcceleration = if (magnitude() < 0.0) -this else this
fun LinearAcceleration.absoluteValue(): LinearAcceleration = if (magnitude() < 0.0) -this else this

//String
@JvmName("angleToReadableString")
fun Measure<AngleUnit>.toReadableString() = "$asDegrees degrees"
@JvmName("distanceToReadableString")
fun Measure<DistanceUnit>.toReadableString() = "$asFeet feet"
@JvmName("angularVelocityToReadableString")
fun Measure<AngularVelocityUnit>.toReadableString() = "$asDegreesPerSecond degrees/second"
@JvmName("linearVelocityToReadableString")
fun Measure<LinearVelocityUnit>.toReadableString() = "$asFeetPerSecond feet/second"
@JvmName("angularAccelerationToReadableString")
fun Measure<AngularAccelerationUnit>.toReadableString() = "$asDegreesPerSecondPerSecond degrees/second^2"
@JvmName("linearAccelerationToReadableString")
fun Measure<LinearAccelerationUnit>.toReadableString() = "$asFeetPerSecondPerSecond feet/second^2"
fun Time.toReadableString() = "$asSeconds seconds"
fun Voltage.toReadableString() = "$asVolts volts"

