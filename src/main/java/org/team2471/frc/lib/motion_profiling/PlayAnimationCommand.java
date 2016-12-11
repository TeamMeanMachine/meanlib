package org.team2471.frc.lib.motion_profiling;

import edu.wpi.first.wpilibj.Utility;
import edu.wpi.first.wpilibj.command.Command;

/**
 * Created by Bob on 12/10/2016.
 */

public class PlayAnimationCommand extends Command {

  private Animation m_animation;
  private double m_speed;

  double m_startTime;
  double m_playTime;
  double m_forwardTime;
  double m_animationLength;

  public PlayAnimationCommand() {
  }

  public PlayAnimationCommand(Animation animation, float speed) {
    setAnimation( animation );
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
    m_animation.play( m_playTime );
  }

  @Override
  protected boolean isFinished() {
    return m_forwardTime >= m_animationLength * Math.abs(m_speed);
  }

  @Override
  protected void end() {
    m_animation.stop();
  }

  @Override
  protected void interrupted() {
    end();
  }

  public Animation getAnimation() {
    return m_animation;
  }

  public void setAnimation(Animation animation) {
    m_animation = animation;
    m_animationLength = m_animation.getLength();
  }

  public double getSpeed() {
    return m_speed;
  }

  public void setSpeed(double speed) {
    m_speed = speed;
  }
}
