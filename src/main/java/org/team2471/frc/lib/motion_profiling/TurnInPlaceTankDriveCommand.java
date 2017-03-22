package org.team2471.frc.lib.motion_profiling;

import com.ctre.CANTalon;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.command.Command;
import org.team2471.frc.lib.control.CANController;

public class TurnInPlaceTankDriveCommand extends Command {

  private double m_angle;
  private double m_time;
  private double m_robotWidth;
  private double m_startTime;
  private double m_playTime;
  private double m_leftDistance;
  private double m_rightDistance;
  private double m_leftDistanceOffset;
  private double m_rightDistanceOffset;
  private CANController m_leftController;
  private CANController m_rightController;
  private MotionCurve m_motionCurve;

  public TurnInPlaceTankDriveCommand(double angle, double time, double robotWidth, CANController leftController, CANController rightController) {
    m_angle = angle;
    m_time = time;
    m_robotWidth = robotWidth;
    m_leftController = leftController;
    m_rightController = rightController;
    double distance = angle / 360.0 * Math.PI * robotWidth;
    m_motionCurve = new MotionCurve();
    m_motionCurve.storeValue(0, 0);
    m_motionCurve.storeValue( time, distance );
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
    m_leftDistance = 0;
    m_rightDistance = 0;
  }

  @Override
  protected void execute() {
    m_playTime = (Timer.getFPGATimestamp() - m_startTime);

    // time to set the controller's position set-points
    m_leftDistance = m_motionCurve.getValue( m_playTime );
    m_rightDistance = -m_leftDistance;

    m_leftController.setSetpoint(m_leftDistance + m_leftDistanceOffset);
    m_rightController.setSetpoint(-m_rightDistance + m_rightDistanceOffset);
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
    return m_playTime >= m_time;
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

  public double getTime() {  // how much time has passed since the start
    return m_playTime;
  }

  public double getMaxTime() {
    return m_time;
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
