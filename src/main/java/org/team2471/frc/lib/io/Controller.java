package org.team2471.frc.lib.io;

import edu.wpi.first.wpilibj.Joystick;

// TODO: Update to match 2017 wpilib Joystick api
public class Controller {
  private final Joystick joystick;

  public Controller(Joystick joystick) {
    this.joystick = joystick;

    int buttonCount = joystick.getButtonCount();
  }

  public Controller(int joystickPort) {
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

  public ControllerAxis getAxis(int axisId) {
    return () -> joystick.getRawAxis(axisId);
  }

  public ControllerButton getButton(int buttonId) {
    return () -> joystick.getRawButton(buttonId);
  }

  public ControllerDPad getDPad(int pov) {
    return () -> joystick.getPOV(pov);
  }

  public ControllerDPad getDPad() {
    return getDPad(0);
  }
}
