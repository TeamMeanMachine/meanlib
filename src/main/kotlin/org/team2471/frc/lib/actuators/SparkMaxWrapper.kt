package org.team2471.frc.lib.actuators

import com.ctre.phoenix6.signals.NeutralModeValue
import com.revrobotics.*
import org.team2471.frc.lib.math.DoubleRange
import org.team2471.frc.lib.units.Angle
import org.team2471.frc.lib.units.degrees

const val TICKS_PER_REVOLUTION = 1.0

class SparkMaxWrapper (override val deviceID: Int) : IMotorController {
    private var positionSetpoint: Double = 0.0
    private var velocitySetPoint: Double = 0.0
    val maxRPM = 5700.0
    val canID = deviceID

//    override var feedbackCoefficient: Double = 1.0
    override var timeoutSec: Double = 0.050
    override var rawOffset: Int = 0
    override val motorOutputPercent: Double
        get() = _motorController.appliedOutput

    private val _motorController = CANSparkMax(deviceID, CANSparkLowLevel.MotorType.kBrushless ).apply { restoreFactoryDefaults() }

    val analogPosition: Double
        get() = _motorController.getAnalog(SparkAnalogSensor.Mode.kAbsolute).position

    val analogAngle: Double
        get() = analogPosition * 360.0/3.036 - 15.65

    val hasErrors: Boolean
        get() = _motorController.faults > 0

    init {
        println("Creating Spark motor  ID: $deviceID")
    }

    override fun follow(followerID: IMotorController) {
        _motorController.follow((followerID as SparkMaxWrapper)._motorController, getInverted() != followerID.getInverted())
    }

    override fun getClosedLoopError(): Double {
        return (positionSetpoint * TICKS_PER_REVOLUTION) - getSelectedSensorPosition()
    }

    override fun getSelectedSensorPosition(): Double {
        return (_motorController.encoder.position/* * TICKS_PER_REVOLUTION*/)
    }

    /**
     * Attempt to get encoder plugged directly into SparkMAX. Has not worked yet.
     *
     * @param countPerRev the counts per revolution of the alternate encoder. Can be found in the Alternate Encoder SparkMAX guide
     */
    fun getAlternateEncoder(countPerRev: Int): Double {
        return _motorController.getAlternateEncoder(SparkMaxAlternateEncoder.Type.kQuadrature, countPerRev).position
    }

    override fun setNeutralMode(neutralMode: NeutralModeValue?) {
        when (neutralMode) {
            NeutralModeValue.Brake -> brakeMode()
            NeutralModeValue.Coast -> coastMode()
            else -> {}
        }
    }

    override fun burnFlash() {
        println("Burned Flash for ${_motorController.deviceId}")
        _motorController.burnFlash()
    }

    override fun closedLoopRamp(secondsToFull: Double) {
        _motorController.closedLoopRampRate = secondsToFull
    }

    override fun coastMode() {
        _motorController.idleMode = CANSparkBase.IdleMode.kCoast
    }

    override fun setInverted(invert: Boolean) {
        _motorController.inverted = invert
    }

    override fun setMotionMagicSetpoint(position: Double) {
        println("setMotionMagicSetpoint not supported by SparkMax")
    }

    override fun setMotionMagicSetpoint(position: Double, feedForward: Double) {
        println("setMotionMagicSetpoint not supported by SparkMax")
    }

    override fun getInverted(): Boolean {//untested
        return _motorController.inverted
    }

    override fun getSelectedSensorVelocity(): Double {//untested
        return (_motorController.encoder.velocity * TICKS_PER_REVOLUTION / 10.0)
    }

    override fun motionMagic(acceleration: Double, cruisingVelocity: Double) {
        println("motionMagic not supported by SparkMax")
    }

    override fun openLoopRamp(secondsToFull: Double) {
        _motorController.openLoopRampRate = secondsToFull
    }

    override fun peakOutputRange(range: DoubleRange) {
        println("peakOutputRange not supported by SparkMax")
    }

    override fun setSelectedSensorPosition(sensorPos: Double) {//untested
        _motorController.encoder.position = sensorPos
    }

    override fun setStatusFramePeriod(periodMs: Int, timeoutSec: Double) {
        println("setStatusFramePeriod not supported by SparkMax")
    }

    override fun setPercentOutput(percent: Double) {//untested
        _motorController.set(percent)
    }

    override fun setVelocitySetpoint(velocity: Double) {//untested
        velocitySetPoint = velocity / TICKS_PER_REVOLUTION * 10.0

//      handle out of bounds conditions
        if (velocitySetPoint > maxRPM) {
            velocitySetPoint = maxRPM
        } else if(velocitySetPoint < (-1 * maxRPM)) {
            velocitySetPoint = -1 * maxRPM
        }

//      set reference point of
        _motorController.pidController.setReference(velocitySetPoint, CANSparkBase.ControlType.kVelocity, 0)
    }

    override fun setVelocitySetpoint(velocity: Double, feedForward: Double) {//untested
        velocitySetPoint = velocity / TICKS_PER_REVOLUTION * 10.0

//      handle out of bounds conditions
        if (velocitySetPoint > maxRPM) {
            velocitySetPoint = maxRPM
        } else if(velocitySetPoint < (-1 * maxRPM)) {
            velocitySetPoint = -1 * maxRPM
        }

//      set reference point of
        _motorController.pidController.setReference(velocitySetPoint, CANSparkBase.ControlType.kVelocity, 0, feedForward)
    }

    override fun stop() {
        _motorController.set(0.0)
    }

    override fun setPositionSetpoint(position: Double) {//untested
        positionSetpoint = position / TICKS_PER_REVOLUTION
        _motorController.pidController.setReference(positionSetpoint, CANSparkBase.ControlType.kPosition, 0)
    //      println("positionSetpoint = $positionSetpoint position=${_motorController.getEncoder().position}")
    }

    override fun setPositionSetpoint(position: Double, feedForward: Double) {//untested
        positionSetpoint = position / TICKS_PER_REVOLUTION
        _motorController.pidController.setReference(positionSetpoint, CANSparkBase.ControlType.kPosition, 0, feedForward)
//      println("positionSetpoint = $positionSetpoint position=${_motorController.getEncoder().position}")
    }

    override fun setAngle(setPoint : Angle){//untested
        setPositionSetpoint(((setPoint - getSelectedSensorPosition().degrees).wrap()).asDegrees)
    }

    override fun config_kP(p: Double) {//untested
        _motorController.pidController.p = p
    }

    override fun config_kD(d: Double) {//untested
        _motorController.pidController.d = d
        println("kD=$d")
    }

    override fun getDValue() : Double = _motorController.pidController.d

    override fun getIValue(): Double = _motorController.pidController.i

    override fun getPValue(): Double = _motorController.pidController.p

    fun config_kF(value: Double) {//untested
        _motorController.pidController.ff = value
    }

    override fun config_kI(i: Double) {//untested
        _motorController.pidController.i = i
    }

    override val current: Double
        get() = _motorController.outputCurrent

    override fun brakeMode() {
        _motorController.idleMode = CANSparkBase.IdleMode.kBrake
    }

    override fun restoreFactoryDefaults() {
        _motorController.restoreFactoryDefaults()
    }

    override fun currentLimit(continuousLimit: Int, peakLimit: Int, peakDuration: Int) {
        _motorController.setSmartCurrentLimit(peakLimit)
    }

    override fun encoderContinuous(continuous: Boolean) {
        println("encoderContinuous not supported by SparkMax")
    }

}