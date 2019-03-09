package org.team2471.frc.lib.input

class Joystick(port: Int) : Controller(port) {
    val x: Double
        get() = getAxis(0)

    val y: Double
        get() = getAxis(1)

    val z: Double
        get() = getAxis(2)

    val pov: Controller.Direction
        get() = getPOV(0)
}
