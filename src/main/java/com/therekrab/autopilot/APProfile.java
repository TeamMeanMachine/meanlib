package com.therekrab.autopilot;


import org.wpilib.units.measure.Angle;import org.wpilib.units.measure.Distance;import static org.wpilib.units.Units.Meters;import static org.wpilib.units.Units.Rotations; /**
 * A class representing a profile that determines how Autopilot approaches a target.
 *
 * The constraints property of the profile limits the robot's behavior.
 *
 * <p> Acceptable error for the controller (both translational and rotational) are stored here.
 *
 * <p> The "beeline radius" determines the distance at which the robot drives directly at the target and
 * no longer respects entry angle. This is helpful because if the robot overshoots by a small
 * amount, that error should not cause the robot do completely circle back around.
 */
public class APProfile {
  protected APConstraints constraints;
  protected Distance errorXY;
  protected Angle errorTheta;
  protected Distance beelineRadius;

  /**
   * Builds an APProfile with the given constraints. Tolerated error and beeline radius are all set
   * to zero.
   *
   * @param constraints The motion constraints for this profile
   */
  public APProfile(APConstraints constraints) {
    this.constraints = constraints;
    errorXY = Meters.of(0);
    errorTheta = Rotations.of(0);
    beelineRadius = Meters.of(0);
  }

  /**
   * Modifies this profile's tolerated error in the XY plane and returns itself
   * 
   * @param errorXY The tolerated translation error for this profile
   */
  public APProfile withErrorXY(Distance errorXY) {
    this.errorXY = errorXY;
    return this;
  }

  /**
   * Modifies this profile's tolerated angular error and returns itself
   * 
   * @param errorTheta The tolerated angular error for this profile
   */
  public APProfile withErrorTheta(Angle errorTheta) {
    this.errorTheta = errorTheta;
    return this;
  }

  /**
   * Modifies this profile's path generation constraints and returns itself
   * 
   * @param constraints The Autopilot constraints to apply to this profile
   */
  public APProfile withConstraints(APConstraints constraints) {
    this.constraints = constraints;
    return this;
  }

  /**
   * Modifies this profile's beeline radius and returns itself
   *
   * <p> The beeline radius is a distance where, under that range, entry angle is no longer respected.
   * This prevents small overshoots from causing the robot to make a full arc and instead correct
   * itself.
   * 
   * @param beelineRadius The distance at which the robot will drive directly at the target
   */
  public APProfile withBeelineRadius(Distance beelineRadius) {
    this.beelineRadius = beelineRadius;
    return this;
  }

  /**
   * Returns the tolerated translation error for this profile.
   */
  public Distance getErrorXY() {
    return errorXY;
  }

  /**
   * Returns the tolerated angular error for this profile.
   */
  public Angle getErrorTheta() {
    return errorTheta;
  }

  /**
   * Returns the path generation constraints for this profile.
   */
  public APConstraints getConstraints() {
    return constraints;
  }

  /**
   * Returns the beeline radius for this profile.
   */
  public Distance getBeelineRadius() {
    return beelineRadius;
  }
}
