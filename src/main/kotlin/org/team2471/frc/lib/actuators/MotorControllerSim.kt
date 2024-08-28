package org.team2471.frc.lib.actuators

import edu.wpi.first.math.controller.PIDController
import edu.wpi.first.math.system.plant.DCMotor
import edu.wpi.first.wpilibj.simulation.DCMotorSim
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.units.*
import org.team2471.frc.lib.util.Timer
import kotlin.math.absoluteValue

@OptIn(DelicateCoroutinesApi::class)
class MotorControllerSim: MotorControllerIO {
    //sim
    private lateinit var sim: DCMotorSim
    private lateinit var motor: DCMotor

    //motor outputs
    private var inputs: MotorControllerIO.MotorControllerIOInputs = MotorControllerIO.MotorControllerIOInputs("null")
    override var outputPercent: Double = 0.0
    override val current: Double get() = inputs.current
    private var acceleration = 0.0

    //control
    private val pid = PIDController(0.0, 0.0, 0.0)
    private var positionSetpoint: Double? = null
    private var feedForward: Double = 0.0

    //break mode
    private var breakMode = false
    private val breakModeVolts: Double
        get() = motor.rOhms * (motor.stallCurrentAmps / motor.freeSpeedRadPerSec.radians.asRotations) * -getSelectedSensorVelocity()

    //ramp rates
    private val rampLimit: Double //0% to 100% power in this many seconds
        get() = if (positionSetpoint == null) openLoopRamp else closedLoopRamp
    private var openLoopRamp = 0.0
    private var closedLoopRamp = 0.0

    private val rampLimitTimer = Timer()
    private val accelerationTimer = Timer()

    private var prevVolts = 0.0
    private var prevVelocity = 0.0


    init {
        restoreFactoryDefaults()
        accelerationTimer.start()

        GlobalScope.launch {
            periodic {
                if (positionSetpoint != null) { //do closed loop control
//                    if (inputs.name == "Drive/FLS") println("error ${getClosedLoopError().round(1)} \t setpoint ${positionSetpoint!!.round(1)} \t angle ${getSelectedSensorPosition().round(1)}")
                    setPercentOutput(
                        pid.calculate(getSelectedSensorPosition(), positionSetpoint ?: getSelectedSensorPosition()) + feedForward
                    )
                }

                val velocity = getSelectedSensorVelocity()
                val calculatedAcceleration = (velocity - prevVelocity) / accelerationTimer.get()


                if (!calculatedAcceleration.isNaN() && calculatedAcceleration.absoluteValue < 1.0E27) {
                    acceleration = calculatedAcceleration
                }
                prevVelocity = velocity
                accelerationTimer.start()
            }
        }
    }


    override fun updateInputs(inputs: MotorControllerIO.MotorControllerIOInputs) {
        sim.update(0.02)

        inputs.position = (sim.angularPositionRotations)
        inputs.velocity = (sim.angularVelocityRPM) / 60.0
        inputs.current = sim.currentDrawAmps.absoluteValue
        inputs.outputPercent = outputPercent

        this.inputs = inputs
    }

    //if simP is set to 0.0 it will not change (in cases when you only want to change the "real p")
    override fun config_kP(p: Double, simP: Double?) { pid.p = if (simP == 0.0) pid.p else simP ?: p }
    override fun config_kI(i: Double, simI: Double?) { pid.i = if (simI == 0.0) pid.i else simI ?: i }
    override fun config_kD(d: Double, simD: Double?) { pid.d = if (simD == 0.0) pid.d else simD ?: d }

    override fun getPValue(): Double = pid.p
    override fun getIValue(): Double = pid.i
    override fun getDValue(): Double = pid.d

    override fun brakeMode() { breakMode = true }
    override fun coastMode() { breakMode = false}

    override fun closedLoopRamp(secondsToFull: Double) { closedLoopRamp = secondsToFull }
    override fun openLoopRamp(secondsToFull: Double) { openLoopRamp = secondsToFull }

    override fun getSelectedSensorPosition(): Double = inputs.position
    override fun getSelectedSensorVelocity(): Double = inputs.velocity
    override fun getSelectedSensorAcceleration(): Double = acceleration

    override fun getClosedLoopError(): Double = (positionSetpoint ?: 0.0) - getSelectedSensorPosition()

    override fun setSelectedSensorPosition(sensorPos: Double) = sim.setState(sensorPos.rotations.asRadians, getSelectedSensorVelocity())

    override fun restoreFactoryDefaults() = configSim(DCMotor.getKrakenX60Foc(1), 1.0)

    override fun stop() = setPercentOutput(0.0)

    override fun configSim(motor: DCMotor, jKgMetersSquared: Double) {
        this.motor = motor
        sim = DCMotorSim(this.motor, 1.0, jKgMetersSquared)
        sim.setState(0.0, 0.0)
    }

    override fun setPercentOutput(percent: Double) {
        outputPercent = percent.coerceIn(-1.0, 1.0)
        setVoltageOutput(outputPercent * 12.0)
    }

    override fun setPositionSetpoint(position: Double) {
        positionSetpoint = position
    }

    override fun setPositionSetpoint(position: Double, feedForward: Double) {
        positionSetpoint = position
        this.feedForward = feedForward
    }

    private fun setVoltageOutput(requestedVolts: Double) {
        var volts = requestedVolts.coerceIn(-12.0, 12.0)

        //ramp rate
        if (rampLimit != 0.0 && volts != 0.0) {
            val maxVoltDelta = (rampLimitTimer.get() / rampLimit * 12.0) * (volts / volts.absoluteValue)
            val voltsDelta = (volts - prevVolts)

            //if (delta and maxDelta are going the same direction && delta bigger than maxDelta)
            if (voltsDelta * maxVoltDelta >= 0.0 && maxVoltDelta.absoluteValue < voltsDelta.absoluteValue) {
//                if (inputs.name == "Drive/FLD") println("over accelerating max: $maxVoltDelta \t delta $voltsDelta")
                volts = prevVolts + maxVoltDelta
            }
//            if (inputs.name == "Drive/FLD") {
//                Logger.recordOutput("maxVoltsDelta", maxVoltDelta)
//                Logger.recordOutput("voltsDelta", voltsDelta)
//                Logger.recordOutput("applyRampRate", voltsDelta * maxVoltDelta > 0.0 && maxVoltDelta.absoluteValue < voltsDelta.absoluteValue)
//            }
            /* only applies when voltage is increasing acceleration */
        }

        //break mode
        if (breakMode && volts == 0.0) {
            volts = breakModeVolts
        }

//        if (inputs.name == "Drive/FLD") {
//            Logger.recordOutput("breakMode", breakModeVolts)
//            Logger.recordOutput("freeSpeed", motor.freeSpeedRadPerSec.radians.asRotations)
//            Logger.recordOutput("velocity", getSelectedSensorVelocity())
//            Logger.recordOutput("volts", volts)
//            Logger.recordOutput("requestedVolts", requestedVolts)
//        }

        sim.setInputVoltage(volts) //apply volts to sim
        prevVolts = volts
        rampLimitTimer.start()
    }

    //unsupported functions
    override fun setVelocitySetpoint(velocity: Double) {}
    override fun setVelocitySetpoint(velocity: Double, feedForward: Double) {}
    override fun currentLimit(continuousLimit: Int, peakLimit: Int, peakDuration: Int) {} //<- could implement this maybe?
    override fun follow(followerID: MotorControllerIO) {}
    override fun getInverted(): Boolean = false
    override fun setInverted(invert: Boolean) {}
}