package org.team2471.frc.lib.motion_profiling.util;

import edu.wpi.first.wpilibj.command.Command;
import org.team2471.frc.lib.control.CANController;
import org.team2471.frc.lib.motion_profiling.FollowPathTankDriveCommand;
import org.team2471.frc.lib.motion_profiling.Path2D;

public final class FollowPathTankDriveCommandBuilder {
  private final FollowPathTankDriveCommand result = new FollowPathTankDriveCommand();
  private final Path2D path = new Path2D();

  private boolean mirrored = false;
  private boolean reversed = false;
  private double speed = 1.0;

  public FollowPathTankDriveCommandBuilder(CANController leftController, CANController rightController) {
    result.setLeftController(leftController);
    result.setRightController(rightController);
  }

  public FollowPathTankDriveCommandBuilder(TankDriveProfile profile) {
    this(profile.leftController, profile.rightController);
  }

  public FollowPathTankDriveCommandBuilder withMirrored(boolean mirrored) {
    this.mirrored = mirrored;
    return this;
  }

  public FollowPathTankDriveCommandBuilder withReversed(boolean reversed) {
    this.reversed = reversed;
    return this;
  }

  public FollowPathTankDriveCommandBuilder withSpeed(double speed) {
    this.speed = speed;
    return this;
  }

  public FollowPathTankDriveCommandBuilder withPoint(double x, double y) {
    path.addPoint(x, y);
    return this;
  }

  public FollowPathTankDriveCommandBuilder withPointAndTangent(double x, double y, double xTangent, double yTangent) {
    path.addPointAndTangent(x, y, xTangent, yTangent);
    return this;
  }

  public FollowPathTankDriveCommandBuilder withPointAngleAndMagnitude(double x, double y, double angle, double magnitude) {
    path.addPointAngleAndMagnitude(x, y, angle, magnitude);
    return this;
  }

  public FollowPathTankDriveCommandBuilder withEasePoint(double time, double value) {
    path.addEasePoint(time, value);
    return this;
  }

  public Command build() {
    path.setTravelDirection(reversed ? -1.0 : 1.0);
    result.setMirrorPath(mirrored);
    result.setSpeed(speed);
    result.setPath(path);
    return result;
  }
}
