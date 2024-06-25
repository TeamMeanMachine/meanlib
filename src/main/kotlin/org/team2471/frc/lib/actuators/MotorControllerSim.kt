package org.team2471.frc.lib.actuators

import com.ctre.phoenix6.signals.NeutralModeValue
import edu.wpi.first.math.controller.PIDController
import edu.wpi.first.math.system.plant.DCMotor
import edu.wpi.first.wpilibj.simulation.DCMotorSim
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.math.round
import org.team2471.frc.lib.units.*
import org.team2471.frc.lib.util.Timer
import kotlin.math.absoluteValue

@OptIn(DelicateCoroutinesApi::class)
class MotorControllerSim: MotorControllerIO {
    //sim
    private lateinit var sim: DCMotorSim
    private val pid = PIDController(0.0, 0.0, 0.0)
    private var motorType = DCMotor.getKrakenX60Foc(1)
    private var jKgMetersSquared = 1.0
    private var gearRatio: Double = 1.0
        set(value) {
            field = value
            constructSim()
        }

    //control
    private var positionSetpoint: Double? = null
    private var feedForward: Double = 0.0
    private var inverted = false //unused for now

    //motor outputs
    override var outputPercent: Double = 0.0
    override val current: Double get() = inputs.current
    private var inputs: MotorControllerIO.MotorControllerIOInputs = MotorControllerIO.MotorControllerIOInputs("null")

    val testSim = DCMotorSim(DCMotor.getNeoVortex(1), 6.12, 0.025)



    init {
        constructSim()

        //simulation loop
        GlobalScope.launch {
            val t = Timer()
            t.start()
            var lastTime = t.get()
            periodic(0.02) {
//                if (inputs.name == "Drive/BLD") {
//                    println(feedbackCoefficient)
//                }
//                if (positionSetpoint != null) {
//                    setVoltageOutput(pid.calculate(inputs.position, positionSetpoint!!) + feedForward)
//                }
//                sim.update(0.02)
//
//                println("${inputs.name} time: ${t.get() - lastTime}")

                lastTime = t.get()
            }
        }
    }




    override fun updateInputs(inputs: MotorControllerIO.MotorControllerIOInputs) {
        sim.update(0.02)
        testSim.update(0.02)

        inputs.position = (sim.angularPositionRad)// * gearRatio).radians.asRotations
        inputs.outputPercent = outputPercent
        inputs.velocity = (sim.angularVelocityRadPerSec)// * gearRatio).radians.asRotations
        inputs.current = sim.currentDrawAmps.absoluteValue

        this.inputs = inputs


        if (inputs.name == "Drive/BLD") println("position: ${testSim.angularPositionRad.radians.asRotations}  velocity: ${testSim.angularVelocityRadPerSec.radians.asRotations}  current: ${testSim.currentDrawAmps}")
    }

    //if simP is set to 0.0 it will not change (in cases when you only want to change the "real p")
    override fun config_kP(p: Double, simP: Double?) { pid.p = if (simP == 0.0) pid.p else simP ?: p }
    override fun config_kD(d: Double, simD: Double?) { pid.d = if (simD == 0.0) pid.d else simD ?: d }
    override fun config_kI(i: Double, simI: Double?) { pid.i = if (simI == 0.0) pid.i else simI ?: i }

    override fun getPValue(): Double = pid.p
    override fun getDValue(): Double = pid.d
    override fun getIValue(): Double = pid.i

    override fun getClosedLoopError(): Double = inputs.position - (positionSetpoint ?: 0.0)

    override fun getInverted(): Boolean = inverted

    override fun getSelectedSensorPosition(): Double = inputs.position

    override fun getSelectedSensorVelocity(): Double = inputs.velocity

    override fun setSelectedSensorPosition(sensorPos: Double) = sim.setState((sensorPos / gearRatio).rotations.asRadians, getSelectedSensorVelocity())


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
        this.gearRatio = feedbackCoefficient
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
        println("creating new sim. name: ${inputs.name}  feedbackCoefficient: $gearRatio  MOI: $jKgMetersSquared")
        sim = DCMotorSim(motorType, gearRatio, jKgMetersSquared)
        sim.setState(0.0, 0.0)
    }

    private fun setVoltageOutput(volts: Double) {
        sim.setInputVoltage(volts.coerceIn(-12.0, 12.0))

        testSim.setInputVoltage(volts.coerceIn(-12.0, 12.0))
    }



    //unsupported functions
    override fun brakeMode() {}
    override fun closedLoopRamp(secondsToFull: Double) {} //<- could implement this
    override fun coastMode() {}
    override fun currentLimit(continuousLimit: Int, peakLimit: Int, peakDuration: Int) {} //<- could implement this maybe?
    override fun openLoopRamp(secondsToFull: Double) {} //<- could implement this?
    override fun setNeutralMode(neutralMode: NeutralModeValue?) {}
    override fun follow(followerID: MotorControllerIO) {}
}