package org.team2471.frc.lib.framework

import edu.wpi.first.wpilibj.XboxController
import kotlinx.coroutines.CoroutineScope
import org.team2471.frc.lib.framework.internal.InputMapper

// A
fun MapperScope<XboxController>.aPress(body: suspend CoroutineScope.() -> Unit) =
    InputMapper.setPressBinding(controller, 1, body)

fun MapperScope<XboxController>.aToggle(body: suspend CoroutineScope.() -> Unit) =
    InputMapper.setToggleBinding(controller, 1, body)

fun MapperScope<XboxController>.aHold(body: suspend CoroutineScope.() -> Unit) =
    InputMapper.setHoldBinding(controller, 1, body)


// B
fun MapperScope<XboxController>.bPress(body: suspend CoroutineScope.() -> Unit) =
    InputMapper.setPressBinding(controller, 2, body)

fun MapperScope<XboxController>.bToggle(body: suspend CoroutineScope.() -> Unit) =
    InputMapper.setToggleBinding(controller, 2, body)

fun MapperScope<XboxController>.bHold(body: suspend CoroutineScope.() -> Unit) =
    InputMapper.setHoldBinding(controller, 2, body)


// X
fun MapperScope<XboxController>.xPress(body: suspend CoroutineScope.() -> Unit) =
    InputMapper.setPressBinding(controller, 3, body)

fun MapperScope<XboxController>.xToggle(body: suspend CoroutineScope.() -> Unit) =
    InputMapper.setToggleBinding(controller, 3, body)

fun MapperScope<XboxController>.xHold(body: suspend CoroutineScope.() -> Unit) =
    InputMapper.setHoldBinding(controller, 3, body)


// Y
fun MapperScope<XboxController>.yPress(body: suspend CoroutineScope.() -> Unit) =
    InputMapper.setPressBinding(controller, 4, body)

fun MapperScope<XboxController>.yToggle(body: suspend CoroutineScope.() -> Unit) =
    InputMapper.setToggleBinding(controller, 4, body)

fun MapperScope<XboxController>.yHold(body: suspend CoroutineScope.() -> Unit) =
    InputMapper.setHoldBinding(controller, 4, body)


// Left Bumper
fun MapperScope<XboxController>.leftBumperPress(body: suspend CoroutineScope.() -> Unit) =
    InputMapper.setPressBinding(controller, 5, body)

fun MapperScope<XboxController>.leftBumperToggle(body: suspend CoroutineScope.() -> Unit) =
    InputMapper.setToggleBinding(controller, 5, body)

fun MapperScope<XboxController>.leftBumperHold(body: suspend CoroutineScope.() -> Unit) =
    InputMapper.setHoldBinding(controller, 5, body)


// Right Bumper
fun MapperScope<XboxController>.rightBumperPress(body: suspend CoroutineScope.() -> Unit) =
    InputMapper.setPressBinding(controller, 6, body)

fun MapperScope<XboxController>.rightBumperToggle(body: suspend CoroutineScope.() -> Unit) =
    InputMapper.setToggleBinding(controller, 6, body)

fun MapperScope<XboxController>.rightBumperHold(body: suspend CoroutineScope.() -> Unit) =
    InputMapper.setHoldBinding(controller, 6, body)


// Back
fun MapperScope<XboxController>.backPress(body: suspend CoroutineScope.() -> Unit) =
    InputMapper.setPressBinding(controller, 7, body)

fun MapperScope<XboxController>.backToggle(body: suspend CoroutineScope.() -> Unit) =
    InputMapper.setToggleBinding(controller, 7, body)

fun MapperScope<XboxController>.backHold(body: suspend CoroutineScope.() -> Unit) =
    InputMapper.setHoldBinding(controller, 7, body)


// Start
fun MapperScope<XboxController>.startPress(body: suspend CoroutineScope.() -> Unit) =
    InputMapper.setPressBinding(controller, 8, body)

fun MapperScope<XboxController>.startToggle(body: suspend CoroutineScope.() -> Unit) =
    InputMapper.setToggleBinding(controller, 8, body)

fun MapperScope<XboxController>.startHold(body: suspend CoroutineScope.() -> Unit) =
    InputMapper.setHoldBinding(controller, 8, body)

// Right Thumbstick
fun MapperScope<XboxController>.leftThumbstickPress(body: suspend CoroutineScope.() -> Unit) =
    InputMapper.setPressBinding(controller, 9, body)

fun MapperScope<XboxController>.leftThumbstickToggle(body: suspend CoroutineScope.() -> Unit) =
    InputMapper.setToggleBinding(controller, 9, body)

fun MapperScope<XboxController>.leftThumbstickHold(body: suspend CoroutineScope.() -> Unit) =
    InputMapper.setHoldBinding(controller, 9, body)

// Left Thumbstick
fun MapperScope<XboxController>.rightThumbstickPress(body: suspend CoroutineScope.() -> Unit) =
    InputMapper.setPressBinding(controller, 10, body)

fun MapperScope<XboxController>.rightThumbstickToggle(body: suspend CoroutineScope.() -> Unit) =
    InputMapper.setToggleBinding(controller, 10, body)

fun MapperScope<XboxController>.rightThumbstickHold(body: suspend CoroutineScope.() -> Unit) =
    InputMapper.setHoldBinding(controller, 10, body)

