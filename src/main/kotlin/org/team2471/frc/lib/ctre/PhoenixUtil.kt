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
import com.ctre.phoenix6.Utils
import com.ctre.phoenix6.Utils.getCurrentTimeSeconds
import com.ctre.phoenix6.controls.PositionTorqueCurrentFOC
import com.ctre.phoenix6.controls.PositionVoltage
import com.ctre.phoenix6.controls.VoltageOut
import com.ctre.phoenix6.swerve.SwerveDrivetrain.SwerveControlParameters
import com.ctre.phoenix6.swerve.SwerveModule
import com.ctre.phoenix6.swerve.SwerveModuleConstants
import com.ctre.phoenix6.swerve.SwerveRequest
import edu.wpi.first.math.kinematics.SwerveModuleState
import edu.wpi.first.wpilibj.DriverStation
import edu.wpi.first.wpilibj.Timer
import org.team2471.frc.lib.util.isSim
import java.util.function.Supplier

object PhoenixUtil {
    /** Attempts to run the command until no error is produced.  */
    fun tryUntilOk(maxAttempts: Int, command: Supplier<StatusCode>) {
        for (i in 0..<maxAttempts) {
            val error = command.get()
            if (error.isOK || isSim) break
            if (i == maxAttempts - 1) DriverStation.reportError("tryUntilOk() reached max attempts of $maxAttempts and failed with error: ${error.description}", true)
        }
    }

    /**
     * Converts a timestamp from the [Utils.getCurrentTimeSeconds] timebase
     * to the FPGA timebase reported by [Timer.getFPGATimestamp].
     *
     * @param currentTimeSeconds The timestamp in [Utils.getCurrentTimeSeconds] seconds
     * @return The equivalent [Timer.getFPGATimestamp] timestamp in seconds
     *
     * @see Utils.fpgaToCurrentTime
     */
    fun currentToFpgaTime(currentTimeSeconds: Double): Double {
        return (Timer.getFPGATimestamp() - getCurrentTimeSeconds()) + currentTimeSeconds
    }
}

/**
 * Swerve request to set the individual module states.
 *
 * If no value is passed in, the modules are set to their current angle with 0 speed
 */
class ApplyModuleStates(vararg val moduleStates: SwerveModuleState? = arrayOf()): SwerveRequest {
    override fun apply(
        parameters: SwerveControlParameters?,
        vararg modulesToApply: SwerveModule<*, *, *>
    ): StatusCode {
        modulesToApply.forEachIndexed { index, module ->
            val wantedState = moduleStates.getOrNull(index) ?: SwerveModuleState(0.0, module.currentState.angle)
            module.apply(SwerveModule.ModuleRequest().withState(wantedState))
        }

        return StatusCode.OK
    }
}

/**
 * Swerve request to set the individual module states. But, reads [SwerveModuleState.speedMetersPerSecond] as voltage NOT m/s
 *
 * If no value is passed in, the modules are set to their current angle with 0 volts
 */
class ApplyModuleStatesVoltage(vararg val moduleStates: SwerveModuleState? = arrayOf()): SwerveRequest {

    /** Local reference to a voltage request for the drive motors  */
    private val m_driveRequest = VoltageOut(0.0)

    /** Local reference to a position voltage request for the steer motors  */
    private val m_steerRequest_Voltage = PositionVoltage(0.0)

    /** Local reference to a position torque current request for the steer motors  */
    private val m_steerRequest_TorqueCurrent = PositionTorqueCurrentFOC(0.0)

    override fun apply(
        parameters: SwerveControlParameters?,
        vararg modulesToApply: SwerveModule<*, *, *>?
    ): StatusCode {
        modulesToApply.forEachIndexed { i, m ->
            val wantedState = moduleStates.getOrNull(i) ?: SwerveModuleState(0.0, m!!.currentState.angle)
            when (m!!.steerClosedLoopOutputType) {
                SwerveModuleConstants.ClosedLoopOutputType.Voltage -> m.apply(
                    m_driveRequest.withOutput(wantedState.speedMetersPerSecond),
                    m_steerRequest_Voltage.withPosition(wantedState.angle.measure)
                )

                SwerveModuleConstants.ClosedLoopOutputType.TorqueCurrentFOC -> m.apply(
                    m_driveRequest.withOutput(wantedState.speedMetersPerSecond),
                    m_steerRequest_TorqueCurrent.withPosition(wantedState.angle.measure)
                )
            }
        }
        return StatusCode.OK
    }
}
