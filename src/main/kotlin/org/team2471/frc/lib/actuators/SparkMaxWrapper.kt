package org.team2471.frc.lib.actuators

import com.revrobotics.*

class SparkMaxWrapper (deviceID: Int) : MotorControllerIO {
    private var positionSetpoint: Double = 0.0
    private var velocitySetPoint: Double = 0.0
    val maxRPM = 5700.0

    private var inputs: MotorControllerIO.MotorControllerIOInputs = MotorControllerIO.MotorControllerIOInputs("null")


    override val outputPercent: Double
        get() = inputs.outputPercent

    override fun updateInputs(inputs: MotorControllerIO.MotorControllerIOInputs) {
        inputs.position = _motorController.encoder.position
        inputs.outputPercent = _motorController.appliedOutput
        inputs.velocity = _motorController.encoder.velocity
        inputs.current = _motorController.outputCurrent

        this.inputs = inputs
    }

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

    override fun follow(followerID: MotorControllerIO) {
        _motorController.follow((followerID as SparkMaxWrapper)._motorController, getInverted() != followerID.getInverted())
    }

    override fun getClosedLoopError(): Double {
        return positionSetpoint - getSelectedSensorPosition()
    }

    override fun getSelectedSensorPosition(): Double {
        return (inputs.position/* * TICKS_PER_REVOLUTION*/)
    }

    /**
     * Attempt to get encoder plugged directly into SparkMAX. Has not worked yet.
     *
     * @param countPerRev the counts per revolution of the alternate encoder. Can be found in the Alternate Encoder SparkMAX guide
     */
    fun getAlternateEncoder(countPerRev: Int): Double {
        return _motorController.getAlternateEncoder(SparkMaxAlternateEncoder.Type.kQuadrature, countPerRev).position
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

    override fun getInverted(): Boolean {
        return _motorController.inverted
    }

    override fun getSelectedSensorVelocity(): Double {
        return inputs.velocity
    }

    override fun openLoopRamp(secondsToFull: Double) {
        _motorController.openLoopRampRate = secondsToFull
    }


    override fun setSelectedSensorPosition(sensorPos: Double) {
        _motorController.encoder.position = sensorPos
    }

    override fun setPercentOutput(percent: Double) {
        _motorController.set(percent)
    }

    override fun setVelocitySetpoint(velocity: Double) {
        velocitySetPoint = velocity * 10.0

//      handle out of bounds conditions
        if (velocitySetPoint > maxRPM) {
            velocitySetPoint = maxRPM
        } else if(velocitySetPoint < (-1 * maxRPM)) {
            velocitySetPoint = -1 * maxRPM
        }

//      set reference point of
        _motorController.pidController.setReference(velocitySetPoint, CANSparkBase.ControlType.kVelocity, 0)
    }

    override fun setVelocitySetpoint(velocity: Double, feedForward: Double) {
        velocitySetPoint = velocity * 10.0

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

    override fun setPositionSetpoint(position: Double) {
        positionSetpoint = position
        _motorController.pidController.setReference(positionSetpoint, CANSparkBase.ControlType.kPosition, 0)
    //      println("positionSetpoint = $positionSetpoint position=${_motorController.getEncoder().position}")
    }

    override fun setPositionSetpoint(position: Double, feedForward: Double) {
        positionSetpoint = position
        _motorController.pidController.setReference(positionSetpoint, CANSparkBase.ControlType.kPosition, 0, feedForward)
//      println("positionSetpoint = $positionSetpoint position=${_motorController.getEncoder().position}")
    }

    override fun config_kP(p: Double, simP: Double?) {
        _motorController.pidController.p = p
    }

    override fun config_kD(d: Double, simD: Double?) {
        _motorController.pidController.d = d
//        println("kD=$d")
    }

    override fun getDValue() : Double = _motorController.pidController.d

    override fun getIValue(): Double = _motorController.pidController.i

    override fun getPValue(): Double = _motorController.pidController.p

    fun config_kF(value: Double) {
        _motorController.pidController.ff = value
    }

    override fun config_kI(i: Double, simI: Double?) {
        _motorController.pidController.i = i
    }

    override val current: Double
        get() = inputs.current

    override fun brakeMode() {
        _motorController.idleMode = CANSparkBase.IdleMode.kBrake
    }

    override fun restoreFactoryDefaults() {
        _motorController.restoreFactoryDefaults()
    }

    override fun currentLimit(continuousLimit: Int, peakLimit: Int, peakDuration: Int) {
        _motorController.setSmartCurrentLimit(peakLimit)
    }
}