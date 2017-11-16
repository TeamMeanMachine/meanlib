package org.team2471.frc.lib.motion_profiling;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.command.Command;

public class PlayAnimationCommand extends Command {

  double m_prevTime;  // the previous clock
  double m_playTime;  // the current animation play time
  double m_animationLength;
  private MotionProfileAnimation m_MotionProfileAnimation;
  private double m_speed;
  private boolean backwards = false;

  public PlayAnimationCommand() {
    m_speed = 1.0;
    backwards = false;
    m_playTime = 0.0;
  }

  public PlayAnimationCommand(MotionProfileAnimation motionProfileAnimation, float speed) {
    setAnimation(motionProfileAnimation);
    m_speed = speed;
    backwards = (speed<0.0);
    m_playTime = 0.0;
  }

  @Override
  protected void initialize() {
    m_prevTime = Timer.getFPGATimestamp();
    if (backwards)
      m_playTime = m_animationLength;
    else
      m_playTime = 0.0;
  }

  @Override
  protected void execute() {
    double currTime = Timer.getFPGATimestamp();
    m_playTime += (currTime - m_prevTime) * m_speed;
    m_MotionProfileAnimation.play(m_playTime);
    m_prevTime = currTime;
  }

  @Override
  protected boolean isFinished() {
    if (backwards) {
      return m_playTime <= 0.0;
    }
    else {
      return m_playTime >= m_animationLength;
    }
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
    if (backwards) {
      m_playTime = m_animationLength;
    }
  }

  public void setBackwards(boolean backwards) {
    this.backwards = backwards;
    if (backwards) {
      m_playTime = m_animationLength;
    }
  }

  public double getSpeed() {
    return m_speed;
  }

  public void setSpeed(double speed) {
    m_speed = speed;
  }

  public double getTime() {  // how much time has passed since the schedule
    return m_playTime;
  }

  public double getLength() {
    return m_animationLength;
  }
}
