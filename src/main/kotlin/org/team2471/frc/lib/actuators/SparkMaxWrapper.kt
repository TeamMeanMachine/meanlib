package org.team2471.frc.lib.actuators

import com.revrobotics.*
import org.team2471.frc.lib.units.Angle
import org.team2471.frc.lib.units.degrees

const val TICKS_PER_REVOLUTION = 42.0

class SparkMaxWrapper (override val deviceID: Int) : IMotorController {
    var positionSetpoint: Double = 0.0
    var velocitySetPoint: Double = 0.0
    val maxRPM = 5700.0
    val canID = deviceID

    private val _motorController = CANSparkMax(deviceID, CANSparkLowLevel.MotorType.kBrushless ).apply { restoreFactoryDefaults() }

    val analogPosition: Double
        get() = _motorController.getAnalog(SparkAnalogSensor.Mode.kAbsolute).position //might not work 2024 wpilib 1/8/2024

    val analogAngle: Double
        get() = analogPosition * 360.0/3.036 - 15.65
    fun init() {
    }

    val hasErrors: Boolean
        get() = _motorController.faults > 0

    override fun follow(followerID: CoreTalonFX) {
        _motorController.follow((followerID as SparkMaxWrapper)._motorController, getInverted() != followerID.getInverted())
    }

    override fun getSelectedSensorPosition(): Double {
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
    override fun setInverted(invert: Boolean) {
        _motorController.inverted = invert
    }

//    fun setInverted(invertType: InvertedValue?) {//untested
//        _motorController.inverted = (invertType == InvertedValue.CounterClockwise_Positive)//could be "Clockwise_Positive"
//    }

    override fun getInverted(): Boolean {//untested
        return _motorController.inverted
    }


    override fun getSelectedSensorVelocity(): Double {//untested
        return (_motorController.encoder.velocity * TICKS_PER_REVOLUTION / 10.0)
    }

    override fun setSelectedSensorPosition(sensorPos: Double) {//untested
        _motorController.encoder.position = sensorPos
    }

    override fun setControl(request: DutyCycleOut?): StatusCode {//untested
        if (request != null) {
            _motorController.set(request.Output)
        }
        return StatusCode.OK
    }
    override fun setControl(request: VelocityDutyCycle): StatusCode {//untested
        velocitySetPoint = request.Velocity / TICKS_PER_REVOLUTION * 10.0

//      handle out of bounds conditions
        if (velocitySetPoint > maxRPM) {
            velocitySetPoint = maxRPM
        } else if(velocitySetPoint < (-1 * maxRPM)) {
            velocitySetPoint = -1 * maxRPM
        }

//      set reference point of
        _motorController.pidController.setReference(velocitySetPoint, CANSparkBase.ControlType.kVelocity, 0, request.FeedForward)
        return StatusCode.OK
    }
    override fun setControl(request: PositionDutyCycle): StatusCode {//untested
        positionSetpoint = request.Position / TICKS_PER_REVOLUTION
        _motorController.pidController.setReference(positionSetpoint, CANSparkBase.ControlType.kPosition, 0, request.FeedForward)
//      println("positionSetpoint = $positionSetpoint position=${_motorController.getEncoder().position}")
        return StatusCode.OK
    }




    override fun setAngle(setPoint : Angle){//untested
        setControl(PositionDutyCycle((setPoint - getSelectedSensorPosition().degrees).wrap().asDegrees))
    }

    override fun config_kP(value: Double) {//untested
        _motorController.pidController.p = value
    }

    override fun config_kD(value: Double) {//untested
        _motorController.pidController.d = value
        println("kD=$value")
    }

    override fun getDValue() : Double {
        return _motorController.pidController.d
    }

    override fun config_kF(value: Double) {//untested
        _motorController.pidController.ff = value
    }

    override fun config_kI(value: Double) {//untested
        _motorController.pidController.i = value
    }

    val current: Double
        get() = _motorController.outputCurrent

    override fun restoreFactoryDefaults() {
        _motorController.restoreFactoryDefaults()
    }

    override fun setCurrentLimit(currLimit: Int) {
        _motorController.setSmartCurrentLimit(currLimit)
    }

}