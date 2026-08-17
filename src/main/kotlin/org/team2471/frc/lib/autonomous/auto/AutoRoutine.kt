package org.team2471.frc.lib.autonomous.auto

import org.wpilib.command3.Command
import org.wpilib.math.geometry.Pose2d

/**
 * A class to store an autonomous command and data to be built into an [AutoOpMode].
 *
 * @param name The name of the auto
 * @param command The command to run when the [AutoOpMode] runs
 * @param startingPositionSupplier Start position of the robot. Continuously sets the starting position of the robot.
 * @param disabledPeriodicFunction A function that runs periodically when the auto is disabled. Useful for pre-setting things like swerve/motor setpoints.
 */
data class AutoRoutine(val name: String, val command: Command, val startingPositionSupplier: (() -> Pose2d)? = null, val disabledPeriodicFunction: (() -> Unit)? = null)
