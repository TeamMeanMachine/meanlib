package com.therekrab.autopilot;

import org.wpilib.math.geometry.Pose2d;
import org.wpilib.math.geometry.Rotation2d;import org.wpilib.math.geometry.Translation2d;
import org.wpilib.math.kinematics.ChassisVelocities;import org.wpilib.units.measure.LinearVelocity;import static org.wpilib.units.Units.*;

/**
 * Autopilot is a class that tries to drive a target to a goal in 2-D space.
 *
 * Autopilot is a stateless algorithm; as such, it does not "think ahead" and cannot avoid
 * obstacles. Any math that Autopilot needs is already worked out such that only a small amount of
 * computation is necessary on the fly. Autopilot is designed to be used in a drivetrain's control
 * loop, where the current state of the robot is passed in, and the next velocity is returned.
 * 
 */
public class Autopilot {
  private APProfile profile;

  private final double dt = 0.020;

  /**
   * Constructs an Autopilot from a given profile. This is the profile that the autopilot will use
   * for all actions.
   */
  public Autopilot(APProfile profile) {
    this.profile = profile;
  }

  /**
   * Returns the next field relative velocity for the trajectory
   *
   * @param current The robot's current position.
   * @param robotRelativeVelocities The robot's current <b>robot relative</b> ChassisSpeeds.
   * @param target The target the robot should drive towards.
   * 
   * @return an APResult containing the next velocity and target angle
   */
  public APResult calculate(Pose2d current, ChassisVelocities robotRelativeVelocities, APTarget target) {
    Translation2d offset = toTargetCoordinateFrame(
        target.m_reference.getTranslation().minus(current.getTranslation()), target);

    if (offset.equals(Translation2d.kZero)) {
      return new APResult(
          MetersPerSecond.zero(),
          MetersPerSecond.zero(),
          target.m_reference.getRotation());
    }

    Translation2d fieldRelativeSpeeds = new Translation2d(
            robotRelativeVelocities.vx,
            robotRelativeVelocities.vy).rotateBy(current.getRotation());

    Translation2d initial = toTargetCoordinateFrame(fieldRelativeSpeeds, target);
    double disp = offset.getNorm();
    if (target.m_entryAngle.isEmpty() || disp < profile.beelineRadius.in(Meters)) {
      Translation2d towardsTarget = offset.div(disp);
      Translation2d goal =
          towardsTarget.times(profile.constraints.calculateMaxVelocity(disp) + target.m_velocity);
      Translation2d out = correct(initial, goal);
      Translation2d velo = toGlobalCoordinateFrame(out, target);
      Rotation2d rot = getRotationTarget(current.getRotation(), target, disp);
      return new APResult(MetersPerSecond.of(velo.getX()), MetersPerSecond.of(velo.getY()), rot);
    }
    Translation2d goal = calculateSwirlyVelocity(offset, target);
    Translation2d out = correct(initial, goal);
    Translation2d velo = toGlobalCoordinateFrame(out, target);
    Rotation2d rot = getRotationTarget(current.getRotation(), target, disp);
    return new APResult(MetersPerSecond.of(velo.getX()), MetersPerSecond.of(velo.getY()), rot);
  }

  /**
   * Turns any other coordinate frame into a coordinate frame with positive x meaning in the
   * direction of the target's entry angle, if applicable (otherwise no change to angles).
   */
  private Translation2d toTargetCoordinateFrame(Translation2d coords, APTarget target) {
    Rotation2d entryAngle = target.m_entryAngle.orElse(Rotation2d.kZero);
    return coords.rotateBy(entryAngle.unaryMinus());
  }

  /**
   * Turns a translation from a target-relative coordinate frame to a global coordinate frame.
   */
  private Translation2d toGlobalCoordinateFrame(Translation2d coords, APTarget target) {
    Rotation2d entryAngle = target.m_entryAngle.orElse(Rotation2d.kZero);
    return coords.rotateBy(entryAngle);
  }

  /**
   * Attempts to drive the initial translation to the goal translation using the parameters for
   * acceleration given in the profile.
   * 
   * @param initial The initial translation to drive from
   * @param goal The goal translation to drive to
   */
  private Translation2d correct(Translation2d initial, Translation2d goal) {
    Rotation2d angleOffset = Rotation2d.kZero;
    if (!goal.equals(Translation2d.kZero)) {
      angleOffset = new Rotation2d(goal.getX(), goal.getY());
    }
    Translation2d adjustedGoal = goal.rotateBy(angleOffset.unaryMinus());
    Translation2d adjustedInitial = initial.rotateBy(angleOffset.unaryMinus());
    double initialI = adjustedInitial.getX();
    double goalI = adjustedGoal.getX();
    // we cap the adjusted I because we'd rather adjust now than overshoot.
    if (goalI > profile.constraints.velocity) {
      goalI = profile.constraints.velocity;
    }
    double adjustedI = Math.min(goalI,
        push(initialI, goalI, profile.constraints.acceleration));
    return new Translation2d(adjustedI, 0).rotateBy(angleOffset);
  }

  /**
   * Using the provided acceleration, "pushes" the start point towards the end point.
   *
   * This is used for ensuring that changes in velocity are withing the acceleration threshold.
   */
  private double push(double start, double end, double accel) {
    double maxChange = accel * dt;
    if (Math.abs(start - end) < maxChange) {
      return end;
    }
    if (start > end) {
      return start - maxChange;
    }
    return start + maxChange;
  }

  /**
   * Uses the swirly method to calculate the correct velocities for the robot, respecting entry
   * angles.
   *
   * @param offset The offset from the robot to the target, in the target's coordinate frame
   * @param target The target that Autopilot is trying to reach
   */
  private Translation2d calculateSwirlyVelocity(Translation2d offset, APTarget target) {
    double disp = offset.getNorm();
    Rotation2d theta = new Rotation2d(offset.getX(), offset.getY());
    double rads = theta.getRadians();
    double dist = calculateSwirlyLength(rads, disp);
    double vx = theta.getCos() - rads * theta.getSin();
    double vy = rads * theta.getCos() + theta.getSin();
    return new Translation2d(vx, vy)
        .div(Math.hypot(vx, vy)) // normalize
        .times(profile.constraints.calculateMaxVelocity(dist) + target.m_velocity); // and scale to
                                                                                    // new length
  }

  /**
   * Using a precomputed integral, returns the length of the path that the swirly method generates.
   *
   * <p>
   * More specifically, this calculates the arc length of the polar curve r=theta from the given
   * angle to zero, then scales it to match the current state.
   * 
   * @param theta The angle of the offset from the robot to the target, in radians
   * @param radius The normalized offset from the robot to the target, in meters
   */
  private double calculateSwirlyLength(double theta, double radius) {
    if (theta == 0) {
      return radius;
    }
    theta = Math.abs(theta);
    double hypot = Math.hypot(theta, 1);
    double u1 = radius * hypot;
    double u2 = radius * Math.log(theta + hypot) / theta;
    return 0.5 * (u1 + u2);
  }

  /**
   * Returns the target's rotation if the robot is within a specified rotation radius; otherwise,
   * returns the current rotation of the robot.
   * 
   * @param current The current rotation of the robot.
   * @param target The APTarget that Autopilot is trying to reach.
   * @param dist The distance from the robot to the target.
   */
  private Rotation2d getRotationTarget(Rotation2d current, APTarget target, double dist) {
    if (target.m_rotationRadius.isEmpty()) {
      return target.m_reference.getRotation();
    }
    double radius = target.m_rotationRadius.get().in(Meters);
    if (radius > dist) {
      return target.m_reference.getRotation();
    } else {
      return current;
    }
  }

  /**
   * Return whether the given pose is within the tolerance of the APTarget.
   * 
   * @param current The current pose of the robot.
   * @param target The APTarget to check against.
   * 
   * @return Returns true if Autopilot has reached the target.
   */
  public boolean atTarget(Pose2d current, APTarget target) {
    Pose2d goal = target.m_reference;
    boolean okXY = Math.hypot(current.getX() - goal.getX(),
        current.getY() - goal.getY()) <= profile.errorXY.in(Meters);
    boolean okTheta = Math.abs(current.getRotation().minus(goal.getRotation())
        .getRadians()) <= profile.errorTheta.in(Radians);
    return okXY && okTheta;
  }

  /**
   * The computed motion from a call to <code>Autopilot.calculate()</code>.
   */
  public record APResult(LinearVelocity vx, LinearVelocity vy, Rotation2d targetAngle) {
  }
}
