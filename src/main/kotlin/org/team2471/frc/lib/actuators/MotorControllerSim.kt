package org.team2471.frc.lib.actuators

import com.ctre.phoenix6.signals.NeutralModeValue
import edu.wpi.first.math.controller.PIDController
import edu.wpi.first.math.system.plant.DCMotor
import edu.wpi.first.wpilibj.simulation.DCMotorSim
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.units.*
import kotlin.math.absoluteValue

@OptIn(DelicateCoroutinesApi::class)
class MotorControllerSim: MotorControllerIO {
    //sim
    private lateinit var sim: DCMotorSim

    //motor outputs
    private var inputs: MotorControllerIO.MotorControllerIOInputs = MotorControllerIO.MotorControllerIOInputs("null")
    override var outputPercent: Double = 0.0
    override val current: Double get() = inputs.current

    //control
    private val pid = PIDController(0.0, 0.0, 0.0)
    private var positionSetpoint: Double? = null
    private var feedForward: Double = 0.0
    private var inverted = false //unused for now


    init {
        restoreFactoryDefaults()

        GlobalScope.launch {
            periodic {
                if (positionSetpoint != null) { //do closed loop control
//                    if (inputs.name == "Drive/FLS") println("error ${getClosedLoopError().round(1)} \t setpoint ${positionSetpoint!!.round(1)} \t angle ${getSelectedSensorPosition().round(1)}")

                    setPercentOutput(
                        pid.calculate(getSelectedSensorPosition(), positionSetpoint ?: getSelectedSensorPosition()) + feedForward
                    )
                }
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
    override fun config_kD(d: Double, simD: Double?) { pid.d = if (simD == 0.0) pid.d else simD ?: d }
    override fun config_kI(i: Double, simI: Double?) { pid.i = if (simI == 0.0) pid.i else simI ?: i }

    override fun getPValue(): Double = pid.p
    override fun getDValue(): Double = pid.d
    override fun getIValue(): Double = pid.i

    override fun getClosedLoopError(): Double = (positionSetpoint ?: 0.0) - getSelectedSensorPosition()

    override fun getInverted(): Boolean = inverted

    override fun getSelectedSensorPosition(): Double = inputs.position

    override fun getSelectedSensorVelocity(): Double = inputs.velocity

    override fun restoreFactoryDefaults() = configSim(DCMotor.getKrakenX60Foc(1), 1.0)

    override fun setSelectedSensorPosition(sensorPos: Double) = sim.setState(sensorPos.rotations.asRadians, getSelectedSensorVelocity())

    override fun stop() = setPercentOutput(0.0)

    override fun setInverted(invert: Boolean) {
        inverted = invert
    }

    override fun configSim(motor: DCMotor, jKgMetersSquared: Double) {
        println("creating new sim. name: ${inputs.name}  MOI: $jKgMetersSquared")
        sim = DCMotorSim(motor, 1.0, jKgMetersSquared)
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

    private fun setVoltageOutput(volts: Double) {
        sim.setInputVoltage(volts.coerceIn(-12.0, 12.0))
    }

    //unsupported functions
    override fun setVelocitySetpoint(velocity: Double) {}
    override fun setVelocitySetpoint(velocity: Double, feedForward: Double) {}
    override fun brakeMode() {}
    override fun closedLoopRamp(secondsToFull: Double) {} //<- could implement this
    override fun coastMode() {}
    override fun currentLimit(continuousLimit: Int, peakLimit: Int, peakDuration: Int) {} //<- could implement this maybe?
    override fun openLoopRamp(secondsToFull: Double) {} //<- could implement this?
    override fun setNeutralMode(neutralMode: NeutralModeValue?) {}
    override fun follow(followerID: MotorControllerIO) {}
}