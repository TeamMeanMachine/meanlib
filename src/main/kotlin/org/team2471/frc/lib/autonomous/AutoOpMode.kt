package org.team2471.frc.lib.autonomous

import org.wpilib.command3.Command
import org.wpilib.command3.Trigger
import org.wpilib.driverstation.RobotState
import org.wpilib.opmode.OpMode

/**
 * Autonomous [OpMode].
 *
 * When selected will call the [warmupFunction] provided by [Autonomi.warmupFunction]
 *
 * Then will periodically run the [disabledPeriodicFunction] until enabled
 *
 * Schedules [autoCommand] when enabled and runs it until its disabled or finished.
 */
class AutoOpMode(val name: String, autoCommand: Command, warmupFunction: () -> Unit, val disabledPeriodicFunction: () -> Unit): OpMode {

    init {
        println("$name auto created")
        warmupFunction()
        println("$name auto warmup complete.")
        // Binds a trigger to the enabled state of the robot.
        // Acts as if "enabled" is a joystick button. When true, it will schedule the command, when false, it will cancel it.
        // This Trigger also makes use of scoping. When the AutoOpMode is no longer selected, this trigger will go away.
        Trigger(RobotState::isEnabled).whileTrue(autoCommand)
    }

    override fun disabledPeriodic() {
        disabledPeriodicFunction()
    }
}