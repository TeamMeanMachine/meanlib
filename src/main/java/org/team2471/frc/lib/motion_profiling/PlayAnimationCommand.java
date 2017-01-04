package org.team2471.frc.lib.motion_profiling;

import edu.wpi.first.wpilibj.Utility;
import edu.wpi.first.wpilibj.command.Command;

public class PlayAnimationCommand extends Command {

  private MotionProfileAnimation m_MotionProfileAnimation;
  private double m_speed;

  double m_startTime;
  double m_playTime;
  double m_forwardTime;
  double m_animationLength;

  public PlayAnimationCommand() {
  }

  public PlayAnimationCommand(MotionProfileAnimation motionProfileAnimation, float speed) {
    setAnimation(motionProfileAnimation);
    m_speed = speed;
  }

  @Override
  protected void initialize() {
    m_startTime = Utility.getFPGATime();
    System.out.println("Start Time: " + m_startTime);
  }

  @Override
  protected void execute() {
    m_forwardTime = ((Utility.getFPGATime() - m_startTime) / 1.0e6) * Math.abs(m_speed);
    if (m_speed < 0) {  // negative speed plays animation backwards
      m_playTime = m_animationLength - m_forwardTime;
    }
    else {
      m_playTime = m_forwardTime;
    }
    m_MotionProfileAnimation.play( m_playTime );
  }

  @Override
  protected boolean isFinished() {
    return m_forwardTime >= m_animationLength;
  }

  @Override
  protected void end() {
    m_MotionProfileAnimation.stop();
  }

  @Override
  protected void interrupted() {
    end();
  }

  public MotionProfileAnimation getAnimation() {
    return m_MotionProfileAnimation;
  }

  public void setAnimation(MotionProfileAnimation motionProfileAnimation) {
    m_MotionProfileAnimation = motionProfileAnimation;
    m_animationLength = m_MotionProfileAnimation.getLength();
  }

  public double getSpeed() {
    return m_speed;
  }

  public void setSpeed(double speed) {
    m_speed = speed;
  }

  public double getTime() {  // how much time has passed since the start
    return m_forwardTime;
  }

  public double getLength() {
    return m_animationLength;
  }
}
