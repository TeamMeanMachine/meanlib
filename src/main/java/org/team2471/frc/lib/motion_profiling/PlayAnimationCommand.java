package org.team2471.frc.lib.motion_profiling;

import edu.wpi.first.wpilibj.Utility;
import edu.wpi.first.wpilibj.command.Command;

/**
 * Created by Bob on 12/10/2016.
 */

public class PlayAnimationCommand extends Command {

  Animation m_animation;
  double m_speed;
  double m_startTime;
  double m_playTime;
  double m_forwardTime;
  double m_animationLength;

  public PlayAnimationCommand(Animation animation, float speed) {
    m_animation = animation;
    m_speed = speed;
    m_animationLength = m_animation.getLength();
  }

  @Override
  protected void initialize() {
    m_startTime = Utility.getFPGATime();
  }

  @Override
  protected void execute() {
    m_forwardTime = Utility.getFPGATime() - m_startTime;
    if (m_speed < 0) {
      m_playTime = m_animationLength - m_forwardTime;
    }
    else {
      m_playTime = m_forwardTime;
    }
    m_animation.play( m_playTime );
  }

  @Override
  protected boolean isFinished() {
    return m_forwardTime >= m_animationLength;
  }

  @Override
  protected void end() {
    m_animation.stop();
  }

  @Override
  protected void interrupted() {
    end();
  }
}
