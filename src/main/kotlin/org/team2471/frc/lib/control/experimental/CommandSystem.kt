package org.team2471.frc.lib.control.experimental

import edu.wpi.first.wpilibj.Timer
import edu.wpi.first.wpilibj.Utility
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import java.util.concurrent.TimeUnit

abstract class Subsystem {
    private var action: Action? = null
    @Volatile internal var isAcquired = false
    protected open val defaultAction: Action? = null

    init {
        launch {
            var startTime = Utility.getFPGATime()
            while(true) {
                run()
                val time = Utility.getFPGATime()
                delay((action?.period ?: 20) * 1000 - (time - startTime), TimeUnit.NANOSECONDS)
                action?.useDt((time - startTime).toInt())
                startTime = time
            }
        }
    }

    @Synchronized
    private fun run() {
        if (action == null && defaultAction != null && !isAcquired) changeAction(defaultAction!!)
        else if (action == null) return

        action!!.run()
        if (action!!.isFinished) reset()
    }

    @Synchronized
    fun changeAction(action: Action) {
        this.action?.stop()
        action.start()
        action.isRunning = true
        this.action = action
    }

    @Synchronized
    fun reset() {
        action?.stop()
        action?.isRunning = false
        action = null
    }
}

abstract class Action(internal val subsystem: Subsystem, internal val period: Int = 20) {
    @Volatile internal var isRunning = false

    open internal fun start() = Unit

    open internal fun run() = Unit

    abstract internal val isFinished: Boolean

    open internal fun stop() = Unit

    operator fun invoke() = subsystem.changeAction(this)

    open fun useDt(dt: Int) = Unit
}

class Command(vararg requirements: Subsystem, private val body: suspend Command.Body.() -> Unit) {
    private val subsystems = hashSetOf(*requirements)

    class Body internal constructor() {
        suspend fun execute(vararg actions: Action,
                            terminateCondition: () -> Boolean = { actions.all { !it.isRunning } }) {
            actions.forEach { it() }

            while (!terminateCondition()) delay(20)
            actions.forEach { it.subsystem.reset() }
        }

        suspend fun executeWithTimeout(vararg actions: Action, timeout: Double) {
            val startTime = Timer.getFPGATimestamp()
            execute(*actions) { Timer.getFPGATimestamp() - startTime > timeout }
        }
    }

    operator fun invoke() = launch {
        synchronized(this) {
            subsystems.forEach { it.isAcquired = true; it.reset() }
            body(Body())
            subsystems.forEach { it.isAcquired = false }
            subsystems.clear()
        }
    }
}
