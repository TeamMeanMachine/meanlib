package org.team2471.frc.lib.actuators

import com.ctre.phoenix6.signals.NeutralModeValue
import edu.wpi.first.math.controller.PIDController
import edu.wpi.first.math.system.plant.DCMotor
import edu.wpi.first.wpilibj.simulation.DCMotorSim
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.math.round
import org.team2471.frc.lib.units.*
import kotlin.math.absoluteValue

class MotorControllerSim: MotorControllerIO {
    //sim
    private lateinit var sim: DCMotorSim
    private val pid = PIDController(0.0, 0.0, 0.0)
    private var motorType = DCMotor.getKrakenX60Foc(1)
    private var jKgMetersSquared = 1.0
    private var feedbackCoefficient: Double = 1.0
        set(value) {
            field = value
            constructSim()
        }

    //control
    private var positionSetpoint: Angle? = null
    private var feedForward: Double = 0.0
    private var inverted = false //unused for now

    //motor outputs
    override var outputPercent: Double = 0.0
    override val current: Double get() = inputs.current
    private var inputs: MotorControllerIO.MotorControllerIOInputs = MotorControllerIO.MotorControllerIOInputs("null")



    init {
        constructSim()

        //simulation loop
        GlobalScope.launch {
            periodic(0.02) {
                if (positionSetpoint != null) {
                    setPercentOutput(pid.calculate(inputs.position.asDegrees, positionSetpoint!!.asDegrees) + feedForward)
                }
                sim.update(0.02)
            }
        }
    }




    override fun updateInputs(inputs: MotorControllerIO.MotorControllerIOInputs) {
        inputs.position = (sim.angularPositionRad.radians.asDegrees / feedbackCoefficient).degrees
        inputs.outputPercent = outputPercent
        inputs.velocity = (sim.angularVelocityRadPerSec.radians.asDegrees / feedbackCoefficient).rotations.perMinute
        inputs.current = sim.currentDrawAmps.absoluteValue

        this.inputs = inputs
    }

    override fun config_kP(p: Double) { pid.p = p }
    override fun config_kD(d: Double) { pid.d = d }
    override fun config_kI(i: Double) { pid.i = i }

    override fun getPValue(): Double = pid.p
    override fun getDValue(): Double = pid.d
    override fun getIValue(): Double = pid.i

    override fun getClosedLoopError(): Double = (inputs.position - (positionSetpoint ?: inputs.position)).asDegrees

    override fun getInverted(): Boolean = inverted

    override fun getSelectedSensorPosition(): Double = inputs.position.asDegrees

    override fun getSelectedSensorVelocity(): Double = inputs.velocity.changePerSecond.asDegrees

    override fun setSelectedSensorPosition(sensorPos: Double) = sim.setState((sensorPos * feedbackCoefficient).rotations.asRadians, 0.0)


    override fun setInverted(invert: Boolean) {
        inverted = invert
    }

    override fun setSimMotor(motor: DCMotor) {
        motorType = motor
        constructSim()
    }

    override fun setSimMOI(jKgMetersSquared: Double) {
        this.jKgMetersSquared = jKgMetersSquared
        constructSim()
    }

    override fun setSimFeedbackCoefficient(feedbackCoefficient: Double) {
        this.feedbackCoefficient = feedbackCoefficient
    }

    override fun setPercentOutput(percent: Double) {
        outputPercent = percent.coerceIn(-1.0, 1.0)
        sim.setInputVoltage(outputPercent * 12.0)
    }

    override fun setPositionSetpoint(position: Double) {
        positionSetpoint = position.degrees * feedbackCoefficient
    }

    override fun setPositionSetpoint(position: Double, feedForward: Double) {
        positionSetpoint = position.degrees * feedbackCoefficient
        this.feedForward = feedForward
    }


    override fun setVelocitySetpoint(velocity: Double) {
        TODO("Not yet implemented")
    }

    override fun setVelocitySetpoint(velocity: Double, feedForward: Double) {
        TODO("Not yet implemented")
    }

    override fun restoreFactoryDefaults() { constructSim() }

    override fun stop() {
        setPercentOutput(0.0)
    }

    private fun constructSim() {
        println("creating new sim. motor: $motorType  feedbackCoefficient: ${feedbackCoefficient.round(6)}  MOI: $jKgMetersSquared")
        sim = DCMotorSim(motorType, feedbackCoefficient, jKgMetersSquared)
    }



    //unsupported functions
    override fun brakeMode() {}
    override fun closedLoopRamp(secondsToFull: Double) {}
    override fun coastMode() {}
    override fun currentLimit(continuousLimit: Int, peakLimit: Int, peakDuration: Int) {}
    override fun openLoopRamp(secondsToFull: Double) {}
    override fun setNeutralMode(neutralMode: NeutralModeValue?) {}
    override fun follow(followerID: MotorControllerIO) {}
}