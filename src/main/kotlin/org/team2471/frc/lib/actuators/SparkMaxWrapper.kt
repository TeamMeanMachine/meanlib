package org.team2471.frc.lib.actuators

import com.ctre.phoenix6.signals.NeutralModeValue
import com.revrobotics.*
import org.team2471.frc.lib.math.DoubleRange
import org.team2471.frc.lib.units.Angle
import org.team2471.frc.lib.units.degrees

const val TICKS_PER_REVOLUTION = 42.0

class SparkMaxWrapper (override val deviceID: Int) : IMotorController {
    private var positionSetpoint: Double = 0.0
    private var velocitySetPoint: Double = 0.0
    val maxRPM = 5700.0
    val canID = deviceID

    override var feedbackCoefficient: Double = 1.0
    override var timeoutMs: Int = 100
    override var rawOffset: Int = 0
    override val motorOutputPercent: Double
        get() = _motorController.appliedOutput

    private val _motorController = CANSparkMax(deviceID, CANSparkLowLevel.MotorType.kBrushless ).apply { restoreFactoryDefaults() }

    val analogPosition: Double
        get() = _motorController.getAnalog(SparkAnalogSensor.Mode.kAbsolute).position //might not work 2024 wpilib 1/8/2024

    val analogAngle: Double
        get() = analogPosition * 360.0/3.036 - 15.65
    fun init() {
        println("creating spark")
    }

    val hasErrors: Boolean
        get() = _motorController.faults > 0

    override fun follow(followerID: IMotorController) {
        _motorController.follow((followerID as SparkMaxWrapper)._motorController, getInverted() != followerID.getInverted())
    }

    override fun getClosedLoopError(pidIdx: Int): Double {
        return (positionSetpoint * TICKS_PER_REVOLUTION)- getSelectedSensorPosition(pidIdx)
    }

    override fun getSelectedSensorPosition(pidIdx: Int): Double {
        return (_motorController.encoder.position * TICKS_PER_REVOLUTION)
    }

    override fun setNeutralMode(neutralMode: NeutralModeValue?) {
        when (neutralMode) {
            NeutralModeValue.Brake -> _motorController.idleMode = CANSparkBase.IdleMode.kBrake
            NeutralModeValue.Coast -> _motorController.idleMode = CANSparkBase.IdleMode.kCoast
            else -> {}
        }
    }


    override fun burnFlash() {
        println("Burned Flash for ${_motorController.deviceId}")
        _motorController.burnFlash()
    }

    override fun closedLoopRamp(secondsToFull: Double) {
        println("no")
    }

    override fun coastMode() {
        _motorController.idleMode = CANSparkBase.IdleMode.kCoast
    }

    override fun setInverted(invert: Boolean) {
        _motorController.inverted = invert
    }

    override fun setMotionMagicSetpoint(position: Double) {
        println("no")
    }

    override fun setMotionMagicSetpoint(position: Double, feedForward: Double) {
        println("no")
    }

//    fun setInverted(invertType: InvertedValue?) {//untested
//        _motorController.inverted = (invertType == InvertedValue.CounterClockwise_Positive)//could be "Clockwise_Positive"
//    }

    override fun getInverted(): Boolean {//untested
        return _motorController.inverted
    }


    override fun getSelectedSensorVelocity(pidIdx: Int): Double {//untested
        return (_motorController.encoder.velocity * TICKS_PER_REVOLUTION / 10.0)
    }

    override fun motionMagic(acceleration: Double, cruisingVelocity: Double) {
        println("no")
    }

    override fun openLoopRamp(secondsToFull: Double) {
        println("no")
    }

    override fun peakOutputRange(range: DoubleRange) {
        println("no")
    }

    override fun setSelectedSensorPosition(sensorPos: Double, pidIdx: Int) {//untested
        _motorController.encoder.position = sensorPos
    }

    override fun setStatusFramePeriod(periodMs: Int, timeoutMs: Int) {
        println("no")
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
        setPositionSetpoint(((setPoint - getSelectedSensorPosition(0).degrees).wrap()).asDegrees)
    }

    override fun config_kP(p: Double) {//untested
        _motorController.pidController.p = p
    }

    override fun config_kD(d: Double) {//untested
        _motorController.pidController.d = d
        println("kD=$d")
    }

    override fun getDValue() : Double {
        return _motorController.pidController.d
    }

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
        println("no")
    }

}