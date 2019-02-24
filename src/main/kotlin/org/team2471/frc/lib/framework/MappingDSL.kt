package org.team2471.frc.lib.framework

import edu.wpi.first.wpilibj.GenericHID
import kotlinx.coroutines.CoroutineScope
import org.team2471.frc.lib.framework.internal.InputMapper

class MapperScope<C : GenericHID>(internal val controller: C) {
    /**
     * Executes [body] when [button] is pressed.
     */
    fun buttonPress(button: Int, body: suspend CoroutineScope.() -> Unit) =
        InputMapper.setPressBinding(controller, button, body)

    /**
     * Toggles the execution of [body] for each press of [button]. When toggled off, [body] is
     * cancelled.
     */
    fun buttonToggle(button: Int, body: suspend CoroutineScope.() -> Unit) =
        InputMapper.setToggleBinding(controller, button, body)

    /**
     * Executes [body] while [button] is pressed. When released, [body] is cancelled.
     */
    fun buttonHold(button: Int, body: suspend CoroutineScope.() -> Unit) =
        InputMapper.setHoldBinding(controller, button, body)
}

/**
 * Create mappings for the specified [GenericHID].
 */
fun <C : GenericHID> C.createMappings(body: MapperScope<C>.() -> Unit) {
    body(MapperScope(this))
}
