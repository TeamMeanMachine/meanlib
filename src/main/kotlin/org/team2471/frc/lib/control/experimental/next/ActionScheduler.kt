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
    private val channel = actor<Message>(capacity = Channel.UNLIMITED) {
        val cache = hashMapOf<SubsystemLock, Job>()
        val knownHandles = hashSetOf<SubsystemLock>()

        messageLoop@ for (message in channel) {
            // process newest message in queue
            when (message) {
                // ActionScheduler.enable invoked
                is Message.Enable -> {
                    // do nothing if already enabled
                    if (isEnabled) continue@messageLoop

                    // put all subsystems into default states
                    knownHandles.forEach { it.launchDefaultAction() }

                    // update state
                    isEnabled = true
                }

                // ActionScheduler.disable invoked
                is Message.Disable -> {
                    // do nothing if already disabled
                    if (!isEnabled) continue@messageLoop

                    // cancel all running actions
                    val e = CancellationException("Subsystem Coordinator Disabled")
                    cache.forEach { (_, job) -> job.cancel(e) }

                    // update state
                    isEnabled = false
                }

                // ActionScheduler.use invoked
                is Message.NewAction -> {
                    // do nothing if currently disabled
                    if (!isEnabled) continue@messageLoop

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
                        continue@messageLoop
                    }

                    val conflictingJobs = conflictingHandles.map { cache[it]!! }

                    // spawn action coroutine and suspend scheduler until a Job can be retrieved
                    val actionJob = suspendCoroutine<Job> { launchCont ->
                        launch(callerContext + CoroutineRequirements(allHandles), CoroutineStart.ATOMIC) {
                            val actionContext = coroutineContext

                            // coroutines created with `launch` always have a Job in
                            // it's context
                            val job = actionContext[Job]!!

                            // kick back new job so the coordinator can have something
                            // to cancel later if necessary
                            launchCont.resume(job)

                            try {
                                // cancel conflicts - action coroutines must not complete until
                                // it's conflict's jobs have finished execution
                                withContext(NonCancellable) {
                                    val e = TakeoverException(conflictingHandles)
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
                                channel.offer(Message.Clean(allHandles, job))
                            }
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

                is Message.RegisterSubsystem -> {
                    val lock = message.lock
                    knownHandles.add(lock)

                    if (isEnabled) lock.launchDefaultAction()
                }

                is Message.Clean -> {
                    val (locks, job) = message
                    locks.filter { cache[it] === job }
                            .forEach {
                                cache.remove(it)
                                if (isEnabled) it.launchDefaultAction()
                            }
                }
            }
        }
    }

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

    internal fun register(subsystemLock: SubsystemLock) {
        channel.offer(Message.RegisterSubsystem(subsystemLock))
    }

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

    private class TakeoverException(conflicts: Iterable<SubsystemLock>)
        : CancellationException("Coroutine takeover by subsystem locks " +
            "{ ${conflicts.joinToString { it.name }} }")

    private class CoroutineRequirements(
            requirements: Set<SubsystemLock>
    ) : Set<SubsystemLock> by requirements, AbstractCoroutineContextElement(Key) {
        companion object Key : CoroutineContext.Key<CoroutineRequirements>
    }
}
