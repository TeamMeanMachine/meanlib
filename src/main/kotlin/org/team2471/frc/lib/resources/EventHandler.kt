package org.team2471.frc.lib.resources

import edu.wpi.first.wpilibj.DriverStation.reportError
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import org.team2471.frc.lib.coroutines.MeanlibScope
import kotlin.coroutines.*

@ExperimentalCoroutinesApi
internal object EventHandler {
    private val messageChannel = Channel<Message>(capacity = Channel.UNLIMITED)

    init {
        MeanlibScope.launch {
            for (message in messageChannel) handleMessage(
                message
            )
        }
    }

    suspend fun <R> useResources(
        resources: Set<Resource>,
        cancelConflicts: Boolean,
        body: suspend () -> R
    ): R {
        val context = coroutineContext

        @Suppress("UNCHECKED_CAST")
        return suspendCancellableCoroutine<Any?> { cont ->
            val message = Message.NewAction(
                resources,
                context,
                body,
                cancelConflicts,
                cont
            )
            messageChannel.offer(message)
        } as R
    }

    fun enableResource(resource: Resource) {
        messageChannel.offer(
            Message.Enable(
                resource
            )
        )
    }

    fun disableResource(resource: Resource) {
        messageChannel.offer(
            Message.Disable(
                resource
            )
        )
    }

    private fun resetResource(resource: DaemonResource) {
        MeanlibScope.launch {
            useResources(setOf(resource), false) {
                resource.onReset()
            }
        }
    }

    //
    // MESSAGE HANDLING
    //

    private fun handleMessage(message: Message) {
        when (message) {
            is Message.NewAction -> {
                // subsystems required by use calls being used in calling coroutine
                val prevResources: Set<Resource> = message.callerContext[Requirements] ?: emptySet()
                // newly required subsystems (not previously required)
                val newResources = message.resources - prevResources
                // all subsystems
                val allResources = message.resources + prevResources

                // verify that all required resources are enabled
                val disabledResources = allResources.filter { !it.isEnabled }
                if (disabledResources.isNotEmpty()) {
                    message.continuation.resumeWithException(CancellationException("Action not allowed to use" +
                            "disabled resources { ${disabledResources.joinToString { it.name }} }"
                    )
                    )
                    return
                }

                // find conflicting resources
                val conflictResources = newResources.filter { it.activeJob != null }

                // verify that all conflicting resources can be canceled
                if (!message.cancelConflicts && conflictResources.isNotEmpty()) {
                    message.continuation.resumeWithException(CancellationException("Action not allowed to cancel conflicts" +
                            "{ ${conflictResources.joinToString { it.name }} }"
                    )
                    )
                    return
                }

                val conflictJobs = conflictResources.map { it.activeJob!! }

                // spawn action coroutine
                val actionJob = CoroutineScope(message.callerContext).launch(
                    Requirements(allResources),
                    CoroutineStart.ATOMIC
                ) {
                    try {
                        // cancel conflicts - action coroutines must not complete until
                        // it's conflict's jobs have finished execution
                        withContext(NonCancellable) {
//                            val e = CancellationException("Taking over subsystems " +
//                                    "{ ${conflictResources.joinToString { it.name }} }"
//                            )
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
                        messageChannel.offer(
                            Message.Clean(
                                allResources,
                                coroutineContext[Job]!!
                            )
                        )
                    }
                }

                // write over state - future actions that use these resources will
                // cancel and wait for the action job to complete
                allResources.forEach { it.activeJob = actionJob }

                // launch watchdog coroutine
                MeanlibScope.launch {
                    var i = 0
                    while (!actionJob.isCompleted) {
                        if (actionJob.isCancelled) {
                            if (i > 0) reportError("Action job hanging up subsystems " +
                                    "{ ${newResources.joinToString { it.name }} } (${i * 2.5}s)", false
                            )
                            i++
                        }

                        delay(2500)
                    }
                }
            }

            is Message.Clean -> {
                message.resources
                    .filter { it.activeJob === message.job }
                    .forEach { resource ->
                        resource.activeJob = null
                        if (resource.isEnabled && resource is DaemonResource) resetResource(
                            resource
                        )
                    }
            }

            is Message.Enable -> {
                if (message.resource.isEnabled) return
                message.resource.isEnabled = true

                if (message.resource is DaemonResource) resetResource(
                    message.resource
                )
            }

            is Message.Disable -> {
                if (!message.resource.isEnabled) return

//                val cause = CancellationException("Resource ${message.resource.name} disabled.")
                message.resource.isEnabled = false
                message.resource.activeJob?.cancel()
            }
        }
    }

    private sealed class Message {
        class NewAction(
            val resources: Set<Resource>,
            val callerContext: CoroutineContext,
            val body: suspend () -> Any?,
            val cancelConflicts: Boolean,
            val continuation: CancellableContinuation<Any?>
        ) : Message()

        class Clean(val resources: Set<Resource>, val job: Job) : Message()

        class Enable(val resource: Resource) : Message()

        class Disable(val resource: Resource) : Message()
    }

    private class Requirements(
        requirements: Set<Resource>
    ) : Set<Resource> by requirements, AbstractCoroutineContextElement(Key) {
        companion object Key : CoroutineContext.Key<Requirements>
    }
}
