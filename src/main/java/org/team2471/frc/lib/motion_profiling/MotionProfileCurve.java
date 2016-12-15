package org.team2471.frc.lib.motion_profiling;

import edu.wpi.first.wpilibj.PIDController;

/**
 * Created by Bob on 12/9/2016.
 */
public class MotionProfileCurve extends MotionCurve {

  public MotionProfileCurve(PIDController pidController) {
    m_PIDController = pidController;
  }

  public void play(double time) {
    m_PIDController.setSetpoint(getValue(time));
    m_PIDController.enable();
  }

  public void stop() {
    // m_PIDController.disable();  // by removing this, we can hold our position once the animation is done.
  }

  public boolean onTarget() {
    return m_PIDController.onTarget();
  }

  private PIDController m_PIDController;
}
