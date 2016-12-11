package org.team2471.frc.lib.control;

import edu.wpi.first.wpilibj.PIDOutput;
import edu.wpi.first.wpilibj.Timer;

public class RateLimitedPIDOutput implements PIDOutput {
  private final double maxRate;
  private final PIDOutput output;

  private double lastTimestamp = Timer.getFPGATimestamp();
  private double lastOutput = 0;

  public RateLimitedPIDOutput(double rate, PIDOutput output) {
    this.maxRate = rate;
    this.output = output;
  }

  @Override
  public void pidWrite(double v) {
    double outputDifference = v - lastOutput;
    double currentTime = Timer.getFPGATimestamp();
    double timeDifference = currentTime - lastTimestamp;

    if (Math.abs(outputDifference) / timeDifference > maxRate) {
      v = lastOutput + Math.signum(outputDifference) * maxRate * timeDifference;
    }

    output.pidWrite(v);

    lastTimestamp = currentTime;
    lastOutput = v;
  }
}
