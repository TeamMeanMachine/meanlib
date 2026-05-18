package org.team2471.frc.lib.commands

import org.team2471.frc.lib.coroutines.measureTimeMonotonic
import org.team2471.frc.lib.units.seconds
import org.wpilib.command3.Command
import org.wpilib.command3.Coroutine
import org.wpilib.command3.Mechanism
import org.wpilib.command3.NeedsExecutionBuilderStage
import org.wpilib.command3.NeedsNameBuilderStage
import org.wpilib.driverstation.DriverStationErrors
import org.wpilib.system.Timer
import org.wpilib.system.Watchdog

class PeriodicScope {
    @PublishedApi
    internal var isDone = false

    fun stop() {
        isDone = true
    }
}

var unnamedCommandCount = 0



// Partial Command Composers
/**
 * Partially composes a nameless command with [body] using provided [Mechanism]s.
 * This partial constructor is so the partial command can be modified before being composed/named, such as with [NeedsNameBuilderStage.whenCanceled]
 *
 * @param mechanisms required mechanisms for the command to execute.
 * @param body the executed [Coroutine] action.
 * @return a [NeedsNameBuilderStage] that must be [named] to be a valid [Command].
 * @see Command.requiring
 * @see NeedsExecutionBuilderStage.executing
 */
fun useUnnamed(vararg mechanisms: Mechanism, body: Coroutine.() -> Unit): NeedsNameBuilderStage =
    Command.requiring(setOf(*mechanisms)).executing(body)

// Full Command Composers
/**
 * Composes a [Command] with a [body] using the provided [Mechanism]s
 *
 * @param name name of the command.
 * @param mechanisms required mechanisms for the command to execute.
 * @param body the executed [Coroutine] action.
 * @param onCancel the function to run when the command gets canceled, doesn't run if the command completes naturally.
 * @see Command.requiring
 * @see NeedsExecutionBuilderStage.executing
 * @see NeedsNameBuilderStage.named
 */
@Suppress("NOTHING_TO_INLINE")
inline fun use(name: String, vararg mechanisms: Mechanism, noinline body: Coroutine.() -> Unit, noinline onCancel: () -> Unit): Command =
    useUnnamed(*mechanisms, body = body).named(name, onCancel = onCancel)
/**
 * Composes a [Command] with an action using provided [Mechanism]s
 *
 * @param name the name of the command.
 * @param mechanisms required mechanisms for the command to execute.
 * @param body the executed [Coroutine] action.
 * @see Command.requiring
 * @see NeedsExecutionBuilderStage.executing
 * @see NeedsNameBuilderStage.named
 */
@Suppress("NOTHING_TO_INLINE")
inline fun use(name: String, vararg mechanisms: Mechanism, noinline body: Coroutine.() -> Unit): Command =
    useUnnamed(*mechanisms, body = body).named(name)

/**
 * Composes a [Command] with an action using provided [Mechanism]s
 * Automatically names the command using the name of the method that called this function.
 *
 * @param mechanisms required mechanisms for the command to execute.
 * @param body the executed [Coroutine] action.
 * @see Command.requiring
 * @see NeedsExecutionBuilderStage.executing
 * @see NeedsNameBuilderStage.named
 */
@Suppress("NOTHING_TO_INLINE")
inline fun use(vararg mechanisms: Mechanism, noinline body: Coroutine.() -> Unit): Command =
    useUnnamed(*mechanisms, body = body).named(try {object {}.javaClass.enclosingMethod.name} catch (e: Exception) { "lambda unnamed $unnamedCommandCount".also { unnamedCommandCount++ } })

/**
 * Composes a [Command] with a [body] using the provided [Mechanism]s
 * Automatically names the command using the name of the method that called this function.
 *
 * @param mechanisms required mechanisms for the command to execute.
 * @param body the executed [Coroutine] action.
 * @param onCancel the function to run when the command gets canceled, doesn't run if the command completes naturally.
 * @see Command.requiring
 * @see NeedsExecutionBuilderStage.executing
 * @see NeedsNameBuilderStage.named
 */
@Suppress("NOTHING_TO_INLINE")
inline fun use(vararg mechanisms: Mechanism, noinline body: Coroutine.() -> Unit, noinline onCancel: () -> Unit): Command =
    useUnnamed(*mechanisms, body = body).named(object {}.javaClass.enclosingMethod.name, onCancel = onCancel)

/**
 * Finishes composing a command by naming it. Also, can apply an [onCancel] action.
 * [onCancel] will not run if the command completes naturally.
 *
 * @param name the name of the command.
 * @param onCancel the function to run when the command gets canceled, doesn't run if the command completes naturally.
 * @return a composed [Command].
 * @see NeedsNameBuilderStage.named
 * @see NeedsNameBuilderStage.whenCanceled
 */
@Suppress("NOTHING_TO_INLINE")
inline fun NeedsNameBuilderStage.named(name: String = object {}.javaClass.enclosingMethod.name, priority: Int? = null, noinline onCancel: () -> Unit = {}): Command =
    (if (priority != null) withPriority(priority) else this).whenCanceled(onCancel).named(name)

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


inline fun Coroutine.periodic(
    period: Double = 0.0,
    watchOverrunName: String? = null,
    crossinline body: PeriodicScope.() -> Unit
) {
    val scope = PeriodicScope()

    val watchdog = if (watchOverrunName != null) {
        Watchdog(period) { DriverStationErrors.reportWarning("Periodic loop $watchOverrunName overrun", true) }
    } else {
        null
    }

    while (true) {
        watchdog?.reset()
        val dt = measureTimeMonotonic {
            body(scope)
        }
        if (scope.isDone) break
        val remainder = period - dt
        if (remainder <= 0.0 || period == 0.0) {
            yield()
        } else {
            wait(remainder.seconds)
        }
    }
}

inline fun Coroutine.periodicTimeout(
    timeout: Double,
    period: Double = 0.0,
    watchOverrunName: String? = null,
    crossinline body: PeriodicScope.(Double) -> Unit
): Boolean {
    val timer = Timer()
    var timedOut = false
    timer.start()
    periodic(period, watchOverrunName) {
        val t = timer.get()
        if (timer.get() > timeout) {
            timedOut = true
            stop()
        } else {
            body(t)
        }
    }
    return timedOut
}

fun Coroutine.parallel(
    vararg commands: Command
) {
    awaitAll(*commands)
}

fun Coroutine.parallel(
    vararg blocks: Coroutine.() -> Unit
) {
    parallel(*blocks.mapIndexed { index, coroutine -> use("unnamedParallel$index") { coroutine() }}.toTypedArray())
}