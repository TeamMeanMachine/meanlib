package org.team2471.frc.lib.actuators

import com.revrobotics.spark.*
import com.revrobotics.spark.config.*

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
        inputs.temp = _motorController.motorTemperature

        this.inputs = inputs
    }

    private val _motorController = SparkMax(deviceID, SparkLowLevel.MotorType.kBrushless ).apply { restoreFactoryDefaults() }

    val analogPosition: Double
        get() = _motorController.analog.position

    val analogAngle: Double
        get() = analogPosition * 360.0/3.036 - 15.65

    init {
        println("Creating Spark motor  ID: $deviceID")
    }

    override fun follow(followerID: MotorControllerIO) {
        applyConfig(SparkMaxConfig().follow((followerID as SparkMaxWrapper)._motorController))
    }

    override fun getClosedLoopError(): Double {
        return positionSetpoint - getSelectedSensorPosition()
    }

    override fun getSelectedSensorPosition(): Double {
        return (inputs.position/* * TICKS_PER_REVOLUTION*/)
    }

    /**
     * Attempt to get encoder plugged directly into SparkMAX. Has not worked yet.
     */
    fun getAlternateEncoder(): Double {
        return _motorController.alternateEncoder.position
    }

    override fun closedLoopRamp(secondsToFull: Double) {
        applyConfig(SparkMaxConfig().closedLoopRampRate(secondsToFull))
    }

    override fun coastMode() {
        applyConfig(SparkMaxConfig().idleMode(SparkBaseConfig.IdleMode.kCoast))
    }

    override fun setInverted(invert: Boolean) {
        applyConfig(SparkMaxConfig().inverted(invert))
    }

    override fun getInverted(): Boolean {
        return _motorController.configAccessor.inverted
    }

    override fun getSelectedSensorVelocity(): Double {
        return inputs.velocity
    }
    override fun getSelectedSensorAcceleration(): Double {
        println("getSelectedSensorAcceleration() not supported by SparkMax")
        return 0.0
    }

    override fun openLoopRamp(secondsToFull: Double) {
        applyConfig(SparkMaxConfig().openLoopRampRate(secondsToFull))
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
        _motorController.closedLoopController.setReference(velocitySetPoint, SparkBase.ControlType.kVelocity, 0)
    }

    override fun setVelocitySetpoint(velocity: Double, feedForward: Double) {
        velocitySetPoint = velocity * 60.0 // RPS to RPM

//      handle out of bounds conditions
        if (velocitySetPoint > maxRPM) {
            velocitySetPoint = maxRPM
        } else if(velocitySetPoint < (-1 * maxRPM)) {
            velocitySetPoint = -1 * maxRPM
        }

//      set reference point of
        _motorController.closedLoopController.setReference(velocitySetPoint, SparkBase.ControlType.kVelocity, 0, feedForward)
    }

    override fun stop() {
        _motorController.set(0.0)
    }

    override fun setPositionSetpoint(position: Double) {
        positionSetpoint = position
        _motorController.closedLoopController.setReference(positionSetpoint, SparkBase.ControlType.kPosition, 0)
    //      println("positionSetpoint = $positionSetpoint position=${_motorController.getEncoder().position}")
    }

    override fun setPositionSetpoint(position: Double, feedForward: Double) {
        positionSetpoint = position
        _motorController.closedLoopController.setReference(positionSetpoint, SparkBase.ControlType.kPosition, 0, feedForward)
//      println("positionSetpoint = $positionSetpoint position=${_motorController.getEncoder().position}")
    }

    override fun setMotionMagicSetpoint(position: Double) {
        _motorController.closedLoopController.setReference(position, SparkBase.ControlType.kMAXMotionPositionControl)
    }

    override fun config_kP(p: Double, simP: Double?) {
        applyConfig(SparkMaxConfig().apply { closedLoop.p(p * 1024.0) })
    }

    override fun config_kD(d: Double, simD: Double?) {
        applyConfig(SparkMaxConfig().apply { closedLoop.d(d * 1024.0) })
    }

    override fun getPValue(): Double = _motorController.configAccessor.closedLoop.p
    override fun getDValue() : Double = _motorController.configAccessor.closedLoop.d
    override fun getIValue(): Double = _motorController.configAccessor.closedLoop.i
    override fun getFValue(): Double = _motorController.configAccessor.closedLoop.ff


    override fun config_kF(f: Double, simF: Double?) {
        val c = _motorController.configAccessor.closedLoop
        applyConfig(SparkMaxConfig().apply { closedLoop.pidf(c.p, c.i, c.d, f) })
    }

    override fun config_kI(i: Double, simI: Double?) {
        applyConfig(SparkMaxConfig().apply { closedLoop.i(i * 1024.0) })
    }

    override val current: Double
        get() = inputs.current

    override fun brakeMode() {
        applyConfig(SparkMaxConfig().idleMode(SparkBaseConfig.IdleMode.kBrake))
    }

    override fun restoreFactoryDefaults() {
        _motorController.configure(SparkMaxConfig(), SparkBase.ResetMode.kResetSafeParameters, SparkBase.PersistMode.kPersistParameters)
    }

    override fun currentLimit(continuousLimit: Int, peakLimit: Int, peakDuration: Double) {
        if (peakDuration == 0.0) {
            applyConfig(SparkMaxConfig().apply { smartCurrentLimit(continuousLimit, peakLimit) })
        } else {
            applyConfig(SparkMaxConfig().apply { smartCurrentLimit(peakLimit) })

        }

    }

    override fun motionMagic(acceleration: Double, cruisingVelocity: Double) {
        applyConfig(SparkMaxConfig().apply { closedLoop.maxMotion
            .maxVelocity(cruisingVelocity)
            .maxAcceleration(acceleration)
            .allowedClosedLoopError(0.0)
        })
    }

    private fun applyConfig(newConfig: SparkBaseConfig) {
        _motorController.configure(newConfig, SparkBase.ResetMode.kNoResetSafeParameters, SparkBase.PersistMode.kNoPersistParameters)
    }
}