package org.team2471.frc.lib.io;

import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj.XboxController;

public class Controller {
  private final GenericHID controller;

  public Controller(GenericHID controller) {
    this.controller = controller;
  }

  public Controller(int hidPort) {
    this(new XboxController(hidPort));
  }

  public String getName() {
    return controller.getName();
  }

  public void rumbleLeft(float strength) {
    controller.setRumble(GenericHID.RumbleType.kLeftRumble, strength);
  }

  public void rumbleRight(float strength) {
    controller.setRumble(GenericHID.RumbleType.kRightRumble, strength);
  }

  public ControllerAxis getAxis(int axisId) {
    return () -> controller.getRawAxis(axisId);
  }

  public ControllerButton getButton(int buttonId) {
    return () -> controller.getRawButton(buttonId);
  }

  public ControllerDPad getDPad(int pov) {
    return () -> controller.getPOV(pov);
  }

  public ControllerDPad getDPad() {
    return getDPad(0);
  }
}
