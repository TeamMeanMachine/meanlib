package org.team2471.frc.lib.commands

import org.team2471.frc.lib.coroutines.measureTimeMonotonic
import org.team2471.frc.lib.units.seconds
import org.wpilib.command3.Command
import org.wpilib.command3.Coroutine
import org.wpilib.command3.Mechanism
import org.wpilib.command3.NeedsNameBuilderStage
import org.wpilib.command3.Scheduler
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
fun useNoName(vararg mechanisms: Mechanism, body: Coroutine.() -> Unit): NeedsNameBuilderStage =
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
fun use(name: String = object {}.javaClass.enclosingMethod.name, vararg mechanisms: Mechanism, body: Coroutine.() -> Unit, onCancel: () -> Unit): Command =
    useNoName(*mechanisms, body = body).named(name, onCancel = onCancel)
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
fun use(name: String = object {}.javaClass.enclosingMethod.name, vararg mechanisms: Mechanism, body: Coroutine.() -> Unit): Command =
    useNoName(*mechanisms, body = body).named(name)
/**
 * Composes a [Command] with an action using provided [Mechanism]s from a [Coroutine] function
 *
 * @param name the name of the command.
 * @param mechanisms the mechanisms the command requires.
 * @param onCancel the function to run when the command gets canceled, doesn't run if the command completes naturally.
 * @see Command.requiring
 * @see NeedsExecutionBuilderStage.executing
 * @see NeedsNameBuilderStage.named
 */
fun (Coroutine.() -> Unit).use(name: String, vararg mechanisms: Mechanism, onCancel: () -> Unit = {}) =
    use(name, *mechanisms, body = this, onCancel = onCancel)

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
fun NeedsNameBuilderStage.named(name: String, priority: Int? = null, onCancel: () -> Unit = {}): Command =
    (if (priority != null) withPriority(priority) else this).whenCanceled(onCancel).named(name)

/**
 * Modifies an existing commands' [onCancel] action.
 * [onCancel] will not run if the command completes naturally.
 *
 * @param onCancel the action to perform when the command is canceled.
 * @return the modified command.
 * @see NeedsNameBuilderStage.whenCanceled
 */
fun Command.modifyOnCancel(onCancel: () -> Unit): Command = Command.requiring(this.requirements()).executing(this::run).whenCanceled(onCancel).named("${this.name()}WithOnCancel")


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
        if (remainder <= 0.0) {
            yield()
        } else {
            wait(remainder.seconds)
        }
    }
}

inline fun Coroutine.periodicTimeout(
    timeout: Double,
    period: Double = 0.02,
    watchOverrunName: String? = null,
    crossinline body: PeriodicScope.() -> Unit
): Boolean {
    val timer = Timer()
    var timedOut = false
    timer.start()
    periodic(period, watchOverrunName) {
        if (timer.get() > timeout) {
            timedOut = true
            stop()
        } else {
            body()
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



//fun test2Command() = use("test2Command") { println("in test2 command") }
//
//fun test3Command() = Command.parallel()
//
//val testCommand4: Command = use {
//
//}.whenCanceled {
//
//}.named("testCommand4")
//
//val testCommand = use("testCommand") {
//    periodic {
//        println("in periodic")
//
//        wait(1.0.seconds)
//
//        println("bye")
//        stop()
//    }
//
//    parallel(use ("parallel1"){
//        periodic {
//            println("in periodic2")
//        }
//    }, use ("parallel2") {
//        periodic {
//            println("in periodic3")
//        }
//    })
//
//    parallel(
//        {
//            periodic {
//                println("in parallel")
//            }
//        }, {
//            periodic {
//                println("in parallel2")
//            }
//        }
//    )
//
//
//    wait(1.0.seconds)
//
////    val test2Command = useWithName("test2Command") { println("in test2 command") }
//    val test3Command = use("test3Command") { println("in test3 command") }
//
//    test2Command() // bad. doesn't work
//
//    await(test2Command()) // do this
//
//    awaitAll(test2Command(), test3Command)
//
//    waitUntil { false }
//
//    println("hi")
//}