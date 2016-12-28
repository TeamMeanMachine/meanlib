package org.team2471.frc.lib.motion_profiling;

import edu.wpi.first.wpilibj.CANTalon;
import edu.wpi.first.wpilibj.PIDController;
import edu.wpi.first.wpilibj.PIDInterface;
import edu.wpi.first.wpilibj.Utility;
import edu.wpi.first.wpilibj.command.Command;
import org.team2471.frc.lib.sensors.CANController;
import org.team2471.frc.lib.vector.Vector2;

public class FollowPathTankDriveCommand extends Command {

  private Path2D m_path;
  private double m_speed;
  private double m_startTime;
  private double m_playTime;
  private double m_forwardTime;
  private double m_pathMaxTime;
  private double m_leftDistance;
  private double m_rightDistance;
  private double m_leftDistanceOffset;
  private double m_rightDistanceOffset;
  private CANController m_leftController;
  private CANController m_rightController;

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
    m_leftController.enable();
    m_rightController.enable();
    m_leftController.changeControlMode(CANTalon.TalonControlMode.Position);
    m_rightController.changeControlMode(CANTalon.TalonControlMode.Position);
    m_leftDistanceOffset = m_leftController.getPosition();
    m_rightDistanceOffset = m_rightController.getPosition();
    System.out.println( "Offsets L, R: " + m_leftDistanceOffset + ", " + m_rightDistanceOffset );
    m_leftDistance = 0;
    m_rightDistance = 0;
    m_path.reset();
  }

  @Override
  protected void execute() {
    m_forwardTime = (Utility.getFPGATime() - m_startTime) / 1.0e6 * Math.abs(m_speed);
    if (m_speed < 0) {  // negative speed plays the path backwards
      m_playTime = m_pathMaxTime - m_forwardTime;
    }
    else {
      m_playTime = m_forwardTime;
    }

    // time to set the controller's position set-points
    m_leftDistance += m_path.getLeftPositionDelta( m_playTime );
    m_rightDistance += m_path.getRightPositionDelta( m_playTime );

    m_leftController.setSetpoint(m_leftDistance + m_leftDistanceOffset);
    m_rightController.setSetpoint(-m_rightDistance + m_rightDistanceOffset);

    System.out.print("Time: " + m_playTime);
    System.out.print("\t Left SetPoint: " + m_leftController.getSetpoint());
    System.out.print("\t Left Position: " + m_leftController.getPosition());
    System.out.println("\t Left Error: " + m_leftController.getError());

/*
    System.out.print("Time: " + m_playTime);
    System.out.print("\t Right SetPoint: " + m_rightController.getSetpoint());
    System.out.print("\t Right Position: " + m_rightController.getPosition());
    System.out.println("\t Right Error: " + m_rightController.getError());
*/
  }

  @Override
  protected boolean isFinished() {
    return m_forwardTime >= m_pathMaxTime * Math.abs(m_speed);
  }

  @Override
  protected void end() {
    m_leftController.changeControlMode(CANTalon.TalonControlMode.PercentVbus);
    m_rightController.changeControlMode(CANTalon.TalonControlMode.PercentVbus);
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
    m_pathMaxTime = m_path.getDuration();
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

  public CANController getLeftController() {
    return m_leftController;
  }

  public void setLeftController(CANController leftController) {
    m_leftController = leftController;
  }

  public CANController getRightController() {
    return m_rightController;
  }

  public void setRightController(CANController rightController) {
    m_rightController = rightController;
  }
}
