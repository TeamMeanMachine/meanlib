package org.team2471.frc.lib.framework

import edu.wpi.first.wpilibj.GenericHID
import kotlinx.coroutines.CoroutineScope

class MapperScope<C : GenericHID>(private val controller: C) {
    fun buttonPress(button: Int, body: suspend () -> Unit) =
        Events.whenActive({ controller.getRawButton(button) }, body)

    fun buttonToggle(button: Int, body: suspend () -> Unit) =
        Events.toggleWhenActive({ controller.getRawButton(button) }, body)

    fun buttonHold(button: Int, body: suspend () -> Unit) =
        Events.whenActive({ controller.getRawButton(button) }, body)
}

@Suppress("DeprecatedCallableAddReplaceWith")
@Deprecated("Will be removed after 2019.")
fun <C : GenericHID> C.createMappings(body: MapperScope<C>.() -> Unit) {
    body(MapperScope(this))
}
