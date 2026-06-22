package org.team2471.frc.lib.ctre.loggedMotors

import edu.wpi.first.math.system.plant.DCMotor

interface LoggedMotor {
    /**
     * Configure the simulation to have accurate values.
     * @param motor The type of [DCMotor] motor to sim.
     * @param jKgMetersSquared The moment of inertia of the motor.
     * @see DCMotor
     */
    fun configSim(motor: DCMotor, jKgMetersSquared: Double)
    fun periodic()
}