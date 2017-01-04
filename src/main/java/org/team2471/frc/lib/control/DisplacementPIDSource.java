package org.team2471.frc.lib.control;

import java.util.function.DoubleSupplier;

import edu.wpi.first.wpilibj.PIDSource;
import edu.wpi.first.wpilibj.PIDSourceType;

@FunctionalInterface
public interface DisplacementPIDSource extends PIDSource {
  static DisplacementPIDSource of(DoubleSupplier supplier) {
    return supplier::getAsDouble;
  }

  @Override
  default PIDSourceType getPIDSourceType() {
    return PIDSourceType.kDisplacement;
  }

  @Override
  default void setPIDSourceType(PIDSourceType pidSourceType) {
  }
}
