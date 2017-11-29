package org.team2471.frc.lib.control.experimental

import edu.wpi.first.wpilibj.DriverStation
import edu.wpi.first.wpilibj.Timer
import edu.wpi.first.wpilibj.Utility
import kotlinx.coroutines.experimental.*
import org.team2471.frc.lib.util.measureTimeFPGA
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
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
        throw IllegalArgumentException("A subsystem cannot register a default command that doesn't require it.")
    else if (command.requirements.size != 1)
        throw IllegalArgumentException("A default command must require exactly one subsystem.")

    Command.defaultCommands.put(this, command)
    if (command !in Command.activeRequirements) command()
}

class Command(vararg requirements: Subsystem, isCancelable: Boolean = true,
              private val body: suspend Command.Scope.() -> Unit) {
    companion object {
        internal val defaultCommands: MutableMap<Subsystem, Command> = HashMap()
        internal val activeRequirements: MutableMap<Subsystem, Command> = HashMap()

        private var contextInstance: CoroutineContext? = null
        val Context: CoroutineContext by lazy {
            contextInstance ?: throw IllegalStateException("The command context was accessed before it was initialized. " +
                    "Command.initCoroutineContext is either being called too late or not being called at all.")
        }

        fun initCoroutineContext(commandContext: CoroutineContext) {
            if (contextInstance != null)
                throw IllegalStateException("The command context can only be initialized once.")
            contextInstance = commandContext
        }
    }

    val isCancelable: Boolean = isCancelable
        get() = field && uncancelableChildren.get() == 0

    /**
     * Returns `true` if the command has a running coroutine.
     */
    val isRunning get() = coroutine?.isCompleted == true

    val isCanceled get() = coroutine?.isCancelled == true

    internal val requirements: Set<Subsystem> = hashSetOf(*requirements)

    private var coroutine: Job? = null
    private var uncancelableChildren = AtomicInteger(0)

    /**
     * Attempts to start the command inside a coroutine.
     *
     * If the command is running or one if it's required subsystems cannot be acquired, the command will not be started.
     *
     * A subsystem cannot be acquired if it's current command is not interrupible.
     *
     * If all subsystems can be acquired, commands requiring the subsystems will be canceled if present.
     */
    operator fun invoke() = invoke(null)

    suspend fun invokeAndJoin() {
        if (invoke()) coroutine?.join()
    }

    suspend fun join() = coroutine?.join()

    /**
     * Cancel current command if it is running.
     *
     * Returns true if the command was canceled as a result of this call.
     */
    fun cancel(cause: Throwable? = null): Boolean = if (isCancelable) {
        coroutine?.cancel(cause) ?: false
    } else {
        false
    }

    private operator fun invoke(parentCommand: Command?): Boolean {
        // Only one command may be invoked at a time.
        // This prevents race conditions where commands with overlapping requirements are invoked in parallel.
        synchronized(Command) {
            if (isRunning) return false

            val conflictingCommands = if (parentCommand == null) {
                requirements
            } else {
                requirements - parentCommand.requirements
            }.mapNotNull { activeRequirements[it] }

            if (conflictingCommands.any { !isCancelable }) return false

            if (!isCancelable) parentCommand?.uncancelableChildren?.incrementAndGet()


            conflictingCommands.forEach { it.cancel() }
            // take over requirements
            requirements.forEach { activeRequirements[it] = this }

            coroutine = launch(Context) {
                try {
                    // suspend until conflicting commands have finished cancelling
                    conflictingCommands.forEach { it.coroutine?.join() }

                    // execute body
                    body(Scope(this@Command, this@launch))
                } finally {
                    println("Cleaning up $this@Command")
                    // clean up
                    synchronized(Command) {
                        requirements.forEach { activeRequirements.remove(it) }
                        parentCommand?.uncancelableChildren?.decrementAndGet()
                    }

                    // invoke default command if it exists
                    requirements.filter { activeRequirements[it] == this@Command }
                            .forEach { defaultCommands[it]?.invoke() }
                    coroutine = null
                }
            }
            return true
        }
    }

    /**
     * Receiver class for command instances.
     */
    class Scope internal constructor(private val command: Command, private val scope: CoroutineScope) :
            CoroutineScope by scope {
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
            while (!condition()) delay(pollingRate.toLong(), TimeUnit.MILLISECONDS)
        }

        suspend fun fork(childCommand: Command) {
            if (childCommand.invoke(command)) childCommand.join()
        }
    }
}
