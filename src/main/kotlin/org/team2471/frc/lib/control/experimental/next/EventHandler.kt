package org.team2471.frc.lib.control.experimental.next

import edu.wpi.first.wpilibj.DriverStation.reportError
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.channels.Channel
import kotlin.coroutines.experimental.AbstractCoroutineContextElement
import kotlin.coroutines.experimental.CoroutineContext
import kotlin.coroutines.experimental.coroutineContext

object EventHandler {
    private val cache = hashMapOf<Resource, Job>()
    private val registry = hashSetOf<Resource>()

    private val channel = Channel<Message>(Channel.UNLIMITED)

    init {
        // spawn scheduler coroutine
        launch(MeanlibContext) {
            // handle incoming messages as they arrive
            for (message in channel) message.handle()
        }
    }

    /*
     * PUBLIC API
     */

    var isEnabled = false
        private set

    fun enable() {
        channel.offer(Message.Enable)
    }

    fun disable() {
        channel.offer(Message.Disable)
    }

    internal suspend fun useResources(
            resources: Array<out Resource>,
            cancelConflicts: Boolean = true,
            body: suspend () -> Unit
    ) {
        val context = coroutineContext

        suspendCancellableCoroutine<Unit> { cont ->
            val message = Message.NewAction(setOf(*resources), context, body, cancelConflicts, cont)
            channel.offer(message)
        }
    }

    /*
     * INTERNAL API
     */

    internal fun register(resource: Resource) {
        channel.offer(Message.RegisterSubsystem(resource))
    }

    /*
     * MESSAGE HANDLING
     */

    private interface Message {
        fun handle()

        object Enable : Message {
            override fun handle() {
                // do nothing if already enabled
                if (isEnabled) return

                // put all subsystems into default states
                registry.forEach { it.launchDefaultAction() }

                // update state
                isEnabled = true
            }
        }

        object Disable : Message {
            override fun handle() {
                // do nothing if already disabled
                if (!isEnabled) return

                // cancel all running actions
                val e = CancellationException("Subsystem Coordinator Disabled")
                cache.forEach { (_, job) -> job.cancel(e) }

                // update state
                isEnabled = false
            }
        }

        data class NewAction(
                private val resources: Set<Resource>,
                private val callerContext: CoroutineContext,
                private val body: suspend () -> Unit,
                private val cancelConflicts: Boolean,
                private val continuation: CancellableContinuation<Unit>
        ) : Message {
            override fun handle() {
                // do nothing if currently disabled
                if (!isEnabled) return

                // subsystems required by use calls being used in calling coroutine
                val prevResources: Set<Resource> = callerContext[Requirements] ?: emptySet()
                // newly required subsystems (not previously required)
                val newResources = resources - prevResources
                // all subsystems
                val allResources = resources + prevResources

                // find conflicts
                val conflictingHandles = newResources.filter { it in cache }

                if (!cancelConflicts && conflictingHandles.isNotEmpty()) {
                    continuation.resumeWithException(CancellationException("Action not allowed to cancel conflicts" +
                            "{ ${conflictingHandles.joinToString { it.name }} }"))
                    return
                }

                val conflictingJobs = conflictingHandles.map { cache[it]!! }

                // spawn action coroutine
                val actionJob = launch(callerContext + Requirements(allResources), CoroutineStart.ATOMIC) {
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
                        body()

                        // resume calling coroutine
                        continuation.resume(Unit)
                    } catch (exception: Throwable) {
                        // pass exception to calling coroutine
                        continuation.resumeWithException(exception)
                    } finally {
                        // tell the scheduler that the action job has finished executing
                        channel.offer(Message.Clean(allResources, coroutineContext[Job]!!))
                    }
                }

                // write over state - future actions that use these resources will
                // cancel and wait for the action job to complete
                allResources.forEach { cache[it] = actionJob }

                // launch watchdog coroutine
                launch(MeanlibContext) {
                    var i = 0
                    while (!actionJob.isCompleted) {
                        if (actionJob.isCancelled) {
                            if (i > 0) reportError("Action job in thread ${Thread.currentThread().name}" +
                                    "hanging up subsystems { ${newResources.joinToString { it.name }} } " +
                                    "(${i * 2.5}s)", false)
                            i++
                        }

                        delay(2500)
                    }
                }
            }
        }

        class RegisterSubsystem(private val resource: Resource) : Message {
            override fun handle() {
                registry.add(resource)

                if (isEnabled && !cache.containsKey(resource)) resource.launchDefaultAction()
            }
        }

        data class Clean(val resources: Iterable<Resource>, val job: Job) : Message {
            override fun handle() {
                resources.filter { cache[it] === job }
                        .forEach {
                            cache.remove(it)
                            if (isEnabled) it.launchDefaultAction()
                        }
            }
        }
    }

    private class Requirements(
            requirements: Set<Resource>
    ) : Set<Resource> by requirements, AbstractCoroutineContextElement(Key) {
        companion object Key : CoroutineContext.Key<Requirements>
    }
}

suspend fun use(vararg resources: Resource, cancelConflicts: Boolean = true, body: suspend () -> Unit) =
        EventHandler.useResources(resources, cancelConflicts, body)
