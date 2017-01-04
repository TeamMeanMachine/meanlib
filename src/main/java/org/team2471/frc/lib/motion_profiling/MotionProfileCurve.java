package org.team2471.frc.lib.motion_profiling;

import edu.wpi.first.wpilibj.PIDController;

public class MotionProfileCurve extends MotionCurve {

  private PIDController m_PIDController;

  public MotionProfileCurve(PIDController pidController) {
    m_PIDController = pidController;
  }

  public MotionProfileCurve(PIDController pidController, MotionProfileAnimation animation) {
    m_PIDController = pidController;
    animation.addMotionProfileCurve( this );
  }

  public void play(double time) {
    m_PIDController.setSetpoint(getValue(time));
  }

  public void stop() {
  }

  public boolean onTarget() {
    return m_PIDController.onTarget();
  }
}
