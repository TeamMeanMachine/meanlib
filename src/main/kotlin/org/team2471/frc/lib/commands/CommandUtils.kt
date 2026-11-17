package org.team2471.frc.lib.commands

import org.wpilib.command3.Command
import org.wpilib.command3.Coroutine
import org.wpilib.command3.Mechanism
import org.wpilib.command3.NeedsExecutionBuilderStage
import org.wpilib.command3.NeedsNameBuilderStage

/**
 * A typealias for a command that hasn't finished it's building. This command
 * cannot be scheduled and `.named` must be called.
 */
typealias UnnamedCommand = NeedsNameBuilderStage

// Partial Command Composers
/**
 * Partially composes a nameless command with [body] using provided [Mechanism]s.
 * This partial constructor is so the partial command can be modified before being composed/named, such as with [NeedsNameBuilderStage.whenCanceled]
 *
 * @param mechanisms required mechanisms for the command to execute.
 * @param body the executed [Coroutine] action.
 * @return a [UnnamedCommand] that must be [named] to be a valid [Command].
 * @see Command.requiring
 * @see NeedsExecutionBuilderStage.executing
 */
inline fun commandUnnamed(vararg mechanisms: Mechanism, crossinline body: Coroutine.() -> Unit): UnnamedCommand =
    Command.requiring(setOf(*mechanisms)).executing { it.body() }

// Full Command Composers

/**
 * Composes a [Command] with a [body] requireing the provided [Mechanism]s
 *
 * ```
 * // Command with name that requires Drive, Intake, and Shooter
 * fun myCoolAutoCommand() = command("myCoolAuto", Drive, Intake, Shooter) {
 *     println("Starting cool auto")
 *     await(driveForwardAndIntakeCommand(10.0.feet)) // Must await when running sequential commands
 *     println("Finished Intaking. Now aiming")
 *     Shooter.rampUpIdle() // Non-command methods don't use await
 *     await(Drive.aimAtGoalCommand())
 *     await(Shooter.shootCommand())
 *     println("Bye!")
 * }
 * ```
 *
 * @param name name of the command.
 * @param mechanisms required mechanisms for the command to execute.
 * @param body the executed [Coroutine] action.
 * @see Command.requiring
 * @see NeedsExecutionBuilderStage.executing
 * @see NeedsNameBuilderStage.named
 */
inline fun command(name: String, vararg mechanisms: Mechanism, crossinline body: Coroutine.() -> Unit): Command =
    commandUnnamed(*mechanisms, body = body).named(name)

/**
 * Finishes composing a command by naming it. Also, can apply a [priority].
 *
 * @param name the name of the command.
 * @param priority the priority of the command.
 * @return a composed [Command].
 * @see NeedsNameBuilderStage.withPriority
 */
fun UnnamedCommand.named(name: String, priority: Int): Command =
    withPriority(priority).named(name)

/**
 * Creates a duplicate [UnnamedCommand] from a command with the same requirements, action, priority, and exit actions.
 *
 * Must be renamed to become a [Command]. This doesn't duplicate the [name] or .until
 *
 * @return a [UnnamedCommand] of the command.
 */
fun Command.unnamedCommand(): UnnamedCommand = Command
    .requiring(this.requirements())
    .executing(this::run)
    .whenCanceled(this::onCancel)
    .whenExited(this::onExit)
    .withPriority(this.priority())

/**
 * Adds an action onto an existing commands' [onCancel] action.
 *
 * [onCancel] actions will run when the command is canceled, NOT when the command finishes naturally. See [onExit]
 *
 * @param addOnCancel the action to perform when the command is canceled.
 * @return the modified command.
 * @see NeedsNameBuilderStage.whenCanceled
 * @see onExit
 */
fun Command.onCancel(addOnCancel: () -> Unit): Command = this.replaceOnCancel { this.onCancel(); addOnCancel() }

/**
 * Adds an action onto an existing commands' [onExit] action.
 *
 * [onExit] will run when the command ends naturally or is canceled. See [onCancel]
 *
 * @param addOnExit the action to perform when the command finishes naturally or is canceled.
 * @return the modified command.
 * @see NeedsNameBuilderStage.whenExited
 * @see onCancel
 */
fun Command.onExit(addOnExit: () -> Unit): Command = this.replaceOnExit { this.onExit(); addOnExit() }

/**
 * Replaces an existing command's onCancel action with [newOnCancel].
 * The onCancel action provided here will override whatever onCancel action was already set.
 * [newOnCancel] will not run if the command completes naturally.
 *
 * @param newOnCancel the action to perform when the command is canceled.
 * @return the modified command.
 * @see NeedsNameBuilderStage.whenCanceled
 */
fun Command.replaceOnCancel(newOnCancel: () -> Unit): Command = this.unnamedCommand().whenCanceled(newOnCancel).named(name())

/**
 * Replaces an existing commands' onExit action with a new action.
 * The onExit action provided here will override whatever onExit action was already set.
 * [newOnExit] will run when the command ends naturally or is canceled.
 *
 * @param newOnExit the action to perform when the command finishes naturally or is canceled.
 * @return the modified command.
 * @see NeedsNameBuilderStage.whenExited
 */
fun Command.replaceOnExit(newOnExit: () -> Unit): Command = this.unnamedCommand().whenExited(newOnExit).named(name())

/**
 * Replaces an existing commands' priority. Default `0`
 *
 * @param newPriority the new priority for the command. [[Int.MIN_VALUE] - [Int.MAX_VALUE]]
 * @return the modified command.
 * @see NeedsNameBuilderStage.withPriority
 */
fun Command.priority(newPriority: Int): Command = this.unnamedCommand().withPriority(newPriority).named(name())


