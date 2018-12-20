package org.team2471.frc.lib.framework.internal

import edu.wpi.first.wpilibj.DriverStation.reportError
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import org.team2471.frc.lib.coroutines.MeanlibScope
import org.team2471.frc.lib.framework.DaemonSubsystem
import org.team2471.frc.lib.framework.Subsystem
import kotlin.coroutines.*

@ExperimentalCoroutinesApi
internal object EventHandler {
    private val messageChannel = Channel<Message>(capacity = Channel.UNLIMITED)

    init {
        MeanlibScope.launch {
            for (message in messageChannel) handleMessage(message)
        }
    }

    suspend fun <R> useSubsystems(
        subsystems: Set<Subsystem>,
        cancelConflicts: Boolean,
        body: suspend () -> R
    ): R {
        val context = coroutineContext

        @Suppress("UNCHECKED_CAST")
        return suspendCancellableCoroutine<Any?> { cont ->
            val message = Message.NewAction(
                subsystems,
                context,
                body,
                cancelConflicts,
                cont
            )
            messageChannel.offer(message)
        } as R
    }

    fun enableSubsystem(subsystem: Subsystem) {
        messageChannel.offer(Message.Enable(subsystem))
    }

    fun disableSubsystem(subsystem: Subsystem) {
        messageChannel.offer(Message.Disable(subsystem))
    }

    private fun resetSubsystem(subsystem: DaemonSubsystem) {
        MeanlibScope.launch {
            useSubsystems(setOf(subsystem), false, subsystem::default)
        }
    }

    //
    // MESSAGE HANDLING
    //

    private var actions = 0

    @ExperimentalCoroutinesApi
    private fun handleMessage(message: Message) {
        when (message) {
            is Message.NewAction -> {
                // subsystems required by use calls being used in calling coroutine
                val prevSubsystems: Set<Subsystem> = message.callerContext[Requirements] ?: emptySet()
                // newly required subsystems (not previously required)
                val newSubsystems = message.subsystems - prevSubsystems
                // all subsystems
                val allSubsystems = message.subsystems + prevSubsystems

                val id = actions++

                // verify that all required subsystems are enabled
                if (allSubsystems.any { !it.isEnabled }) {
                    return message.continuation.resumeWithException(CancellationException(
                        "Action not allowed to use disabled subsystems { ${allSubsystems.filter {
                            !it.isEnabled
                        }.joinToString { it.name }} }"
                    )
                    )
                }

                // find conflicting subsystems
                val conflictResources = newSubsystems.filter { it.activeJob != null }

                // verify that all conflicting subsystems can be canceled
                if (!message.cancelConflicts && conflictResources.isNotEmpty()) {
                    message.continuation.resumeWithException(
                        CancellationException(
                            "Action not allowed to cancel conflicts" +
                                    "{ ${conflictResources.joinToString { it.name }} }"
                        )
                    )
                    return
                }

                val conflictJobs = conflictResources.map { it.activeJob!! }

                // spawn action coroutine
                val actionJob = CoroutineScope(message.callerContext).launch(
                    Requirements(allSubsystems),
                    CoroutineStart.ATOMIC
                ) {
                    try {
                        // Cancel conflicting jobs. It is critical that coroutines must not complete until
                        // it's conflict's jobs have finished execution
                        withContext(NonCancellable) {
                            conflictJobs.forEach { it.cancel() }
                            conflictJobs.forEach { it.join() }
                        }

                        // run provided code
                        val result = message.body()

                        // resume calling coroutine
                        message.continuation.resume(result)
                    } catch (exception: Throwable) {
                        // pass exception to calling coroutine
                        message.continuation.resumeWithException(exception)
                    } finally {
                        // tell the scheduler that the action job has finished executing
                        messageChannel.offer(Message.Clean(newSubsystems, coroutineContext[Job]!!))
                    }
                }

                // write over state - future actions that use these subsystems will
                // cancel and wait for the action job to complete
                newSubsystems.forEach { it.activeJob = actionJob }

                // launch watchdog coroutine
                MeanlibScope.launch {
                    var i = 0
                    while (!actionJob.isCompleted) {
                        if (actionJob.isCancelled) {
                            if (i > 0) reportError(
                                "Action job hanging up subsystems " +
                                        "{ ${newSubsystems.joinToString { it.name }} } (${i * 2.5}s)", false
                            )
                            i++
                        }

                        delay(2500)
                    }
                }
            }

            is Message.Clean -> {
                message.subsystems
                    .filter { it.activeJob === message.job }
                    .forEach { subsystem ->
                        subsystem.activeJob = null
                        if (subsystem.isEnabled && subsystem is DaemonSubsystem) resetSubsystem(subsystem)
                    }
            }

            is Message.Enable -> {
                if (message.subsystem.isEnabled) return
                message.subsystem.isEnabled = true

                if (message.subsystem is DaemonSubsystem) resetSubsystem(message.subsystem)
            }

            is Message.Disable -> {
                if (!message.subsystem.isEnabled) return

                message.subsystem.isEnabled = false
                message.subsystem.activeJob?.cancel()
            }
        }
    }

    private sealed class Message {
        class NewAction(
            val subsystems: Set<Subsystem>,
            val callerContext: CoroutineContext,
            val body: suspend () -> Any?,
            val cancelConflicts: Boolean,
            val continuation: CancellableContinuation<Any?>
        ) : Message()

        class Clean(val subsystems: Set<Subsystem>, val job: Job) : Message()

        class Enable(val subsystem: Subsystem) : Message()

        class Disable(val subsystem: Subsystem) : Message()
    }

    private class Requirements(
        requirements: Set<Subsystem>
    ) : Set<Subsystem> by requirements, AbstractCoroutineContextElement(Key) {
        companion object Key : CoroutineContext.Key<Requirements>
    }
}