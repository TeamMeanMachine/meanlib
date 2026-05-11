package org.team2471.frc.lib.control

import org.team2471.frc.lib.util.isSim
import org.wpilib.command3.button.CommandNiDsXboxController
import org.wpilib.driverstation.DriverStation
import org.wpilib.driverstation.POVDirection

/** Sometimes the sim GUI doesn't detect an Xbox controller as a gamepad and does not bind it as such. [simBeingDumb] attempts to rebind the joystick as if the "map gamepad" button was pressed. */
class MeanCommandXboxController(port: Int, val simBeingDumb: Boolean = false): CommandNiDsXboxController(port) {
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

/** Extension functions for raw button values. */

inline val CommandNiDsXboxController.a: Boolean get() = this.hid.aButton
inline val CommandNiDsXboxController.b: Boolean get() = this.hid.bButton
inline val CommandNiDsXboxController.x: Boolean get() = this.hid.xButton
inline val CommandNiDsXboxController.y: Boolean get() = this.hid.yButton

inline val CommandNiDsXboxController.rightBumper: Boolean get() = this.hid.rightBumperButton
inline val CommandNiDsXboxController.leftBumper: Boolean get() = this.hid.leftBumperButton

inline val CommandNiDsXboxController.start: Boolean get() = this.hid.startButton
inline val CommandNiDsXboxController.back: Boolean get() = this.hid.backButton

inline val CommandNiDsXboxController.rightStickButton: Boolean get() = this.hid.rightStickButton
inline val CommandNiDsXboxController.leftStickButton: Boolean get() = this.hid.leftStickButton

inline val CommandNiDsXboxController.dPad: POVDirection get() = this.hid.pov
