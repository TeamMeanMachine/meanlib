package org.team2471.frc.lib.input

import edu.wpi.first.hal.HAL
import edu.wpi.first.wpilibj.DriverStation
import edu.wpi.first.wpilibj.Timer
import java.lang.IllegalStateException

private val ds = DriverStation.getInstance()

open class Controller(val port: Int) {
    private var lastWarningReported = 0.0

    val isXbox
        get() = ds.getJoystickIsXbox(port)

    val buttonCount
        get() = ds.getStickButtonCount(port)
    val axisCount
        get() = ds.getStickAxisCount(port)
    val povCount
        get() = ds.getStickPOVCount(port)

    val isConnected
        get() = buttonCount != 0 || axisCount != 0 || povCount != 0

    /**
     * Ensures the [Controller] is connected, and, if not, returns a [backup] value and prints a
     * warning to the driver station.
     *
     * @param backup the backup value to return in case the [Controller] is disconnected
     * @param body a lambda which returns the value if the [Controller] is connected
     */
    private fun <T>ensureConnection(backup: T, body: () -> T) = if (isConnected) {
        body()
    } else {
        val currentTime = Timer.getFPGATimestamp()
        if (ds.isEnabled && currentTime - lastWarningReported >= JOYSTICK_WARNING_INTERVAL) {
            lastWarningReported = currentTime
            DriverStation.reportWarning("Controller on port $port is disconnected", false)
        }

        backup
    }


    fun getButton(button: Int) = ensureConnection(false) { ds.getStickButton(port, button) }

    fun getAxis(axis: Int) = ensureConnection(0.0) { ds.getStickAxis(port, axis) }

    fun getPOV(pov: Int = 0) = ensureConnection(Direction.IDLE) {
        ds.getStickPOV(port, pov).let { value ->
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
    }

    var rumble: Double = 0.0
        set(value) {
            field = value
            val rumble = (value * 65535).toShort()
            HAL.setJoystickOutputs(port.toByte(), 0, rumble, rumble)
        }

    enum class Direction { IDLE, UP, UP_RIGHT, RIGHT, DOWN_RIGHT, DOWN, DOWN_LEFT, LEFT, UP_LEFT }

    companion object {
        private const val JOYSTICK_WARNING_INTERVAL = 5.0
    }
}
