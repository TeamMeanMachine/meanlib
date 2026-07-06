package com.therekrab.autopilot;

/**
 * A class that holds constraint information for an Autopilot action.
 * 
 * Constraints are max velocity, acceleration, and jerk.
 */
public class APConstraints {
  protected double velocity;
  protected double acceleration;
  protected double jerk;

  // These values represent the "cutoff" parameters that Autopilot uses to determine what should be
  // happening during the end behavior of a path. These are always the same for a constant
  // constraint, so to save the most computational time, they're precomputed with the constraint,
  // rather than each time Autopilot.calculate is called.

  /** Cutoff distance */
  protected final double x0;
  /** Velocity at cutoff distance */
  protected final double v0;

  /**
   * Creates a blank APConstraints object.
   * <p>
   * A blank APConstraints will not limit velocity, acceleration, or jerk.
   */
  public APConstraints() {
    this(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
  }

  /**
   * Creates a new APConstraints object with a given max velocity, acceleration, and jerk.
   * 
   * @param velocity The maximum velocity that Autopilot will demand, in m/s
   * @param acceleration The maximum acceleration that Autopilot action will use to correct initial
   *        velocities, in m/s^2
   * @param jerk The maximum jerk that Autopilot will use to decelerate at the end of an action, in
   *        m/s^3
   */

  public APConstraints(double velocity, double acceleration, double jerk) {
    this.velocity = velocity;
    this.acceleration = acceleration;
    this.jerk = jerk;

    x0 = Math.pow(acceleration, 3.0) / (18.0 * jerk * jerk);
    v0 = jerkConstrainedVelocity(x0);
  }

  /**
   * Create a new APConstraints object with a given max acceleration and jerk.
   * <p>
   * This constructor defaults the velocity to unlimited.
   * 
   * @param acceleration The maximum acceleration that Autopilot action will use to correct initial
   *        velocities, in m/s^2
   * @param jerk The maximum jerk that Autopilot will use to decelerate at the end of an action, in
   *        m/s^3
   * 
   */
  public APConstraints(double acceleration, double jerk) {
    this(Double.POSITIVE_INFINITY, acceleration, jerk);
  }

  /**
   * Copies this APConstraints object, changes the copy's max velocity, and returns the copy. This
   * affects the maximum velocity that Autopilot can demand.
   * 
   * @param newVelocity The maximum velocity that Autopilot will demand, in m/s
   */
  public APConstraints withVelocity(double newVelocity) {
    return new APConstraints(newVelocity, this.acceleration, this.jerk);
  }

  /**
   * Copies this APConstraint object, changes the copy's acceleration, and returns the copy. This
   * affects the maximum acceleration that Autopilot will use to correct initial velocities.
   *
   * <p>
   * Autopilot's acceleration is used at the beginning and end of an action
   * 
   * @param newAcceleration The maximum acceleration that Autopilot will use to start a path, in
   *        m/s^2
   */
  public APConstraints withAcceleration(double newAcceleration) {
    return new APConstraints(this.velocity, newAcceleration, this.jerk);
  }

  /**
   * Copies this APConstraint object, changes the copy's max jerk, and returns the copy. Higher
   * values mean a faster deceleration.
   * 
   * Autopilot's jerk is used at the end of an action (not relevant to Autopilot's start behavior).
   * 
   * @param newJerk The maximum jerk that Autopilot will use to decelerate at the end of an action,
   *        in m/s^3
   */
  public APConstraints withJerk(double newJerk) {
    return new APConstraints(this.velocity, this.acceleration, newJerk);
  }

  /**
   * Determines the maximum velocity required to travel the given distance and end at 0 m/s.
   * 
   * @param dist The distance to travel, in meters
   */
  protected double calculateMaxVelocity(double dist) {
    if (dist > x0) {
      return accelerationConstrainedVelocity(dist);
    }
    return jerkConstrainedVelocity(dist);
  }

  private double accelerationConstrainedVelocity(double dist) {
    return Math.sqrt(v0 * v0 + 2.0 * acceleration * (dist - x0));
  }

  private double jerkConstrainedVelocity(double dist) {
    return Math.pow((4.5 * Math.pow(dist, 2.0)) * jerk, 1.0 / 3.0);
  }
}
