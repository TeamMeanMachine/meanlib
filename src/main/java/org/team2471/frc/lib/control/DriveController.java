package org.team2471.frc.lib.control;

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.buttons.JoystickButton;
import edu.wpi.first.wpilibj.command.Command;

public class DriveController {
  private final Joystick joystick;
  private final JoystickButton[] buttons;

  public DriveController(Joystick joystick) {
    this.joystick = joystick;

    int buttonCount = joystick.getButtonCount();
    buttons = new JoystickButton[buttonCount];
    for (int i = 0; i < buttonCount; i++) {
      buttons[i] = new JoystickButton(joystick, i + 1); // Joystick buttons are indexed from 1
    }
  }
  
  public DriveController(int joystickPort) {
    this(new Joystick(joystickPort));
  }

  public DriveController withRunCommandOnButtonPressEvent(int buttonId, Command command) {
    buttons[buttonId].whenPressed(command);
    return this;
  }

  public DriveController withRunCommandOnButtonReleaseEvent(int buttonId, Command command) {
    buttons[buttonId].whenReleased(command);
    return this;
  }

  public DriveController withRunCommandWhileButtonHoldEvent(int buttonId, Command command) {
    buttons[buttonId].whileHeld(command);
    return this;
  }

  public DriveController withToggleCommandOnButtonPressEvent(int buttonId, Command command) {
    buttons[buttonId].toggleWhenPressed(command);
    return this;
  }

  public DriveController withCancelCommandOnButtonPressEvent(int buttonId, Command command) {
    buttons[buttonId].cancelWhenPressed(command);
    return this;
  }
}
