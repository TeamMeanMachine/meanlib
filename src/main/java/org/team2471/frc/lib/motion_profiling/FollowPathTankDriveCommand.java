package org.team2471.frc.lib.motion_profiling;

import edu.wpi.first.wpilibj.PIDController;
import edu.wpi.first.wpilibj.Utility;
import edu.wpi.first.wpilibj.command.Command;

public class FollowPathTankDriveCommand extends Command {

  private Path2D m_path;
  private double m_speed;
  private double m_startTime;
  private double m_playTime;
  private double m_forwardTime;
  private double m_pathMaxTime;
  private PIDController m_leftController;
  private PIDController m_rightController;

  public FollowPathTankDriveCommand() {
    m_speed = 1.0;
  }

  public FollowPathTankDriveCommand(Path2D path, double speed) {
    setPath(path);
    m_speed = speed;
  }

  @Override
  protected void initialize() {
    m_startTime = Utility.getFPGATime();
  }

  @Override
  protected void execute() {
    m_forwardTime = (Utility.getFPGATime() - m_startTime) / 1.0e6 * Math.abs(m_speed);
    if (m_speed < 0) {  // negative speed plays the path backwards
      m_playTime = m_pathMaxTime - m_forwardTime;
    } else {
      m_playTime = m_forwardTime;
    }

    // time to set the controller's position set points
  }

  @Override
  protected boolean isFinished() {
    return m_forwardTime >= m_pathMaxTime * Math.abs(m_speed);
  }

  @Override
  protected void end() {
  }

  @Override
  protected void interrupted() {
    end();
  }

  public Path2D getPath() {
    return m_path;
  }

  public void setPath(Path2D path) {
    m_path = path;
    m_pathMaxTime = path.getMaxTime();
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

  public double getMaxTime() {
    return m_pathMaxTime;
  }

  public PIDController getLeftController() {
    return m_leftController;
  }

  public void setLeftController(PIDController leftController) {
    m_leftController = leftController;
  }

  public PIDController getRightController() {
    return m_rightController;
  }

  public void setRightController(PIDController rightController) {
    m_rightController = rightController;
  }
}
