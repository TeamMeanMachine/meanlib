package org.team2471.frc.lib.ctre.loggedTalonFX

import com.ctre.phoenix6.CANBus
import com.ctre.phoenix6.hardware.TalonFXS
import com.ctre.phoenix6.signals.NeutralModeValue
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.team2471.frc.lib.environment.isReal
import org.wpilib.math.system.DCMotor
import org.wpilib.math.system.Models
import org.wpilib.simulation.DCMotorSim

/**
 * Wrapper for that [TalonFXS] class that supports simulation when [configSim] is called.
 * Also supports backing safe calls when calling [brakeMode] & [coastMode]
 *
 * @param id The CAN ID of the motor.
 * @param canBus The CAN bus to use. Defaults to roboRIO or if null.
 *
 * @see TalonFXS
 * @see DCMotorSim
 */
class LoggedTalonFXS(id: Int, canBus: CANBus = CANBus()): TalonFXS(id, canBus), LoggedMotor {
    private val talonFXSSim = this.simState
    private var motor: DCMotor? = null
    private var motorSim: DCMotorSim? = null

    private var addedToMaster = false


    init {
        talonFXSSim.setSupplyVoltage(12.0)
    }

    /**
     * Configure the simulation to have accurate values.
     * @param motor The type of [DCMotor] motor to sim.
     * @param jKgMetersSquared The moment of inertia of the motor.
     * @see DCMotor
     */
    fun configSim(motor: DCMotor, jKgMetersSquared: Double) {
        this.motor = motor
        motorSim = DCMotorSim(Models.singleJointedArmFromPhysicalConstants(this.motor, jKgMetersSquared, 1.0), this.motor)
        motorSim?.setState(0.0, 0.0)

        //Ensures this gets added to the MasterMotor list only once
        if (!addedToMaster) {
            addedToMaster = true
            MasterMotor.addMotor(this)
        }
    }

    /**
     * A backing safe call to set the brake mode of the motor.
     * This function will finish instantly, but the motor will take longer (>100 ms) to apply the change.
     * @see configNeutralMode
     * @see GlobalScope
     */
    @OptIn(DelicateCoroutinesApi::class)
    fun brakeMode() {
        if (isReal) {
            GlobalScope.launch {
                configNeutralMode(NeutralModeValue.Brake)
            }
        }
    }

    /**
     * A backing safe call to set the coast mode of the motor.
     * This function will finish instantly, but the motor will take longer (>100 ms) to apply the change.
     * @see configNeutralMode
     * @see GlobalScope
     */
    @OptIn(DelicateCoroutinesApi::class)
    fun coastMode() {
        if (isReal) {
            GlobalScope.launch {
                configNeutralMode(NeutralModeValue.Coast)
            }
        }
    }

    override fun simPeriodic() {
        if (motorSim != null) {
            val talonFXVoltage = talonFXSSim.motorVoltage

            motorSim!!.inputVoltage = talonFXVoltage
            motorSim!!.update(0.02)

            talonFXSSim.setRawRotorPosition(motorSim!!.angularPosition)
            talonFXSSim.setRotorVelocity(motorSim!!.angularVelocity)
            talonFXSSim.setRotorAcceleration(motorSim!!.angularAcceleration)
        }
    }
}