package org.team2471.frc.lib.framework

import edu.wpi.first.wpilibj.GenericHID
import kotlinx.coroutines.CoroutineScope
import org.team2471.frc.lib.framework.internal.InputMapper

class MapperScope<C : GenericHID>(internal val controller: C) {
    fun buttonPress(button: Int, body: suspend CoroutineScope.() -> Unit) =
        InputMapper.setPressBinding(controller, button, body)

    fun buttonToggle(button: Int, body: suspend CoroutineScope.() -> Unit) =
        InputMapper.setToggleBinding(controller, button, body)

    fun buttonHold(button: Int, body: suspend CoroutineScope.() -> Unit) =
        InputMapper.setHoldBinding(controller, button, body)
}

fun <C : GenericHID> C.createMappings(body: MapperScope<C>.() -> Unit) {
    body(MapperScope(this))
}
