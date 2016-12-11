package org.team2471.frc.lib.motion_profiling;

import edu.wpi.first.wpilibj.PIDController;

/**
 * Created by Bob on 12/9/2016.
 */
public class MotionProfilingCurve extends MotionCurve {

  public MotionProfilingCurve(PIDController pidController) {
    m_PIDController = pidController;
  }

  public void play(double time) {
    m_PIDController.setSetpoint(getValue(time));
    m_PIDController.enable();
  }

  public void stop() {
    m_PIDController.disable();
  }

  private PIDController m_PIDController;
}
