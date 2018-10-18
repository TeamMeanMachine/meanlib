package org.team2471.frc.lib.control.experimental.next

import edu.wpi.first.wpilibj.DriverStation.reportError
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.actor
import kotlin.coroutines.experimental.AbstractCoroutineContextElement
import kotlin.coroutines.experimental.CoroutineContext
import kotlin.coroutines.experimental.coroutineContext

internal object EventHandler {
    private val messageChannel = actor<Message>(MeanlibContext, Channel.UNLIMITED) {
        // handle incoming messages as they arrive
        for (message in channel) handleMessage(message)
    }

    suspend fun useResources(
            resources: Set<Resource>,
            cancelConflicts: Boolean = true,
            body: suspend () -> Unit
    ) {
        val context = coroutineContext

        suspendCancellableCoroutine<Unit> { cont ->
            val message = Message.NewAction(resources, context, body, cancelConflicts, cont)
            messageChannel.offer(message)
        }
    }

    fun enableResource(resource: Resource) {
        messageChannel.offer(Message.Enable(resource))
    }

    fun disableResource(resource: Resource) {
        messageChannel.offer(Message.Disable(resource))
    }

    private fun resetResource(resource: DaemonResource) {
        launch(MeanlibContext) {
            use(resource, cancelConflicts = false) {
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

                // verify that all requirements are enabled
                val disabledResources = allResources.filter { !it.isEnabled }
                if (disabledResources.isNotEmpty()) {
                    message.continuation.resumeWithException(CancellationException("Action not allowed to use" +
                            "disabled resources { ${disabledResources.joinToString { it.name }} }"))
                    return
                }


                // find conflicts
                val conflictingHandles = newResources.filter { it.activeJob != null }

                // verify that all conflicts can be canceled
                if (!message.cancelConflicts && conflictingHandles.isNotEmpty()) {
                    message.continuation.resumeWithException(CancellationException("Action not allowed to cancel conflicts" +
                            "{ ${conflictingHandles.joinToString { it.name }} }"))
                    return
                }

                val conflictingJobs = conflictingHandles.map { it.activeJob!! }

                // spawn action coroutine
                val actionJob = launch(message.callerContext + Requirements(allResources), CoroutineStart.ATOMIC) {
                    try {
                        // cancel conflicts - action coroutines must not complete until
                        // it's conflict's jobs have finished execution
                        withContext(NonCancellable) {
                            val e = CancellationException("Taking over subsystems " +
                                    "{ ${conflictingHandles.joinToString { it.name }} }")
                            conflictingJobs.forEach { it.cancel(e) }
                            conflictingJobs.forEach { it.join() }
                        }

                        // run provided code
                        message.body()

                        // resume calling coroutine
                        message.continuation.resume(Unit)
                    } catch (exception: Throwable) {
                        // pass exception to calling coroutine
                        message.continuation.resumeWithException(exception)
                    } finally {
                        // tell the scheduler that the action job has finished executing
                        messageChannel.offer(Message.Clean(allResources, coroutineContext[Job]!!))
                    }
                }

                // write over state - future actions that use these resources will
                // cancel and wait for the action job to complete
                allResources.forEach { it.activeJob = actionJob }

                // launch watchdog coroutine
                launch(MeanlibContext) {
                    var i = 0
                    while (!actionJob.isCompleted) {
                        if (actionJob.isCancelled) {
                            if (i > 0) reportError("Action job hanging up subsystems " +
                                    "{ ${newResources.joinToString { it.name }} } (${i * 2.5}s)", false)
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
                            if (resource.isEnabled && resource is DaemonResource) resetResource(resource)
                        }
            }

            is Message.Enable -> {
                if (message.resource.isEnabled) return
                message.resource.isEnabled = true

                if (message.resource is DaemonResource) resetResource(message.resource)
            }

            is Message.Disable -> {
                if (!message.resource.isEnabled) return

                message.resource.isEnabled = false
                message.resource.activeJob?.cancel(CancellationException("Resource ${message.resource.name} disabled."))
            }
        }
    }

    private sealed class Message {
        class NewAction(
                val resources: Set<Resource>,
                val callerContext: CoroutineContext,
                val body: suspend () -> Unit,
                val cancelConflicts: Boolean,
                val continuation: CancellableContinuation<Unit>
        ) : Message()

        class Clean(val resources: Iterable<Resource>, val job: Job) : Message()

        class Enable(val resource: Resource) : Message()

        class Disable(val resource: Resource) : Message()
    }

    private class Requirements(
            requirements: Set<Resource>
    ) : Set<Resource> by requirements, AbstractCoroutineContextElement(Key) {
        companion object Key : CoroutineContext.Key<Requirements>
    }
}

suspend fun use(vararg resources: Resource, cancelConflicts: Boolean = true, body: suspend () -> Unit) =
        EventHandler.useResources(setOf(*resources), cancelConflicts, body)
