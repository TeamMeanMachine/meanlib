package org.team2471.frc.lib.resources

import edu.wpi.first.wpilibj.XboxController
import kotlinx.coroutines.CoroutineScope

// Xbox controller extensions
fun MapperScope<XboxController>.a(body: suspend CoroutineScope.() -> Unit) =
        InputMapper.setMapping(controller, 1, body)

fun MapperScope<XboxController>.b(body: suspend CoroutineScope.() -> Unit) =
        InputMapper.setMapping(controller, 2, body)

fun MapperScope<XboxController>.x(body: suspend CoroutineScope.() -> Unit) =
        InputMapper.setMapping(controller, 3, body)

fun MapperScope<XboxController>.y(body: suspend CoroutineScope.() -> Unit) =
        InputMapper.setMapping(controller, 4, body)

fun MapperScope<XboxController>.leftBumper(body: suspend CoroutineScope.() -> Unit) =
        InputMapper.setMapping(controller, 5, body)

fun MapperScope<XboxController>.rightBumper(body: suspend CoroutineScope.() -> Unit) =
        InputMapper.setMapping(controller, 6, body)

fun MapperScope<XboxController>.back(body: suspend CoroutineScope.() -> Unit) =
        InputMapper.setMapping(controller, 7, body)

fun MapperScope<XboxController>.start(body: suspend CoroutineScope.() -> Unit) =
        InputMapper.setMapping(controller, 8, body)

fun MapperScope<XboxController>.leftThumbstick(body: suspend CoroutineScope.() -> Unit) =
        InputMapper.setMapping(controller, 9, body)

fun MapperScope<XboxController>.rightThumbstick(body: suspend CoroutineScope.() -> Unit) =
        InputMapper.setMapping(controller, 10, body)
