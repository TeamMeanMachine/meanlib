package org.team2471.frc.lib.motion_profiling;

import edu.wpi.first.wpilibj.Utility;
import edu.wpi.first.wpilibj.command.Command;

/**
 * Created by Bob on 12/10/2016.
 */

public class PlayAnimationCommand extends Command {

  private MotionProfileAnimation m_MotionProfile_animation;
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
  }

  @Override
  protected void execute() {
    m_forwardTime = Utility.getFPGATime() - m_startTime;
    if (m_speed < 0) {  // negative speed plays animation backwards
      m_playTime = m_animationLength - m_forwardTime;
    }
    else {
      m_playTime = m_forwardTime;
    }
    m_MotionProfile_animation.play( m_playTime );
  }

  @Override
  protected boolean isFinished() {
    return m_forwardTime >= m_animationLength * Math.abs(m_speed) &&
            m_MotionProfile_animation.onTarget();
  }

  @Override
  protected void end() {
    m_MotionProfile_animation.stop();
  }

  @Override
  protected void interrupted() {
    end();
  }

  public MotionProfileAnimation getAnimation() {
    return m_MotionProfile_animation;
  }

  public void setAnimation(MotionProfileAnimation motionProfileAnimation) {
    m_MotionProfile_animation = motionProfileAnimation;
    m_animationLength = m_MotionProfile_animation.getLength();
  }

  public double getSpeed() {
    return m_speed;
  }

  public void setSpeed(double speed) {
    m_speed = speed;
  }
}
