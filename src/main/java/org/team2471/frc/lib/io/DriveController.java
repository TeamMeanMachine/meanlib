package org.team2471.frc.lib.io;

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

  public String getName() {
    return joystick.getName();
  }

  public void rumbleLeft(float strength) {
    joystick.setRumble(Joystick.RumbleType.kLeftRumble, strength);
  }

  public void rumbleRight(float strength) {
    joystick.setRumble(Joystick.RumbleType.kRightRumble, strength);
  }

  public DriveAxis getAxis(int axisId) {
    return () -> joystick.getRawAxis(axisId);
  }

  public DriveButton getButton(int buttonId) {
    return () -> joystick.getRawButton(buttonId);
  }

  public DriveController withRunCommandOnButtonPressEvent(int buttonId, Command command) {
    buttons[buttonId - 1].whenPressed(command);
    return this;
  }

  public DriveController withRunCommandOnButtonReleaseEvent(int buttonId, Command command) {
    buttons[buttonId - 1].whenReleased(command);
    return this;
  }

  public DriveController withRunCommandWhileButtonHoldEvent(int buttonId, Command command) {
    buttons[buttonId - 1].whileHeld(command);
    return this;
  }

  public DriveController withToggleCommandOnButtonPressEvent(int buttonId, Command command) {
    buttons[buttonId - 1].toggleWhenPressed(command);
    return this;
  }

  public DriveController withCancelCommandOnButtonPressEvent(int buttonId, Command command) {
    buttons[buttonId - 1].cancelWhenPressed(command);
    return this;
  }
}
