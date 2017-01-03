package org.team2471.frc.lib.control;

import java.util.function.DoubleSupplier;

import edu.wpi.first.wpilibj.PIDSource;
import edu.wpi.first.wpilibj.PIDSourceType;

@FunctionalInterface
public interface RatePIDSource extends PIDSource {
  @Override
  default void setPIDSourceType(PIDSourceType pidSourceType) {
  }

  @Override
  default PIDSourceType getPIDSourceType() {
    return PIDSourceType.kRate;
  }

  static RatePIDSource of(DoubleSupplier supplier) {
    return supplier::getAsDouble;
  }
}
