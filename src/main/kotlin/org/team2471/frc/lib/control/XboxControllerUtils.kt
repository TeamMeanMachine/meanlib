package org.team2471.frc.lib.control

import org.team2471.frc.lib.util.isSim
import org.wpilib.command3.Trigger
import org.wpilib.command3.button.CommandGamepad
import org.wpilib.command3.button.CommandNiDsXboxController
import org.wpilib.driverstation.DriverStation
import org.wpilib.driverstation.POVDirection

/** Sometimes the sim GUI doesn't detect an Xbox controller as a gamepad and does not bind it as such. [simBeingDumb] attempts to rebind the joystick as if the "map gamepad" button was pressed. */
class MeanCommandXboxController(port: Int, val simBeingDumb: Boolean = false): CommandGamepad(port) {
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

    fun y(): Trigger = this.northFace()
    fun b(): Trigger = this.eastFace()
    fun a(): Trigger = this.southFace()
    fun x(): Trigger = this.westFace()
}

/** Extension functions for raw button values. */

inline val CommandGamepad.a: Boolean get() = this.hid.southFaceButton
inline val CommandGamepad.b: Boolean get() = this.hid.eastFaceButton
inline val CommandGamepad.x: Boolean get() = this.hid.westFaceButton
inline val CommandGamepad.y: Boolean get() = this.hid.northFaceButton

inline val CommandGamepad.rightBumper: Boolean get() = this.hid.rightBumperButton
inline val CommandGamepad.leftBumper: Boolean get() = this.hid.leftBumperButton

inline val CommandGamepad.start: Boolean get() = this.hid.startButton
inline val CommandGamepad.back: Boolean get() = this.hid.backButton

inline val CommandGamepad.rightStickButton: Boolean get() = this.hid.rightStickButton
inline val CommandGamepad.leftStickButton: Boolean get() = this.hid.leftStickButton

inline val CommandGamepad.dPad: POVDirection get() = this.hid.pov
