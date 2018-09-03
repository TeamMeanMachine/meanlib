package org.team2471.frc.lib.control.experimental.next

import edu.wpi.first.wpilibj.DriverStation.reportError
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.actor
import kotlin.coroutines.experimental.AbstractCoroutineContextElement
import kotlin.coroutines.experimental.CoroutineContext
import kotlin.coroutines.experimental.coroutineContext
import kotlin.coroutines.experimental.suspendCoroutine

object ActionScheduler {
    val cache = hashMapOf<SubsystemLock, Job>()
    val knownHandles = hashSetOf<SubsystemLock>()

    private val channel = Channel<Message>(Channel.UNLIMITED)

    init {
        // spawn scheduler coroutine
        launch(MeanlibContext) {
            // handle incoming messages as they arrive
            for (message in channel) {
                when (message) {
                    is Message.Enable -> handleEnableMessage()
                    is Message.Disable -> handleDisableMessage()
                    is Message.NewAction -> handleNewActionMessage(message)
                    is Message.RegisterSubsystem -> handleRegisterSubsystemMessage(message)
                    is Message.Clean -> handleCleanMessage(message)
                }
            }
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

    suspend fun use(
            vararg subsystemLocks: SubsystemLock,
            cancelConflicts: Boolean = true,
            body: suspend () -> Unit
    ) {
        val context = coroutineContext

        suspendCancellableCoroutine<Unit> { cont ->
            val message = Message.NewAction(setOf(*subsystemLocks), context, body, cancelConflicts, cont)
            channel.offer(message)
        }
    }

    /*
     * INTERNAL API
     */

    internal fun register(subsystemLock: SubsystemLock) {
        channel.offer(Message.RegisterSubsystem(subsystemLock))
    }

    /*
     * MESSAGE HANDLING
     */

    private sealed class Message {
        object Enable : Message()

        object Disable : Message()

        data class NewAction(
                val locks: Set<SubsystemLock>,
                val coroutineContext: CoroutineContext,
                val body: suspend () -> Unit,
                val cancelConflicts: Boolean,
                val continuation: CancellableContinuation<Unit>
        ) : Message()

        class RegisterSubsystem(val lock: SubsystemLock) : Message()

        data class Clean(val locks: Iterable<SubsystemLock>, val job: Job) : Message()
    }

    private fun handleEnableMessage() {
        // do nothing if already enabled
        if (isEnabled) return

        // put all subsystems into default states
        knownHandles.forEach { it.launchDefaultAction() }

        // update state
        isEnabled = true
    }

    private fun handleDisableMessage() {
        // do nothing if already disabled
        if (!isEnabled) return

        // cancel all running actions
        val e = CancellationException("Subsystem Coordinator Disabled")
        cache.forEach { (_, job) -> job.cancel(e) }

        // update state
        isEnabled = false
    }

    private fun handleNewActionMessage(message: Message.NewAction) {
        // do nothing if currently disabled
        if (!isEnabled) return

        // destructure message object
        val (locks, callerContext, body, cancelConflicts, continuation) = message

        // subsystems required by use calls being used in calling coroutine
        val previousHandles: Set<SubsystemLock> = callerContext[CoroutineRequirements] ?: emptySet()
        // newly required subsystems (not previously required)
        val newHandles = locks - previousHandles
        // all subsystems
        val allHandles = locks + previousHandles

        // find conflicts
        val conflictingHandles = newHandles.filter { it in cache }

        if (!cancelConflicts && conflictingHandles.isNotEmpty()) {
            continuation.resumeWithException(CancellationException("Action not allowed to cancel conflicts" +
                    "{ ${conflictingHandles.joinToString { it.name }} }"))
            return
        }

        val conflictingJobs = conflictingHandles.map { cache[it]!! }

        // spawn action coroutine
        val actionJob = launch(callerContext + CoroutineRequirements(allHandles), CoroutineStart.ATOMIC) {
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
                channel.offer(Message.Clean(allHandles, coroutineContext[Job]!!))
            }
        }

        // write over state - future actions that use these locks will
        // cancel and wait for the action job to complete
        allHandles.forEach { cache[it] = actionJob }

        // launch watchdog coroutine
        launch(MeanlibContext) {
            var i = 0
            while (!actionJob.isCompleted) {
                if (actionJob.isCancelled) {
                    if (i > 0) {
                        reportError("Action job in thread ${Thread.currentThread().name}" +
                                "hanging up subsystems { ${newHandles.joinToString { it.name }} } " +
                                "(${i * 2.5}s)", false)
                    }
                    i++
                }

                delay(2500)
            }
        }
    }

    private fun handleRegisterSubsystemMessage(message: Message.RegisterSubsystem) {
        val lock = message.lock
        knownHandles.add(lock)

        if (isEnabled) lock.launchDefaultAction()
    }

    private fun handleCleanMessage(message: Message.Clean) {
        val (locks, job) = message
        locks.filter { cache[it] === job }
                .forEach {
                    cache.remove(it)
                    if (isEnabled) it.launchDefaultAction()
                }
    }

    private class CoroutineRequirements(
            requirements: Set<SubsystemLock>
    ) : Set<SubsystemLock> by requirements, AbstractCoroutineContextElement(Key) {
        companion object Key : CoroutineContext.Key<CoroutineRequirements>
    }
}
