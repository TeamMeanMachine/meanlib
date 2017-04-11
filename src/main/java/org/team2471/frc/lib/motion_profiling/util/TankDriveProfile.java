package org.team2471.frc.lib.motion_profiling.util;

import org.team2471.frc.lib.control.CANController;

public class TankDriveProfile {
  public final CANController leftController;
  public final CANController rightController;

  public TankDriveProfile(CANController leftController, CANController rightController) {
    this.leftController = leftController;
    this.rightController = rightController;
  }
}
