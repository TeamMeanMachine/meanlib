package org.team2471.frc.lib.framework

import edu.wpi.first.wpilibj.XboxController

// A
fun MapperScope<XboxController>.aPress(body: suspend () -> Unit) =
    buttonPress(1, body)

fun MapperScope<XboxController>.aToggle(body: suspend () -> Unit) =
    buttonToggle(1, body)

fun MapperScope<XboxController>.aHold(body: suspend () -> Unit) =
    buttonHold(1, body)


// B
fun MapperScope<XboxController>.bPress(body: suspend () -> Unit) =
    buttonPress(2, body)

fun MapperScope<XboxController>.bToggle(body: suspend () -> Unit) =
    buttonToggle(2, body)

fun MapperScope<XboxController>.bHold(body: suspend () -> Unit) =
    buttonHold(2, body)


// X
fun MapperScope<XboxController>.xPress(body: suspend () -> Unit) =
    buttonPress(3, body)

fun MapperScope<XboxController>.xToggle(body: suspend () -> Unit) =
    buttonToggle(3, body)

fun MapperScope<XboxController>.xHold(body: suspend () -> Unit) =
    buttonHold(3, body)


// Y
fun MapperScope<XboxController>.yPress(body: suspend () -> Unit) =
    buttonPress(4, body)

fun MapperScope<XboxController>.yToggle(body: suspend () -> Unit) =
    buttonToggle(4, body)

fun MapperScope<XboxController>.yHold(body: suspend () -> Unit) =
    buttonHold(4, body)


// Left Bumper
fun MapperScope<XboxController>.leftBumperPress(body: suspend () -> Unit) =
    buttonPress(5, body)

fun MapperScope<XboxController>.leftBumperToggle(body: suspend () -> Unit) =
    buttonToggle(5, body)

fun MapperScope<XboxController>.leftBumperHold(body: suspend () -> Unit) =
    buttonHold(5, body)


// Right Bumper
fun MapperScope<XboxController>.rightBumperPress(body: suspend () -> Unit) =
    buttonPress(6, body)

fun MapperScope<XboxController>.rightBumperToggle(body: suspend () -> Unit) =
    buttonToggle(6, body)

fun MapperScope<XboxController>.rightBumperHold(body: suspend () -> Unit) =
    buttonHold(6, body)


// Back
fun MapperScope<XboxController>.backPress(body: suspend () -> Unit) =
    buttonPress(7, body)

fun MapperScope<XboxController>.backToggle(body: suspend () -> Unit) =
    buttonToggle(7, body)

fun MapperScope<XboxController>.backHold(body: suspend () -> Unit) =
    buttonHold(7, body)


// Start
fun MapperScope<XboxController>.startPress(body: suspend () -> Unit) =
    buttonPress(8, body)

fun MapperScope<XboxController>.startToggle(body: suspend () -> Unit) =
    buttonToggle(8, body)

fun MapperScope<XboxController>.startHold(body: suspend () -> Unit) =
    buttonHold(8, body)

// Right Thumbstick
fun MapperScope<XboxController>.leftThumbstickPress(body: suspend () -> Unit) =
    buttonPress(9, body)

fun MapperScope<XboxController>.leftThumbstickToggle(body: suspend () -> Unit) =
    buttonToggle(9, body)

fun MapperScope<XboxController>.leftThumbstickHold(body: suspend () -> Unit) =
    buttonHold(9, body)

// Left Thumbstick
fun MapperScope<XboxController>.rightThumbstickPress(body: suspend () -> Unit) =
    buttonPress(10, body)

fun MapperScope<XboxController>.rightThumbstickToggle(body: suspend () -> Unit) =
    buttonToggle(10, body)

fun MapperScope<XboxController>.rightThumbstickHold(body: suspend () -> Unit) =
    buttonHold(10, body)
