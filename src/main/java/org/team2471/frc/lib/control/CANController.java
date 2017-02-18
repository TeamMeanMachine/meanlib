package org.team2471.frc.lib.control;

import com.ctre.CANTalon;
import edu.wpi.first.wpilibj.PIDInterface;

public class CANController extends CANTalon implements PIDInterface {

  public CANController( int canID ) {
    super( canID );
  }
}
