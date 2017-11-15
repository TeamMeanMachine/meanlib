package org.team2471.frc.lib.control.experimental

import edu.wpi.first.wpilibj.Utility
import kotlinx.coroutines.experimental.*
import java.util.concurrent.TimeUnit
import kotlin.coroutines.experimental.CoroutineContext

// anything can be a subsystem!
typealias Subsystem = Any

/**
 * Registers the given [command] as the subsystem's default command.
 *
 * When a command that requires a subsystem terminates, the subsystem's default command will be started if present.
 *
 * If a command is not running that requires the calling subsystem, the provided command will be started.
 */
fun Subsystem.registerDefaultCommand(command: Command) {
    if (this !in command.requirements)
        throw IllegalArgumentException("A subsystem cannot register a default command that doesn't require it!")
    else if (command.requirements.size != 1)
        throw IllegalArgumentException("A default command must require exactly one subsystem!")

    Command.defaultCommands.put(this, command)
    if (Command.activeRequirements[command] == null) launch { command(CommonPool) }
}

class Command(vararg internal val requirements: Subsystem, private val isInterruptible: Boolean = true,
              private val body: suspend Command.Scope.() -> Unit) {
    internal companion object {
        val defaultCommands: MutableMap<Subsystem, Command> = HashMap()
        val activeRequirements: MutableMap<Subsystem, Command> = HashMap()
    }

    /**
     * Returns `true` if the command has a running coroutine.
     */
    val isRunning get() = coroutine?.isCompleted == true

    private var coroutine: Job? = null

    /**
     * Attempts to start the command inside a coroutine.
     *
     * If the command is running or one if it's required subsystems cannot be acquired, the command will not be started.
     *
     * A subsystem cannot be acquired if it's current command is not interrupible.
     *
     * If all subsystems can be acquired, commands requiring the subsystems will be canceled if present.
     */
    operator fun invoke(context: CoroutineContext) {
        if (isRunning) return

        val conflictingCommands = requirements.mapNotNull { activeRequirements[it] }

        if (conflictingCommands.any { !it.isInterruptible }) return

        coroutine = launch(context) {
            try {
                requirements.forEach { activeRequirements[it] = this@Command }

                // cancel conflicting commands
                conflictingCommands.forEach { it.cancel() }
                // wait for conflicting commands to complete cancellation
                conflictingCommands.forEach { it.join() }
                // suspend until conflicting commands have finished cancelling
                conflictingCommands.mapNotNull { it.coroutine }.forEach { it.join() }


                body(Scope(this))
            } finally {
                requirements.forEach { activeRequirements.remove(it) }
                // restart default commands
                requirements.forEach { defaultCommands[it]?.invoke(CommonPool) }
            }
        }
    }

    /**
     * Cancel current job if present.
     */
    fun cancel() = coroutine?.cancel()

    /**
     * Suspend until coroutine completes, if present.
     */
    suspend fun join() = coroutine?.join()

    /**
     * Cancel current job if present, and suspend until cancellation completes.
     */
    suspend fun cancelAndJoin() = coroutine?.cancelAndJoin()

    /**
     * Receiver class for command instances.
     */
    class Scope internal constructor(scope: CoroutineScope) : CoroutineScope by scope {
        val startTime = Utility.getFPGATime()
        val elapsedTime get() = Utility.getFPGATime() - startTime

        /**
         * Runs the provided [body] of code periodically per [period] ms.
         *
         * The [period] parameter defaults to 20ms.
         *
         * Additionally, a [condition] can be provided to allow termination of the loop without cancellation
         * of the command coroutine.
         */
        suspend fun periodic(period: Int = 20, condition: () -> Boolean = { true }, body: () -> Unit) {
            while (condition()) {
                body()
                delay(elapsedTime % (period * 1000), TimeUnit.NANOSECONDS)
            }

        }


        /**
         * Runs all provided [bodies] as their own coroutines in parallel,
         * and suspends until all coroutines are finished.
         */
        suspend fun parallel(vararg bodies: suspend () -> Unit) {
            bodies.map { launch(coroutineContext) { it() } }.forEach { it.join() }
        }

        suspend fun fork(command: Command) {

        }
    }
}

val test = Command {
    parallel({
        if(System.getenv()["COMPETITION"] == "1") {

        }

    }, {

    })
}
