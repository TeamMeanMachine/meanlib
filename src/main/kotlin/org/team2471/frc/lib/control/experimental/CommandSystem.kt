package org.team2471.frc.lib.control.experimental

import edu.wpi.first.wpilibj.DriverStation
import edu.wpi.first.wpilibj.Timer
import edu.wpi.first.wpilibj.Utility
import kotlinx.coroutines.experimental.*
import org.team2471.frc.lib.util.measureTimeFPGA
import java.util.*
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
fun Subsystem.registerDefaultCommand(context: CoroutineContext, command: Command) {
    if (this !in command.requirements)
        throw IllegalArgumentException("A subsystem cannot register a default command that doesn't require it.")
    else if (command.requirements.size != 1)
        throw IllegalArgumentException("A default command must require exactly one subsystem.")

    Command.defaultCommands.put(this, command to context)
    if (command !in Command.activeRequirements) command(context)
}

class Command(vararg internal val requirements: Subsystem, private val isInterruptible: Boolean = true,
              private val body: suspend Command.Scope.() -> Unit) {
    companion object {
        internal val defaultCommands: MutableMap<Subsystem, Pair<Command, CoroutineContext>> = HashMap()
        internal val activeRequirements: MutableMap<Subsystem, Command> = HashMap()

        private object InvokeMutex
    }

    /**
     * Returns `true` if the command has a running coroutine.
     */
    val isRunning get() = coroutine?.isCompleted == true

    val isCanceled get() = coroutine?.isCancelled == true

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
    operator fun invoke(context: CoroutineContext): Boolean {
        // Only one command may be invoked at a time.
        // This prevents race conditions where commands with overlapping requirements are invoked in parallel.
        synchronized(InvokeMutex) {
            if (isRunning) return false

            // Only use requirements if the provided coroutine context is a dispatcher such as a CommonPool.
            // This also means that commands must include all requirements in it's constructor, even if the
            // subsystem is required by a child command. TODO: define child command
            val conflictingCommands = if (context is CoroutineDispatcher) {
                requirements.mapNotNull { activeRequirements[it] }
            } else {
                emptyList()
            }

            if (conflictingCommands.any { !it.isInterruptible }) return false


            println("Running command with ${conflictingCommands.size} conflicting commands and job ${context[Job]}")
            // cancel conflicting commands
            conflictingCommands.forEach { it.cancel() }

            // take over requirements
            requirements.forEach { activeRequirements[it] = this }

            coroutine = launch(context) {
                try {
                    // suspend until conflicting commands have finished cancelling
                    conflictingCommands.forEach { it.coroutine?.join() }

                    // execute body
                    body(Scope(this@launch))
                } finally {
                    // clean up
                    requirements.filter { activeRequirements[it] == this@Command }.forEach {
                        activeRequirements.remove(it)
                        // restart default command if it exists

                        val (defaultCommand, defaultCommandContext) = defaultCommands[it] ?: return@forEach
                        defaultCommand(defaultCommandContext)
                    }
                }
            }
            return true
        }
    }

    suspend fun invokeAndJoin(context: CoroutineContext) {
        invoke(context)
        coroutine?.join()
    }

    suspend fun join() = coroutine?.join()

    /**
     * Cancel current job if present.
     */
    fun cancel() = coroutine?.cancel() ?: false

    /**
     * Receiver class for command instances.
     */
    class Scope internal constructor(private val scope: CoroutineScope) : CoroutineScope by scope {
        val startTimeNanos = Utility.getFPGATime()
        val elapsedTimeNanos get() = Utility.getFPGATime() - startTimeNanos

        val startTimeSeconds = Timer.getFPGATimestamp()
        val elapsedTimeSeconds get() = Timer.getFPGATimestamp() - startTimeSeconds

        /**
         * Runs the provided [body] of code periodically per [period] ms.
         *
         * The [period] parameter defaults to 20ms.
         *
         * Additionally, a [condition] can be provided to allow termination of the loop without cancellation
         * of the command coroutine.
         */
        suspend fun periodic(period: Int = 20,
                             condition: () -> Boolean = { true }, body: () -> Unit) {
            while (condition()) {
                val time = measureTimeFPGA {
                    body()
                }
                if (time > period * 1000) DriverStation.reportWarning("Periodic loop went over expected time. " +
                        "Got: ${time / 1000}ms, expected: <${period}ms", true)
                delay(elapsedTimeNanos % (period * 1000), TimeUnit.NANOSECONDS)
            }
        }

        suspend fun suspendUntil(pollingRate: Int = 20, condition: () -> Boolean) {
            while(!condition()) delay(pollingRate.toLong(), TimeUnit.MILLISECONDS)
        }

    }
}
