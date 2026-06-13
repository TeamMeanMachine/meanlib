package org.team2471.frc.lib.control

import org.wpilib.command3.button.CommandXboxController
import org.wpilib.driverstation.POVDirection
import org.wpilib.driverstation.XboxController

/** Extension functions for raw button values. */

inline val CommandXboxController.aButton: Boolean get() = this.controller.aButton
inline val CommandXboxController.bButton: Boolean get() = this.controller.bButton
inline val CommandXboxController.xButton: Boolean get() = this.controller.xButton
inline val CommandXboxController.yButton: Boolean get() = this.controller.yButton

inline val CommandXboxController.rightBumperButton: Boolean get() = this.controller.rightBumperButton
inline val CommandXboxController.leftBumperButton: Boolean get() = this.controller.leftBumperButton

/** Value of the 'view' button (overlapping squares). Old Xbox controllers called this the 'start' button */
inline val CommandXboxController.viewButton: Boolean get() = this.controller.viewButton
/** Value of the 'menu' button (3 lines). Old Xbox controllers called this the 'back' button */
inline val CommandXboxController.menuButton: Boolean get() = this.controller.menuButton

inline val CommandXboxController.xboxButton: Boolean get() = this.controller.xboxButton

/** Value of the [viewButton] button (overlapping squares), this is just a syntax rename for what we are used to. Old Xbox controllers called this the 'start' button */
inline val CommandXboxController.startButton: Boolean get() = this.viewButton
/** Value of the [menuButton] button (3 lines), this is just a syntax rename for what we are used to. Old Xbox controllers called this the 'back' button */
inline val CommandXboxController.backButton: Boolean get() = this.menuButton

inline val CommandXboxController.rightStickButton: Boolean get() = this.controller.rightStickButton
inline val CommandXboxController.leftStickButton: Boolean get() = this.controller.leftStickButton

/** Gets the value of the + pov button (dpad) on the controller. */
inline val CommandXboxController.dpad: POVDirection get() = this.hid.hid.pov


inline val CommandXboxController.isConnected: Boolean get() = this.controller.isConnected
