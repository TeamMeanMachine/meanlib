package org.team2471.frc.lib.autonomous.test

import org.wpilib.command3.Command

/**
 * A class to store a test command to be built into a test OpMode.
 *
 * @param name The name of the test
 * @param command The command to run when the test OpMode runs
 * @param initFunction A function that runs when the test is selected. ALLOWS FOR SCOPING: Useful for overriding buttons or Mechanism defaults that only persist while the test is selected.
 *
 * @example TestRoutine("TestDrive", specialJoystickCommand(), { driveSubsystem.defaultCommand = Command.idle()})
 */
data class TestRoutine(val name: String, val command: Command, val initFunction: () -> Unit = {})
