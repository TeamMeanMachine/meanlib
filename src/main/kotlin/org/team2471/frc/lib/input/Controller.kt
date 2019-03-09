package org.team2471.frc.lib.input

import edu.wpi.first.wpilibj.DriverStation
import java.lang.IllegalStateException

private val ds = DriverStation.getInstance()

open class Controller(val port: Int) {
    val isXbox: Boolean
        get() = ds.getJoystickIsXbox(port)

    fun getButton(button: Int) = ds.getStickButton(port, button)

    fun getAxis(axis: Int) = ds.getStickAxis(port, axis)

    fun getPOV(pov: Int = 0) = ds.getStickPOV(port, pov).let { value ->
        when (value) {
            -1 -> Direction.IDLE
            0 -> Direction.UP
            45 -> Direction.UP_RIGHT
            90 -> Direction.RIGHT
            135 -> Direction.DOWN_RIGHT
            180 -> Direction.DOWN
            225 -> Direction.DOWN_LEFT
            270 -> Direction.LEFT
            315 -> Direction.UP_LEFT
            else -> throw IllegalStateException("Invalid DPAD value $value")
        }
    }

    enum class Direction { IDLE, UP, UP_RIGHT, RIGHT, DOWN_RIGHT, DOWN, DOWN_LEFT, LEFT, UP_LEFT }
}