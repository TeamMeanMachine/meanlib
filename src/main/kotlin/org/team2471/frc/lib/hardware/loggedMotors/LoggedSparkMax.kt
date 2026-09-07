package org.team2471.frc.lib.hardware.loggedMotors

import com.revrobotics.sim.SparkMaxSim
import com.revrobotics.spark.SparkMax
import org.littletonrobotics.junction.Logger
import org.team2471.frc.lib.environment.isReplay
import org.team2471.frc.lib.environment.isSim
import org.team2471.frc.lib.units.asRPM
import org.team2471.frc.lib.units.asRotations
import org.team2471.frc.lib.units.asVolts
import org.team2471.frc.lib.units.perSecond
import org.team2471.frc.lib.units.rotations
import org.team2471.frc.lib.units.rpm
import org.team2471.frc.lib.units.volts
import org.wpilib.math.system.DCMotor
import org.wpilib.math.system.Models
import org.wpilib.simulation.DCMotorSim

/**
 * Wrapper for [SparkMax] that supports replay and simulation when [configSim] is called.
 *
 * @param id The CAN ID of the motor.
 * @param type The motor type connected to the controller.
 * Brushless motor wires must be connected to their matching colors, and the hall sensor must be plugged in.
 * Brushed motors must be connected to the Red and Black terminals only.
 *
 * @see SparkMax
 * @see DCMotorSim
 */
class LoggedSparkMax(id: Int, canBus: Int, type: MotorType): SparkMax(canBus, id, type), LoggedMotor {
    private var motor: DCMotor? = null
    private var motorPhysicsSim: DCMotorSim? = null

    private var sparkMaxSim = SparkMaxSim(this, motor ?: DCMotor.getNEO(1))

    val loggedInputs = MotorInputsAutoLogged()

    init {
        MasterMotor.addMotor(this)
    }

    override fun configSim(motor: DCMotor, jKgMetersSquared: Double) {
        if (isSim) {
            this.motor = motor
            motorPhysicsSim = DCMotorSim(Models.singleJointedArmFromPhysicalConstants(this.motor, jKgMetersSquared, 1.0), this.motor).apply {
                setState(0.0, 0.0)
            }
            sparkMaxSim = SparkMaxSim(this, motor)
        }
    }

    override fun periodic() {
        if (isReplay) {
            // Apply replayed motor values
            sparkMaxSim.position = loggedInputs.angularPosition.asRotations
            sparkMaxSim.velocity = loggedInputs.angularVelocity.asRPM
            sparkMaxSim.busVoltage = loggedInputs.supplyVoltage.asVolts
        } else {
            // If in simulation, simulate the motor values
            if (motorPhysicsSim != null) {
                // Update motor sim
                motorPhysicsSim!!.inputVoltage = sparkMaxSim.appliedOutput * sparkMaxSim.busVoltage
                motorPhysicsSim!!.update(0.02)

                sparkMaxSim.iterate(motorPhysicsSim!!.angularVelocity, 12.0, 0.02)
            }

            // Log motor outputs
            loggedInputs.angularPosition = encoder.position.get().rotations
            val currentAngularVelocity = encoder.velocity.get().rpm
            loggedInputs.angularAcceleration = ((currentAngularVelocity - loggedInputs.angularVelocity) / 0.02).perSecond
            loggedInputs.angularVelocity = currentAngularVelocity
            loggedInputs.supplyVoltage = this.busVoltage.get().volts
        }
        Logger.processInputs("Motors/SparkMax $deviceId", loggedInputs)
    }
}