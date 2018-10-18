package org.team2471.frc.lib.control.experimental.next

import edu.wpi.first.wpilibj.GenericHID
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.launch

internal object InputMapper {
    private val bindings = hashMapOf<Mapping, suspend CoroutineScope.() -> Unit>()

    fun setMapping(controller: GenericHID, button: Int, body: suspend CoroutineScope.() -> Unit) {
        bindings[Mapping(controller, button)] = body
    }

    fun process() {
        bindings.forEach { (controller, button), body ->
            if (controller.getRawButtonPressed(button)) {
                launch(MeanlibContext, block = body)
            }
        }
    }

    private data class Mapping(val controller: GenericHID, val button: Int)
}

class MapperScope<C : GenericHID>(internal val controller: C) {
    fun button(button: Int, body: suspend CoroutineScope.() -> Unit) = InputMapper.setMapping(controller, button, body)
}

fun <C : GenericHID> createMappings(controller: C, body: MapperScope<C>.() -> Unit) {
    body(MapperScope(controller))
}
