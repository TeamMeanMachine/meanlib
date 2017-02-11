package org.team2471.frc.lib.control;

import edu.wpi.first.wpilibj.*;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

// A Motor controller that can switch from controlling on the roborio using WPILib PIDController or on board the Talon SRX

public class MeanMotorController extends CANTalon implements PIDInterface {
  private PIDController pidController;
  private double Kf;
  private double accumulatePower;

  public enum MeanMode {WPI_CLOSED_LOOP_VELOCITY, SRX_CLOSED_LOOP_VELOCITY, OPEN_LOOP, WPI_CLOSED_LOOP_POSITION, SRX_CLOSED_LOOP_POSITION };

  private MeanMode meanMode = MeanMode.WPI_CLOSED_LOOP_VELOCITY;

  public MeanMotorController( int canBusID, MeanMode meanModeParam, double Kp, double Ki, double Kd, double KfParam ) {
    super( canBusID );

    Kf = KfParam;
    pidController = new PIDController(Kp, Ki, Kd, new PIDSource() {
      @Override
      public void setPIDSourceType(PIDSourceType pidSource) {
      }

      @Override
      public PIDSourceType getPIDSourceType() {
        return PIDSourceType.kDisplacement;  // according to WPI we always use displacement
      }

      @Override
      public double pidGet() {
        if (meanMode == MeanMode.WPI_CLOSED_LOOP_POSITION)
          return getPosition();
        else  // MeanMode.WPI_CLOSED_LOOP_VELOCITY
          return getSpeed();
      }
    }, output -> {
      if (meanMode == MeanMode.WPI_CLOSED_LOOP_POSITION)
        set(output);
      else { // MeanMode.WPI_CLOSED_LOOP_VELOCITY
        accumulatePower += output;
        double power = Kf * getSetpoint() + accumulatePower;
        set(power);
      }
    }, 0.001 );  // 1000 hz

    setPID( Kp, Ki, Kd );
    setF( Kf );
    setMeanMode( meanModeParam );
  }

  public MeanMode getMeanMode() {
    return meanMode;
  }

  public void setMeanMode(MeanMode meanMode) {
    if (this.meanMode == meanMode)
      return;

    // leaving one mode
    switch (this.meanMode) {
      case WPI_CLOSED_LOOP_VELOCITY:
      case WPI_CLOSED_LOOP_POSITION:
        pidController.disable();
        break;
      case SRX_CLOSED_LOOP_VELOCITY:
      case SRX_CLOSED_LOOP_POSITION:
        super.changeControlMode(TalonControlMode.PercentVbus);
        break;
    }

    this.meanMode = meanMode;

    // entering another mode
    switch (this.meanMode) {
      case WPI_CLOSED_LOOP_VELOCITY:
      case WPI_CLOSED_LOOP_POSITION:
        pidController.enable();
        break;
      case SRX_CLOSED_LOOP_VELOCITY:
        super.changeControlMode(TalonControlMode.Speed);
        break;
      case SRX_CLOSED_LOOP_POSITION:
        super.changeControlMode(TalonControlMode.Position);
        break;
    }
  }

  @Override
  public void setPID(double p, double i, double d) {
    switch (meanMode) {
      case WPI_CLOSED_LOOP_VELOCITY:
      case WPI_CLOSED_LOOP_POSITION:
        pidController.setPID(p, i, d);
        break;
      case SRX_CLOSED_LOOP_VELOCITY:
      case SRX_CLOSED_LOOP_POSITION:
        super.setPID(p*1023.0, i*1023.0, d*1023.0);
        break;
    }
  }

  @Override
  public void setF(double f) {
    switch (meanMode) {
      case WPI_CLOSED_LOOP_VELOCITY:
        Kf = f;
        break;
      case SRX_CLOSED_LOOP_VELOCITY:
        super.setF(f*1023.0);
        break;
    }
  }

  @Override
  public double getP() {
    switch (meanMode) {
      case WPI_CLOSED_LOOP_VELOCITY:
      case WPI_CLOSED_LOOP_POSITION:
        return pidController.getP();
      case SRX_CLOSED_LOOP_VELOCITY:
      case SRX_CLOSED_LOOP_POSITION:
        return super.getP()/1023.0;
    }
    return 0;
  }

  @Override
  public double getI() {
    switch (meanMode) {
      case WPI_CLOSED_LOOP_VELOCITY:
      case WPI_CLOSED_LOOP_POSITION:
        return pidController.getI();
      case SRX_CLOSED_LOOP_VELOCITY:
      case SRX_CLOSED_LOOP_POSITION:
        return super.getI()/1023.0;
    }
    return 0;
  }

  @Override
  public double getD() {
    switch (meanMode) {
      case WPI_CLOSED_LOOP_VELOCITY:
      case WPI_CLOSED_LOOP_POSITION:
        return pidController.getD();
      case SRX_CLOSED_LOOP_VELOCITY:
      case SRX_CLOSED_LOOP_POSITION:
        return super.getD()/1023.0;
    }
    return 0;
  }

  @Override
  public double getF() {
    switch (meanMode) {
      case WPI_CLOSED_LOOP_VELOCITY:
        return pidController.getF();
      case SRX_CLOSED_LOOP_VELOCITY:
        return super.getF()/1023.0;
    }
    return 0;
  }

  @Override
  public void setSetpoint(double setpoint) {
    switch (meanMode) {
      case WPI_CLOSED_LOOP_VELOCITY:
      case WPI_CLOSED_LOOP_POSITION:
        pidController.setSetpoint(setpoint);
        break;
      case SRX_CLOSED_LOOP_VELOCITY:
      case SRX_CLOSED_LOOP_POSITION:
        super.setSetpoint(setpoint);
        break;
    }
  }

  @Override
  public double getSetpoint() {
    switch (meanMode) {
      case WPI_CLOSED_LOOP_VELOCITY:
      case WPI_CLOSED_LOOP_POSITION:
        return pidController.getSetpoint();
      case SRX_CLOSED_LOOP_VELOCITY:
      case SRX_CLOSED_LOOP_POSITION:
        return super.getSetpoint();
      case OPEN_LOOP:
        return SmartDashboard.getNumber("MOTOR_0_SETPOINT", 0.0);  // hack
    }
    return 0;
  }

  @Override
  public double getError() {
    switch (meanMode) {
      case WPI_CLOSED_LOOP_VELOCITY:
      case WPI_CLOSED_LOOP_POSITION:
        return pidController.getError();
      case SRX_CLOSED_LOOP_VELOCITY:
      case SRX_CLOSED_LOOP_POSITION:
        return super.getError();
      case OPEN_LOOP:
        return getSetpoint() - getSpeed();  // hack and somewhat meaningless.  getSetpoint() - getPosition() ?
    }
    return 0;
  }

  @Override
  public void enable() {
    switch (meanMode) {
      case WPI_CLOSED_LOOP_VELOCITY:
        accumulatePower = 0;
        pidController.enable();
        break;
      case WPI_CLOSED_LOOP_POSITION:
        pidController.enable();
        break;
      case SRX_CLOSED_LOOP_VELOCITY:
        super.changeControlMode(TalonControlMode.Speed);
        break;
      case SRX_CLOSED_LOOP_POSITION:
        super.changeControlMode(TalonControlMode.Position);
        break;
    }
  }

  @Override
  public void disable() {
    switch (meanMode) {
      case WPI_CLOSED_LOOP_VELOCITY:
      case WPI_CLOSED_LOOP_POSITION:
        pidController.disable();
        break;
      case SRX_CLOSED_LOOP_VELOCITY:
      case SRX_CLOSED_LOOP_POSITION:
        super.changeControlMode(TalonControlMode.PercentVbus);
        break;
    }
  }

  @Override
  public boolean isEnabled() {
    switch (meanMode) {
      case WPI_CLOSED_LOOP_VELOCITY:
      case WPI_CLOSED_LOOP_POSITION:
        return pidController.isEnabled();
      case SRX_CLOSED_LOOP_VELOCITY:
      case SRX_CLOSED_LOOP_POSITION:
        return super.isEnabled();
    }
    return true;
  }

  @Override
  public void reset() {
    switch (meanMode) {
      case WPI_CLOSED_LOOP_VELOCITY:
        pidController.reset();
        break;
      case SRX_CLOSED_LOOP_VELOCITY:
        super.reset();
        break;
    }
  }

  public double getOutput() {
    return getOutputVoltage() / 12.0 + Math.random() / 1000.0;  // hack so that the graph will always update - it ignores identical sequential values
  }

  public void calibrateFeedForward() {
    switch (meanMode) {
      case WPI_CLOSED_LOOP_VELOCITY:
        Kf = getOutput() / getSetpoint();
        break;
      case SRX_CLOSED_LOOP_VELOCITY:
        setF( getOutput() / getSetpoint());  // this one may need scaled to native units (1023.0) ?
        break;
    }
  }
}
