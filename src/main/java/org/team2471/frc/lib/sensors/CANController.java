package org.team2471.frc.lib.sensors;

import edu.wpi.first.wpilibj.CANTalon;
import edu.wpi.first.wpilibj.PIDInterface;


public class CANController extends CANTalon implements PIDInterface {

  public CANController(int id) {
    super(id);
  }

  public CANController(double p, double i, double d, int id) {
    super(id);
    setPID(p, i, d);
  }
}
