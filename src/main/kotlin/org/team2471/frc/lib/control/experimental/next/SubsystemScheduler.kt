package org.team2471.frc.lib.control.experimental.next

import edu.wpi.first.wpilibj.DriverStation.reportError
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.actor
import kotlin.coroutines.experimental.AbstractCoroutineContextElement
import kotlin.coroutines.experimental.CoroutineContext
import kotlin.coroutines.experimental.coroutineContext
import kotlin.coroutines.experimental.suspendCoroutine

object SubsystemScheduler {
    private val channel = actor<Message>(capacity = Channel.UNLIMITED) {
        val cache = hashMapOf<SubsystemHandle, Job>()
        val knownHandles = hashSetOf<SubsystemHandle>()

        // process incoming messages
        messageLoop@ for (message in channel) {
            when (message) {
                is Message.Enable -> {
                    if (isEnabled) continue@messageLoop

                    knownHandles.forEach { it.launchDefaultAction() }
                    isEnabled = true
                }

                is Message.Disable -> {
                    if (!isEnabled) continue@messageLoop

                    CancellationException("Subsystem Coordinator Disabled").let { e ->
                        cache.map { (_, job) -> job.cancel(e); job }
                    }.forEach { it.join() }

                    isEnabled = false
                }

                is Message.NewAction -> {
                    if (!isEnabled) continue@messageLoop

                    val (handles, callerContext, body, cancelConflicts, continuation) = message

                    val previousHandles: Set<SubsystemHandle> = callerContext[CoroutineRequirements] ?: emptySet()
                    val newHandles = handles - previousHandles
                    val allHandles = handles + previousHandles

                    // find conflicting jobs
                    val conflicts = newHandles.filter { it in cache }

                    if (!cancelConflicts && conflicts.isNotEmpty()) {
                        val e = CancellationException("Action not allowed to cancel conflicts" +
                                "{ ${conflicts.joinToString { it.name }} }")
                        continuation.resumeWithException(e)
                        continue@messageLoop
                    }

                    val conflictJobs = conflicts.map { cache[it]!! }

                    val newJob = suspendCoroutine<Job> { launchCont ->
                        // launch new action coroutine with all handles applied
                        launch(callerContext + CoroutineRequirements(allHandles),
                                CoroutineStart.ATOMIC) {

                            val actionContext = coroutineContext

                            // coroutines created with `launch` always have a Job in
                            // it's context
                            val job = actionContext[Job]!!

                            // kick back new job so the coordinator can have something
                            // to cancel later if necessary
                            launchCont.resume(job)

                            try {
                                // cancel conflicts - action coroutines must not complete until
                                // it's conflict jobs have finished execution
                                withContext(NonCancellable) {
                                    val e = TakeoverException(conflicts)
                                    conflictJobs.forEach { it.cancel(e) }
                                    conflictJobs.forEach { it.join() }
                                }

                                body()
                                continuation.resume(Unit)
                            } catch (exception: Throwable) {
                                continuation.resumeWithException(exception)
                            } finally {
                                channel.offer(Message.Clean(job))
                            }
                        }
                    }

                    // write over state
                    allHandles.forEach { cache[it] = newJob }

                    // launch watchdog coroutine
                    launch(MeanlibContext) {
                        var i = 0
                        while (!newJob.isCompleted) {
                            if (newJob.isCancelled) {
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
                    val handle = message.handle
                    knownHandles.add(handle)

                    if (isEnabled) handle.launchDefaultAction()
                }

                is Message.Clean -> {
                    // grab currently used handles
                    cache.keys
                            .filter { cache[it] === message.job }
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
            vararg subsystemHandles: SubsystemHandle,
            cancelConflicts: Boolean = true,
            body: suspend () -> Unit
    ) {
        val context = coroutineContext

        suspendCancellableCoroutine<Unit> { cont ->
            val message = Message.NewAction(setOf(*subsystemHandles), context, body, cancelConflicts, cont)
            channel.offer(message)
        }
    }

    internal fun register(subsystemHandle: SubsystemHandle) {
        channel.offer(Message.RegisterSubsystem(subsystemHandle))
    }

    private sealed class Message {
        object Enable : Message()

        object Disable : Message()

        data class NewAction(
                val handles: Set<SubsystemHandle>,
                val coroutineContext: CoroutineContext,
                val body: suspend () -> Unit,
                val cancelConflicts: Boolean,
                val continuation: CancellableContinuation<Unit>
        ) : Message()

        class RegisterSubsystem(val handle: SubsystemHandle) : Message()

        class Clean(val job: Job) : Message()
    }

    private class TakeoverException(conflicts: Iterable<SubsystemHandle>)
        : CancellationException("Coroutine takeover by subsystem handles " +
            "{ ${conflicts.joinToString { it.name }} }")

    private class CoroutineRequirements(
            requirements: Set<SubsystemHandle>
    ) : Set<SubsystemHandle> by requirements, AbstractCoroutineContextElement(Key) {
        companion object Key : CoroutineContext.Key<CoroutineRequirements>
    }
}
