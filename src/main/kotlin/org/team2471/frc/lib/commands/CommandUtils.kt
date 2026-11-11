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
 * Adds an action onto an existing commands' [onCancelAction] action.
 * Both actions will run when the command is canceled.
 * [onCancelAction] will not run if the command completes naturally.
 *
 * @param onCancelAction the action to perform when the command is canceled.
 * @return the modified command.
 * @see NeedsNameBuilderStage.whenCanceled
 */
fun Command.onCancel(onCancelAction: () -> Unit): Command = Command.requiring(this.requirements()).executing(this::run).whenCanceled { this.onCancel(); onCancelAction() }.named("${this.name()}WithOnCancel")

/**
 * Replaces an existing commands' [onCancel] action to a new action.
 * The onCancel action provided here will override whatever onCancel action was already set.
 * [onCancel] will not run if the command completes naturally.
 *
 * @param onCancel the action to perform when the command is canceled.
 * @return the modified command.
 * @see NeedsNameBuilderStage.whenCanceled
 */
fun Command.replaceOnCancel(onCancel: () -> Unit): Command = Command.requiring(this.requirements()).executing(this::run).whenCanceled(onCancel).named("${this.name()}WithOnCancel")