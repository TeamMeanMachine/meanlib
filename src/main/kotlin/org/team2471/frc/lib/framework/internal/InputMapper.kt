package org.team2471.frc.lib.framework.internal

import edu.wpi.first.wpilibj.GenericHID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.team2471.frc.lib.coroutines.MeanlibDispatcher

internal object InputMapper {
    private val bindings = hashMapOf<Mapping, Binding>()

    @Synchronized
    fun setPressBinding(controller: GenericHID, button: Int, body: suspend CoroutineScope.() -> Unit) {
        bindings[Mapping(controller, button)] = Binding.Press(body)
    }

    @Synchronized
    fun setToggleBinding(controller: GenericHID, button: Int, body: suspend CoroutineScope.() -> Unit) {
        bindings[Mapping(controller, button)] = Binding.Toggle(body)
    }

    @Synchronized
    fun setHoldBinding(controller: GenericHID, button: Int, body: suspend CoroutineScope.() -> Unit) {
        bindings[Mapping(controller, button)] = Binding.Hold(body)
    }

    @Synchronized
    fun process() = bindings.forEach { mapping, binding -> binding.process(mapping) }

    private data class Mapping(val controller: GenericHID, val button: Int)

    private sealed class Binding {
        abstract fun process(mapping: Mapping)

        class Press(
            private val body: suspend CoroutineScope.() -> Unit
        ) : Binding() {
            override fun process(mapping: Mapping) {
                if (mapping.controller.getRawButtonPressed(mapping.button)) {
                    GlobalScope.launch(MeanlibDispatcher, block = body)
                }
            }
        }

        class Toggle(
            private val body: suspend CoroutineScope.() -> Unit
        ) : Binding() {
            private var prevJob: Job? = null

            override fun process(mapping: Mapping) {
                if (mapping.controller.getRawButtonPressed(mapping.button)) {
                    prevJob = if (prevJob == null || prevJob!!.isCompleted) {
                        GlobalScope.launch(MeanlibDispatcher, block = body)
                    } else {
                        prevJob?.cancel()
                        null
                    }
                }
            }
        }

        class Hold(
            private val body: suspend CoroutineScope.() -> Unit
        ) : Binding() {
            private var prevJob: Job? = null

            override fun process(mapping: Mapping) {
                val buttonState = mapping.controller.getRawButton(mapping.button)

                if (buttonState && prevJob == null) {
                    prevJob = GlobalScope.launch(MeanlibDispatcher, block = body)
                } else if (!buttonState && prevJob != null) {
                    prevJob!!.cancel()
                    prevJob = null
                }
            }
        }
    }
}

