package org.team2471.frc.lib.framework

import kotlinx.coroutines.*
import org.team2471.frc.lib.coroutines.MeanlibDispatcher

object Events {
    private val functions = hashSetOf<() -> Unit>()

    @Synchronized
    internal fun process() = functions.forEach { it() }

    @OptIn(DelicateCoroutinesApi::class)
    @Synchronized
    fun whenActive(condition: () -> Boolean, action: suspend () -> Unit) {
        var prevState = false
        var job: Job? = null

        functions.add {
            val state = condition()

            if (state && !prevState) {
                val prevJob = job

                job = GlobalScope.launch(MeanlibDispatcher) {
                    prevJob?.cancelAndJoin()
                    action()
                }
            }

            prevState = state
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    @Synchronized
    fun whileActive(condition: () -> Boolean, action: suspend () -> Unit) {
        var prevState = false
        var job: Job? = null

        functions.add {
            val state = condition()

            if (state && !prevState) {
                val prevJob = job

                job = GlobalScope.launch(MeanlibDispatcher) {
                    prevJob?.cancelAndJoin()
                    action()
                }
            } else if (!state && prevState) {
                job?.cancel()
            }

            prevState = state
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    @Synchronized
    fun toggleWhenActive(condition: () -> Boolean, action: suspend () -> Unit) {
        var prevState = false
        var job: Job? = null

        functions.add {
            val state = condition()

            val prevJob = job
            if (state && !prevState) {
                if (prevJob != null && prevJob.isActive) {
                    job!!.cancel()
                } else {
                    job = GlobalScope.launch(MeanlibDispatcher) {
                        prevJob?.join()
                        action()
                    }
                }
            }

            prevState = state
        }
    }
}