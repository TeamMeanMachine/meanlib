package com.therekrab.autopilot;

import org.wpilib.math.geometry.Pose2d;import org.wpilib.math.geometry.Rotation2d;import org.wpilib.units.measure.Distance;import java.util.Optional;

/**
 * The APTarget class represents the goal end state of an Autopilot action.
 * 
 * A target needs a reference Pose2d, but can optionally have a specified entry angle and rotation
 * radius.
 *
 * A target may also specify an end velocity or end velocity.
 */
public class APTarget {
  protected Pose2d m_reference;
  protected Optional<Rotation2d> m_entryAngle;
  protected double m_velocity;
  protected Optional<Distance> m_rotationRadius;

  /**
   * Creates a new Autopilot target with the given target pose, no entry angle, and no end velocity.
   * 
   * @param pose The reference pose for this target.
   */
  public APTarget(Pose2d pose) {
    m_reference = pose;
    m_velocity = 0;
    m_entryAngle = Optional.empty();
    m_rotationRadius = Optional.empty();
  }

  /**
   * Returns a copy of this target with the given reference Pose2d.
   *
   * @param reference The reference Pose2d for this target.
   */
  public APTarget withReference(Pose2d reference) {
    APTarget target = this.clone();
    target.m_reference = reference;
    return target;
  }

  /**
   * Returns a copy of this target with the given entry angle.
   *
   * @param entryAngle The entry angle for the new target.
   */
  public APTarget withEntryAngle(Rotation2d entryAngle) {
    APTarget target = this.clone();
    target.m_entryAngle = Optional.of(entryAngle);
    return target;
  }

  /**
   * Returns a copy of this target with the given end velocity. Note that if the robot does not
   * reach this velocity, no issues will be thrown. Autopilot will only try to reach this target,
   * but it will not be affected by whether it does.
   *
   * @param velocity The desired end velocity when the robot approaches the target
   */
  public APTarget withVelocity(double velocity) {
    APTarget target = this.clone();
    target.m_velocity = velocity;
    return target;
  }

  /**
   * Returns a copy of this target with the given rotation radius.
   *
   * <p> Rotation radius is the distance from the target pose that rotation goals are respected.
   * 
   * <p> By default, rotation goals are always respected. Adjusting this radius prevents Autopilot from reorienting
   * the robot until the robot is within the specified radius of the target.
   *
   * @param radius The rotation radius for the new target
   */
  public APTarget withRotationRadius(Distance radius) {
    APTarget copy = this.clone();
    copy.m_rotationRadius = Optional.of(radius);
    return copy;
  }

  /**
   * Returns this target's reference Pose2d.
   */
  public Pose2d getReference() {
    return m_reference;
  }

  /**
   * Returns this target's desired entry angle.
   */
  public Optional<Rotation2d> getEntryAngle() {
    return m_entryAngle;
  }

  /**
   * Returns this target's end velocity.
   */
  public double getVelocity() {
    return m_velocity;
  }

  /**
   * Returns this target's rotation radius.
   */
  public Optional<Distance> getRotationRadius() {
    return m_rotationRadius;
  }

  /**
   * Creates a copy of this APTarget.
   */
  public APTarget clone() {
    APTarget target = new APTarget(m_reference);
    target.m_velocity = m_velocity;
    target.m_entryAngle = m_entryAngle;
    target.m_rotationRadius = m_rotationRadius;
    return target;
  }

  /**
   * Retuns a copy of this target, without the entry angle set. 
   * 
   * <p> This is useful if trying to make two different targets with and without entry angle set.
   */
  public APTarget withoutEntryAngle() {
    APTarget target = new APTarget(m_reference);
    target.m_velocity = m_velocity;
    target.m_rotationRadius = m_rotationRadius;
    return target;
  }
}
