package org.team2471.frc.lib.motion_profiling;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.interfaces.Gyro;
import org.team2471.frc.lib.control.CANController;
import org.team2471.frc.lib.control.MeanMotorController;

import com.ctre.CANTalon;
import edu.wpi.first.wpilibj.Utility;
import edu.wpi.first.wpilibj.command.Command;
import org.team2471.frc.lib.vector.Vector2;

public class FollowPathTankDriveCommand extends Command {

  private Path2D m_path;
  private double m_speed;
  private boolean m_mirrorPath;
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
  private Gyro m_gyro;
  private double m_startGyro;
  private double m_startPathHeading;

  public FollowPathTankDriveCommand() {
    m_speed = 1.0;
  }

  public FollowPathTankDriveCommand(Path2D path, double speed) {
    setPath(path);
    m_speed = speed;
    setMirrorPath(false);
    m_gyro = null;
  }

  public FollowPathTankDriveCommand(Path2D path, double speed, boolean mirrorPath) {
    setPath(path);
    m_speed = speed;
    setMirrorPath(mirrorPath);
    m_gyro = null;
  }

  @Override
  protected void initialize() {
    m_startTime = Timer.getFPGATimestamp();
    m_leftController.enable();
    m_rightController.enable();
    m_leftController.changeControlMode(CANTalon.TalonControlMode.Position);
    m_rightController.changeControlMode(CANTalon.TalonControlMode.Position);
    m_leftDistanceOffset = m_leftController.getPosition();
    m_rightDistanceOffset = m_rightController.getPosition();
    System.out.println("Offsets L, R: " + m_leftDistanceOffset + ", " + m_rightDistanceOffset);
    m_leftDistance = 0;
    m_rightDistance = 0;
    m_path.reset();
    if (m_gyro!=null) {
      m_startGyro = m_gyro.getAngle();
      if (m_speed < 0) {  // negative travels along the path backwards
        m_playTime = m_pathMaxTime;
      } else {
        m_playTime = 0.0;
      }
      Vector2 tangent = m_path.getTangent(m_playTime);
      m_startPathHeading = Math.atan2(tangent.x, tangent.y) / Math.PI * 180.0;
    }
  }

  @Override
  protected void execute() {
    m_forwardTime = (Timer.getFPGATimestamp() - m_startTime) * Math.abs(m_speed);
    if (m_speed < 0) {  // negative travels along the path backwards
      m_playTime = m_pathMaxTime - m_forwardTime;
    } else {
      m_playTime = m_forwardTime;
    }

    // time to set the controller's position set-points
    m_leftDistance += m_path.getLeftPositionDelta(m_playTime);
    m_rightDistance += m_path.getRightPositionDelta(m_playTime);

    double gyroCorrection = 0.0;
    if (m_gyro!=null) {
      Vector2 tangent = m_path.getTangent(m_playTime);
      double pathHeading = Math.atan2(tangent.x, tangent.y) / Math.PI * 180.0;
      double headingError = (m_gyro.getAngle() - m_startGyro) - (pathHeading - m_startPathHeading);
      gyroCorrection = Math.tan( headingError / 180.0 * Math.PI ) * m_path.getRobotWidth() * (m_mirrorPath ? -1.0 : 1.0);
      m_leftDistance += gyroCorrection;
      m_rightDistance -= gyroCorrection;
    }

    // we should mark the right encoder as inverted, but instead, for now, we just negate the right side values.

    if (m_mirrorPath) {
      m_leftController.setSetpoint(-m_rightDistance + m_leftDistanceOffset);
      m_rightController.setSetpoint(m_leftDistance + m_rightDistanceOffset);
    }
    else {
      m_leftController.setSetpoint(m_leftDistance + m_leftDistanceOffset);
      m_rightController.setSetpoint(-m_rightDistance + m_rightDistanceOffset);
    }
/*
    System.out.print("Time: " + m_playTime);
    System.out.print("\t Left SetPoint: " + m_leftController.getSetpoint());
    System.out.print("\t Left Position: " + m_leftController.getPosition());
    double error = m_leftController.getPosition() - m_leftController.getSetpoint();
    System.out.println("\t Left Error: " + error);
    SmartDashboard.putNumber("Left Error", error);
*/
/*
    System.out.print("Time: " + m_playTime);
    System.out.print("\t Right SetPoint: " + m_rightController.getSetpoint());
    System.out.print("\t Right Position: " + m_rightController.getPosition());
    System.out.println("\t Right Error: " + m_rightController.getError());
*/
  }

  @Override
  protected boolean isFinished() {
    return m_forwardTime >= m_pathMaxTime;
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

  public boolean isMirrorPath() {
    return m_mirrorPath;
  }

  public void setMirrorPath(boolean m_mirrorPath) {
    this.m_mirrorPath = m_mirrorPath;
  }

  public Gyro getGyro() {
    return m_gyro;
  }

  public void setGyro(Gyro gyro) {
    this.m_gyro = gyro;
  }
}
