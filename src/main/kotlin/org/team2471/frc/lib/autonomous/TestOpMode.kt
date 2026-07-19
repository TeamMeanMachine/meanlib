package org.team2471.frc.lib.autonomous

import org.wpilib.command3.Command
import org.wpilib.command3.Trigger
import org.wpilib.driverstation.RobotState
import org.wpilib.opmode.OpMode

/**
 * Utility/Test [OpMode].
 *
 * When selected will call the [initFunction] in the OpMode's scope.
 *
 * Schedules [testCommand] when enabled and runs it until its disabled or finished.
 */
class TestOpMode(val name: String, testCommand: Command, initFunction: () -> Unit): OpMode {
    init {
        println("$name test created")
        initFunction()
        // Binds a trigger to the enabled state of the robot.
        // Acts as if "enabled" is a joystick button. When true, it will schedule the command, when false, it will cancel it.
        // This Trigger also makes use of scoping. When the TestOpMode is no longer selected, this trigger will go away.
        Trigger(RobotState::isEnabled).whileTrue(testCommand)
    }
}