package org.team2471.frc.lib.control

import org.wpilib.command3.Trigger
import org.wpilib.command3.button.CommandXboxController
import org.wpilib.driverstation.POVDirection

/**
 * Custom extension class of a [CommandXboxController] for the Razer Wolverine controller, with custom rebinding inside the Razer software. The controller bindings should be configured accurately for this class to work.
 *
 * The two left/right "claw grip" buttons (small ones at top, M1, M2) are bound to the L/R *stick buttons*. (Buttons that activate when you press the stick in)
 *
 * The back paddles are bounded to the dpad buttons. Where:
 *
 * - Upper Left: `dpad == POVDirection.UP`
 * - Lower Left: `dpad == POVDirection.LEFT`
 * - Lower Right: `dpad == POVDirection.DOWN`
 * - Upper Right: `dpad == POVDirection.RIGHT`
 *
 * (dpad cardinal directions rotated 45° counterclockwise)
 */
class CommandRazerWolverineController(port: Int): CommandXboxController(port) {

    // BUTTON GETTERS
    /**
     * Gets the Top Left Small Button on the Razer Wolverine controller.
     *
     * (Same as [leftStickButton] since the controller bindings should be configured to activate that)
     */
    val clawGripLeftButton: Boolean get() = leftStickButton

    /**
     * Gets the Top Right Small Button on the Razer Wolverine controller.
     *
     * (Same as [rightStickButton] since the controller bindings should be configured to activate that)
     */
    val clawGripRightButton: Boolean get() = rightStickButton

    /**
     * Gets the Backside Upper Right Paddle on the Razer Wolverine controller.
     *
     * (Same as `dpad == POVDirection.RIGHT` since the controller bindings should be configured to activate that)
     */
    val upperRightPaddle: Boolean get() = dpad == POVDirection.RIGHT

    /**
     * Gets the Backside Upper Left Paddle on the Razer Wolverine controller.
     *
     * (Same as `dpad == POVDirection.UP` since the controller bindings should be configured to activate that)
     */
    val upperLeftPaddle: Boolean get() = dpad == POVDirection.UP

    /**
     * Gets the Backside Lower Right Paddle on the Razer Wolverine controller.
     *
     * (Same as `dpad == POVDirection.DOWN` since the controller bindings should be configured to activate that)
     */
    val lowerRightPaddle: Boolean get() = dpad == POVDirection.DOWN

    /**
     * Gets the Backside Lower Left Paddle on the Razer Wolverine controller.
     *
     * (Same as `dpad == POVDirection.LEFT` since the controller bindings should be configured to activate that)
     */
    val lowerLeftPaddle: Boolean get() = dpad == POVDirection.LEFT

    // TRIGGERS

    /**
     * Constructs a Trigger instance around the Top Left Small Button's digital signal on the Razer Wolverine controller.
     *
     * (Same as [leftStick] since the controller bindings should be configured to activate that)
     */
    fun clawGripLeftButton(): Trigger = leftStick()

    /**
     * Constructs a Trigger instance around the Top Right Small Button's digital signal on the Razer Wolverine controller.
     *
     * (Same as [rightStick] since the controller bindings should be configured to activate that)
     */
    fun clawGripRightButton(): Trigger = rightStick()


    /**
     * Constructs a Trigger instance around the Backside Upper Left Paddle's digital signal on the Razer Wolverine controller.
     *
     * (Same as [dpadUp] since the controller bindings should be configured to activate that)
     */
    fun upperLeftPaddle(): Trigger = dpadUp()

    /**
     * Constructs a Trigger instance around the Backside Upper Right Paddle's digital signal on the Razer Wolverine controller.
     *
     * (Same as [dpadRight] since the controller bindings should be configured to activate that)
     */
    fun upperRightPaddle(): Trigger = dpadRight()

    /**
     * Constructs a Trigger instance around the Backside Lower Left Paddle's digital signal on the Razer Wolverine controller.
     *
     * (Same as [dpadLeft] since the controller bindings should be configured to activate that)
     */
    fun lowerLeftPaddle(): Trigger = dpadLeft()

    /**
     * Constructs a Trigger instance around the Backside Lower Right Paddle's digital signal on the Razer Wolverine controller.
     *
     * (Same as [dpadDown] since the controller bindings should be configured to activate that)
     */
    fun lowerRightPaddle(): Trigger = dpadDown()
}