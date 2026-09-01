package org.team2471.frc.lib.hardware.loggedMotors

import com.ctre.phoenix6.BaseStatusSignal
import com.ctre.phoenix6.CANBus
import com.ctre.phoenix6.hardware.TalonFX
import edu.wpi.first.math.system.plant.DCMotor
import edu.wpi.first.math.system.plant.LinearSystemId
import edu.wpi.first.wpilibj.simulation.DCMotorSim
import org.team2471.frc.lib.units.volts
import org.littletonrobotics.junction.Logger
import org.team2471.frc.lib.util.isReplay
import org.team2471.frc.lib.util.isSim

/**
 * Wrapper for [TalonFX] that supports replay and simulation when [configSim] is called.
 *
 * @param id The CAN ID of the motor.
 * @param canBus The CAN bus to use. Defaults to roboRIO or if null.
 *
 * @see TalonFX
 * @see DCMotorSim
 */
class LoggedTalonFX(id: Int, canBus: CANBus = CANBus()): TalonFX(id, canBus), LoggedMotor {
    private var motor: DCMotor? = null
    private var motorPhysicsSim: DCMotorSim? = null

    val loggedInputs = MotorInputsAutoLogged()

    private val positionStatusSignal = position
    private val velocityStatusSignal = velocity
    private val accelerationStatusSignal = acceleration
    private val supplyVoltageStatusSignal = supplyVoltage
    private val loggingStatusSignals = listOf(positionStatusSignal, velocityStatusSignal, accelerationStatusSignal, supplyVoltageStatusSignal)

    init {
        MasterMotor.addMotor(this)
    }

    override fun configSim(motor: DCMotor, jKgMetersSquared: Double) {
        if (isSim) {
            this.motor = motor
            motorPhysicsSim = DCMotorSim(LinearSystemId.createDCMotorSystem(this.motor, jKgMetersSquared, 1.0), this.motor).apply {
                setState(0.0, 0.0)
            }
        }
    }

    override fun periodic() {
        if (isReplay) {
            // Apply replayed motor values
            simState.setRawRotorPosition(loggedInputs.angularPosition)
            simState.setRotorVelocity(loggedInputs.angularVelocity)
            simState.setRotorAcceleration(loggedInputs.angularAcceleration)
            simState.setSupplyVoltage(loggedInputs.supplyVoltage)
        } else {
            // If in simulation, simulate the motor values
            if (motorPhysicsSim != null) {
                // Update motor sim
                motorPhysicsSim!!.inputVoltage = simState.motorVoltage
                motorPhysicsSim!!.update(0.02)
                // Apply to talonFXSim
                simState.setRawRotorPosition(motorPhysicsSim!!.angularPosition)
                simState.setRotorVelocity(motorPhysicsSim!!.angularVelocity)
                simState.setRotorAcceleration(motorPhysicsSim!!.angularAcceleration)
                simState.setSupplyVoltage(12.0.volts) // We have no battery sim
            }

            // Log motor outputs
            BaseStatusSignal.refreshAll(loggingStatusSignals)
            loggedInputs.angularPosition = positionStatusSignal.value
            loggedInputs.angularVelocity = velocityStatusSignal.value
            loggedInputs.angularAcceleration = accelerationStatusSignal.value
            loggedInputs.supplyVoltage = supplyVoltageStatusSignal.value
        }
        Logger.processInputs("Motors/TalonFX $deviceID ${network.name}", loggedInputs)
    }
}