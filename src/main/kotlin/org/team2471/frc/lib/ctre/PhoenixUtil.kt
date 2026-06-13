// Copyright 2021-2025 FRC 6328
// http://github.com/Mechanical-Advantage
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// version 3 as published by the Free Software Foundation or
// available in the root directory of this project.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
package org.team2471.frc.lib.ctre

import com.ctre.phoenix6.StatusCode
import com.ctre.phoenix6.controls.PositionTorqueCurrentFOC
import com.ctre.phoenix6.controls.PositionVoltage
import com.ctre.phoenix6.controls.VoltageOut
import com.ctre.phoenix6.signals.ControlModeValue
import com.ctre.phoenix6.swerve.SwerveDrivetrain.SwerveControlParameters
import com.ctre.phoenix6.swerve.SwerveModule
import com.ctre.phoenix6.swerve.SwerveModuleConstants
import com.ctre.phoenix6.swerve.SwerveRequest
import org.team2471.frc.lib.util.isSim
import org.wpilib.driverstation.DriverStationErrors
import org.wpilib.math.kinematics.SwerveModuleVelocity
import java.util.function.Supplier

object PhoenixUtil {

    val positionControlModes = listOf(
        ControlModeValue.PositionVoltage, ControlModeValue.PositionVoltageFOC,
        ControlModeValue.PositionTorqueCurrentFOC, ControlModeValue.MotionMagicDutyCycle,
        ControlModeValue.MotionMagicDutyCycleFOC, ControlModeValue.MotionMagicVoltage,
        ControlModeValue.MotionMagicVoltageFOC, ControlModeValue.PositionDutyCycle,
        ControlModeValue.PositionDutyCycleFOC, ControlModeValue.MotionMagicExpoDutyCycle,
        ControlModeValue.MotionMagicExpoDutyCycleFOC, ControlModeValue.MotionMagicExpoVoltage,
        ControlModeValue.MotionMagicExpoVoltageFOC, ControlModeValue.MotionMagicExpoTorqueCurrentFOC,
    )

    /** Determines if the control mode controls the position of the motor. As used in this list: [positionControlModes]*/
    fun ControlModeValue.isPositionControlMode() = positionControlModes.contains(this)

    /** Attempts to run the command until no error is produced.  */
    fun tryUntilOk(maxAttempts: Int, command: Supplier<StatusCode>): Boolean {
        for (i in 0..<maxAttempts) {
            val error = command.get()
            if (error.isOK || isSim) break
            if (i == maxAttempts - 1) {
                DriverStationErrors.reportError("tryUntilOk() reached max attempts of $maxAttempts and failed with error: ${error.description}", true)
                return false
            }
        }
        return true
    }
}

/**
 * Swerve request to set the individual module states.
 *
 * If no value is passed in, the modules are set to their current angle with 0 speed
 */
class ApplyModuleStates(vararg val moduleStates: SwerveModuleVelocity? = arrayOf()): SwerveRequest {
    override fun apply(
        parameters: SwerveControlParameters?,
        vararg modulesToApply: SwerveModule<*, *, *>
    ): StatusCode {
        modulesToApply.forEachIndexed { index, module ->
            val wantedState = moduleStates.getOrNull(index) ?: SwerveModuleVelocity(0.0, module.currentVelocity.angle)
            module.apply(SwerveModule.ModuleRequest().withVelocity(wantedState))
        }

        return StatusCode.OK
    }
}

/**
 * Swerve request to set the individual module states. But, reads [SwerveModuleVelocity.velocity] as voltage NOT m/s
 *
 * If no value is passed in, the modules are set to their current angle with 0 volts
 */
class ApplyModuleStatesVoltage(vararg val moduleStates: SwerveModuleVelocity? = arrayOf()): SwerveRequest {

    /** Local reference to a voltage request for the drive motors  */
    private val driveRequest = VoltageOut(0.0)

    /** Local reference to a position voltage request for the steer motors  */
    private val steerRequestVoltage = PositionVoltage(0.0)

    /** Local reference to a position torque current request for the steer motors  */
    private val steerRequestTorqueCurrent = PositionTorqueCurrentFOC(0.0)

    override fun apply(
        parameters: SwerveControlParameters?,
        vararg modulesToApply: SwerveModule<*, *, *>?
    ): StatusCode {
        modulesToApply.forEachIndexed { i, m ->
            val wantedState = moduleStates.getOrNull(i) ?: SwerveModuleVelocity(0.0, m!!.currentVelocity.angle)
            when (m!!.steerClosedLoopOutputType) {
                SwerveModuleConstants.ClosedLoopOutputType.Voltage -> m.apply(
                    driveRequest.withOutput(wantedState.velocity),
                    steerRequestVoltage.withPosition(wantedState.angle.measure)
                )

                SwerveModuleConstants.ClosedLoopOutputType.TorqueCurrentFOC -> m.apply(
                    driveRequest.withOutput(wantedState.velocity),
                    steerRequestTorqueCurrent.withPosition(wantedState.angle.measure)
                )
            }
        }
        return StatusCode.OK
    }
}
