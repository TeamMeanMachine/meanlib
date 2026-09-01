package org.team2471.frc.lib.hardware.loggedMotors

import com.revrobotics.sim.SparkMaxSim
import com.revrobotics.spark.SparkMax
import edu.wpi.first.math.system.plant.DCMotor
import edu.wpi.first.math.system.plant.LinearSystemId
import edu.wpi.first.wpilibj.simulation.DCMotorSim
import org.littletonrobotics.junction.Logger
import org.team2471.frc.lib.units.asRPM
import org.team2471.frc.lib.units.asRotations
import org.team2471.frc.lib.units.asVolts
import org.team2471.frc.lib.units.perSecond
import org.team2471.frc.lib.units.rotations
import org.team2471.frc.lib.units.rpm
import org.team2471.frc.lib.units.volts
import org.team2471.frc.lib.util.isReplay
import org.team2471.frc.lib.util.isSim

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
class LoggedSparkMax(id: Int, type: MotorType): SparkMax(id, type), LoggedMotor {
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
            motorPhysicsSim = DCMotorSim(LinearSystemId.createDCMotorSystem(this.motor, jKgMetersSquared, 1.0), this.motor).apply {
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

                sparkMaxSim.iterate(motorPhysicsSim!!.angularVelocity.asRPM, 12.0, 0.02)
            }

            // Log motor outputs
            loggedInputs.angularPosition = encoder.position.rotations
            val currentAngularVelocity = encoder.velocity.rpm
            loggedInputs.angularAcceleration = ((currentAngularVelocity - loggedInputs.angularVelocity) / 0.02).perSecond
            loggedInputs.angularVelocity = currentAngularVelocity
            loggedInputs.supplyVoltage = this.busVoltage.volts
        }
        Logger.processInputs("Motors/SparkMax $deviceId", loggedInputs)
    }
}