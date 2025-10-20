package org.team2471.frc.lib.control

import edu.wpi.first.wpilibj2.command.button.CommandXboxController
import org.team2471.frc.lib.util.isSim

/** Sometimes the sim GUI doesn't detect an Xbox controller as a gamepad and does not bind it as such. [simBeingDumb] attempts to rebind the joystick as if the "map gamepad" button was pressed. */
class MeanCommandXboxController(port: Int, val simBeingDumb: Boolean = false): CommandXboxController(port) {
    override fun getRightX(): Double {
        if (simBeingDumb && isSim) {
            return super.leftTriggerAxis
        }
        return super.getRightX()
    }

    override fun getLeftTriggerAxis(): Double {
        if (simBeingDumb && isSim) {
            return super.rightX
        }
        return super.leftTriggerAxis
    }
}

inline val CommandXboxController.a: Boolean get() = this.hid.aButton
inline val CommandXboxController.b: Boolean get() = this.hid.bButton
inline val CommandXboxController.x: Boolean get() = this.hid.xButton
inline val CommandXboxController.y: Boolean get() = this.hid.yButton

inline val CommandXboxController.rightBumper: Boolean get() = this.hid.rightBumperButton
inline val CommandXboxController.leftBumper: Boolean get() = this.hid.leftBumperButton

inline val CommandXboxController.start: Boolean get() = this.hid.startButton
inline val CommandXboxController.back: Boolean get() = this.hid.backButton

inline val CommandXboxController.rightStickButton: Boolean get() = this.hid.rightStickButton
inline val CommandXboxController.leftStickButton: Boolean get() = this.hid.leftStickButton

inline val CommandXboxController.dPad: Direction get() = when (this.hid.pov) {
    -1 -> Direction.IDLE
    0 -> Direction.UP
    45 -> Direction.UP_RIGHT
    90 -> Direction.RIGHT
    135 -> Direction.DOWN_RIGHT
    180 -> Direction.DOWN
    225 -> Direction.DOWN_LEFT
    270 -> Direction.LEFT
    315 -> Direction.UP_LEFT
    else -> throw IllegalStateException("Invalid DPAD value ${this.hid.pov}")
}

enum class Direction { IDLE, UP, UP_RIGHT, RIGHT, DOWN_RIGHT, DOWN, DOWN_LEFT, LEFT, UP_LEFT }

