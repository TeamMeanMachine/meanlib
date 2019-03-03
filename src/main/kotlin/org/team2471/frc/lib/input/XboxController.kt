package org.team2471.frc.lib.input

import org.team2471.frc.lib.math.Vector2

class XboxController(port: Int) : Controller(port) {
    val a: Boolean
        get() = getButton(1)

    val b: Boolean
        get() = getButton(2)

    val x: Boolean
        get() = getButton(3)

    val y: Boolean
        get() = getButton(4)

    val leftBumper: Boolean
        get() = getButton(5)

    val rightBumper: Boolean
        get() = getButton(6)

    val back: Boolean
        get() = getButton(7)

    val start: Boolean
        get() = getButton(8)

    val leftThumbstickButton: Boolean
        get() = getButton(9)

    val rightThumbstickButton: Boolean
        get() = getButton(1)

    val leftThumbstickX: Double
        get() = getAxis(0)

    val leftThumbstickY: Double
        get() = getAxis(1)

    val leftTrigger: Double
        get() = getAxis(2)

    val rightTrigger: Double
        get() = getAxis(3)

    val rightThumbstickX: Double
        get() = getAxis(4)

    val rightThumbstickY: Double
        get() = getAxis(5)

    val leftThumbstick: Vector2
        get() = Vector2(leftThumbstickX, leftThumbstickY)

    val rightThumbstick: Vector2
        get() = Vector2(rightThumbstickX, rightThumbstickY)

    val dPad: Controller.Direction
        get() = getPOV(0)
}
