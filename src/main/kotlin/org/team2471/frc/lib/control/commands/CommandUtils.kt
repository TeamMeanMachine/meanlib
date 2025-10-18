package org.team2471.frc.lib.control.commands

import edu.wpi.first.units.measure.Time
import edu.wpi.first.wpilibj2.command.Command
import edu.wpi.first.wpilibj2.command.Commands
import edu.wpi.first.wpilibj2.command.DeferredCommand
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup
import edu.wpi.first.wpilibj2.command.Subsystem
import edu.wpi.first.wpilibj2.command.WrapperCommand

/**
 * Turns the function into a runOnce Command
 *
 * @see Commands.runOnce
 */
fun (() -> Unit).toCommand(vararg requirements: Subsystem): Command = runOnceCommand(*requirements) { this() }

fun printlnCommand(message: String) = Commands.print(message)
/**
 * Decorates this command with a lambda to call on interrupt or end, following the command's inherent end(boolean) method.
 * @param run A lambda (function) accepting a boolean parameter specifying whether the command was interrupted.
 * @return: the decorated command
 */
fun Command.finallyRun(run: (Boolean) -> Unit): WrapperCommand =
    this.finallyDo { interrupted -> run(interrupted)}

/**
 * Decorates this command to schedule another command on interrupt or end, following the command's inherent end(boolean) method.
 * @param command A command to run after the main command has been interrupted or finished
 * @return: the decorated command
 */
fun Command.finallyRun(command: Command): Command = this.finallyRun { command.schedule() }

/**
 * After the initial command finishes, wait [seconds] more than finish.
 * @param seconds The amount of time to wait after the initial command has finished
 * @see Command.andThen
 * @see Commands.waitSeconds
 */
fun Command.finallyWait(seconds: Double) = this.andThen(waitCommand(seconds))!!

/**
 * Only schedule and continue running the command if [condition] is true.
 *
 * Combines onlyIf and onlyWhile and will only start the command if the [condition] is true
 * and will stop the command when the [condition] returns false.
 *
 * @param condition a lambda (function) that should return true if the function should continue running.
 * @see Command.onlyIf
 * @see Command.onlyWhile
 * @see onlyRunWhileFalse
 */
fun Command.onlyRunWhileTrue(condition: () -> Boolean) = this.onlyWhile(condition).onlyIf(condition)!!

/**
 * Only schedule and continue running the command if [condition] is false.
 *
 * Combines until and unless and will only start the command if the [condition] is false
 * and will stop the command when the [condition] returns true.
 *
 * @param condition a lambda (function) that should return true if the function should stop.
 * @see Command.unless
 * @see Command.until
 * @see onlyRunWhileTrue
 */
fun Command.onlyRunWhileFalse(condition: () -> Boolean) = this.until(condition).unless(condition)!!

/**
 * Before the initial command starts, wait [seconds] more than start.
 * @param seconds The amount of time to wait before the initial command starts
 * @see Command.beforeStarting
 * @see Commands.waitSeconds
 */
fun Command.beforeWait(seconds: Double) = this.beforeStarting(waitCommand(seconds))!!

/**
 * Before the initial command starts, wait until [condition] returns true.
 * @param condition A lambda (function) returning if the command should start
 * @see Command.beforeStarting
 * @see Commands.waitUntil
 */
fun Command.beforeWaitUntil(condition: () -> Boolean) = this.beforeStarting(waitUntilCommand(condition))!!

/**
 * Before the command starts, run this [action] first.
 * @param action the action to run
 * @param requirements subsystems the action requires
 * @return the command
 * @see Command.beforeStarting
 */
fun Command.beforeRun(vararg requirements: Subsystem, action: () -> Unit): SequentialCommandGroup =
    this.beforeStarting(action, *requirements)


/**
 * Constructs a command that runs an action once and finishes.
 * @param action the action to run
 * @param requirements subsystems the action requires
 * @return the command
 * @see edu.wpi.first.wpilibj2.command.InstantCommand
 */
fun runOnceCommand(vararg requirements: Subsystem, action: () -> Unit): Command = Commands.runOnce(action, *requirements)

/**
 * Constructs a command that runs an action once and finishes.
 * @param action the action to run
 * @param requirements subsystems the action requires
 * @return the command
 * @see edu.wpi.first.wpilibj2.command.InstantCommand
 */
fun runOnce(vararg requirements: Subsystem, action: () -> Unit): Command = runOnceCommand(*requirements, action = action)

/**
 * Constructs a command that runs an action every iteration until interrupted.
 * @param action the action to run
 * @param requirements subsystems the action requires
 * @return the command
 * @see edu.wpi.first.wpilibj2.command.RunCommand
 */
fun runCommand(vararg requirements: Subsystem, action: () -> Unit): Command = Commands.run(action, *requirements)

/**
 * Runs a group of commands in series, one after the other.
 * @param commands the commands to include
 * @return the command group
 * @see SequentialCommandGroup
 */
fun sequenceCommand(vararg commands: Command): Command = Commands.sequence(*commands)

/**
 * Runs a group of commands in series, one after the other. Once the last command ends, the group is restarted.
 * @param commands the commands to include
 * @return the command group
 * @see SequentialCommandGroup,
 * @see Command.repeatedly()
 */
fun repeatingSequenceCommand(vararg commands: Command): Command = Commands.repeatingSequence(*commands)

/**
 * Runs a group of commands at the same time. Ends once all commands in the group finish.
 * @param commands the commands to include
 * @return the command
 * @see edu.wpi.first.wpilibj2.command.ParallelCommandGroup
 */
fun parallelCommand(vararg commands: Command): Command = Commands.parallel(*commands)

/**
 * Constructs a command that does nothing, finishing after a specified duration.
 * @param seconds after how long the command finishes
 * @return the command
 * @see edu.wpi.first.wpilibj2.command.WaitCommand
 */
fun waitCommand(seconds: Double): Command = Commands.waitSeconds(seconds)

/**
 * Constructs a command that does nothing, finishing after a specified duration.
 * @param time after how long the command finishes
 * @return the command
 * @see edu.wpi.first.wpilibj2.command.WaitCommand
 */
fun waitCommand(time: Time): Command = Commands.waitTime(time)

/**
 * Constructs a command that does nothing, finishing once a condition becomes true.
 * @param condition the condition
 * @return the command
 * @see edu.wpi.first.wpilibj2.command.WaitUntilCommand
 */
fun waitUntilCommand(condition: () -> Boolean): Command = Commands.waitUntil(condition)

/**
 * Constructs a command that does nothing, finishing once a condition becomes true.
 * @param condition the condition
 * @param overrideSeconds the maximum time to wait for the condition to become true
 * @return the command
 * @see WaitUntilOrTimeCommand
 */
fun waitUntilCommand(overrideSeconds: Double, condition: () -> Boolean): Command =
    WaitUntilOrTimeCommand(overrideSeconds, condition)

/**
 * Constructs a command that does nothing until interrupted.
 * @param requirements Subsystems to require
 * @return the command
 */
fun idleCommand(vararg requirements: Subsystem) = Commands.idle(*requirements)!!

/**
 * Runs a group of commands at the same time. Ends once a specific command finishes, and cancels the others.
 * @param deadline the deadline command
 * @param otherCommands the other commands to include
 * @return the command group
 * @throws IllegalArgumentException if the deadline command is also in the otherCommands argument
 * @see edu.wpi.first.wpilibj2.command.ParallelDeadlineGroup
 */
fun deadlineCommand(deadline: Command, vararg otherCommands: Command ): Command = Commands.deadline(deadline, *otherCommands)
/**
 * Runs a group of commands at the same time. Ends once any command in the group finishes, and cancels the others.
 * @param commands the commands to include
 * @return the command group
 * @see edu.wpi.first.wpilibj2.command.ParallelRaceGroup
 */
fun raceCommand(vararg commands: Command): Command = Commands.race(*commands)

/**
 * Runs one of two commands, based on the boolean selector function.
 * @param onTrue the command to run if the selector function returns true
 * @param onFalse the command to run if the selector function returns false
 * @param selector the selector function
 * @return the command
 * @see edu.wpi.first.wpilibj2.command.ConditionalCommand
 */
fun eitherCommand(onTrue: Command, onFalse: Command, selector: () -> Boolean): Command = Commands.either(onTrue, onFalse, selector)

/**
 * Constructs a command that runs an action once, and then runs an action every iteration until interrupted.
 * @param start the action to run on start
 * @param run the action to run every iteration
 * @param requirements subsystems the action requires
 * @return the command
 */
fun startRunCommand(vararg requirements: Subsystem, start: () -> Unit, run: () -> Unit): Command = Commands.startRun(start, run, *requirements)

/**
 * Constructs a command that runs an action once and another action when the command is interrupted.
 * @param start the action to run on start
 * @param end the action to run on interrupt
 * @param requirements subsystems the action requires
 * @return the command
 * @see edu.wpi.first.wpilibj2.command.StartEndCommand
 */
fun startEndCommand(vararg requirements: Subsystem, start: () -> Unit, end: () -> Unit): Command = Commands.startEnd(start, end, *requirements)

/**
 * Constructs a command that runs an action every iteration until interrupted, and then runs a second action.
 * @param run the action to run every iteration
 * @param end the action to run on interrupt
 * @param requirements subsystems the action requires
 * @return the command
 */
fun runEndCommand(vararg requirements: Subsystem, run: () -> Unit, end: () -> Unit): Command = Commands.runEnd(run, end, *requirements)

/**
 * Runs one of several commands, based on the selector function.
 * @param commands map of commands to select from
 * @param selector the selector function
 * @param K The type of key used to select the command
 * @return the command
 * @see edu.wpi.first.wpilibj2.command.SelectCommand
 */
fun <K> selectCommand(commands: Map<K, Command>, selector: () -> K): Command = Commands.select<K>(commands, selector)

/**
 * Creates [DeferredCommand], a command that gets constructed at runtime.
 *
 * The most similar type of command to 2025 Meanlib's "use" function.
 *
 * @param supplier a function that returns the command to run
 * @param requirements the subsystems required by the command
 *
 * @see DeferredCommand
 */
fun deferCommand(vararg requirements: Subsystem, supplier: () -> Command): Command = Commands.defer(supplier, mutableSetOf(*requirements))

/**
 * Creates [DeferredCommand], a command that gets constructed at runtime.
 *
 * The most similar type of command to 2025 Meanlib's "use" function.
 *
 * @param supplier a function that returns the command to run
 * @param requirements the subsystems required by the command
 *
 * @see DeferredCommand
 */
fun use(vararg requirements: Subsystem, supplier: () -> Command): Command = deferCommand(*requirements, supplier = supplier)
