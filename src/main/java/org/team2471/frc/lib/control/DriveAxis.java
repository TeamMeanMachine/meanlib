package org.team2471.frc.lib.control;

import java.util.function.DoubleFunction;

@FunctionalInterface
public interface DriveAxis {
  double get();

  default DriveAxis map(DoubleFunction<Double> function) {
    return () -> function.apply(get());
  }

  default DriveAxis applyDeadband(double tolerance, boolean scale) {
    return map(value -> {
      value = Math.abs(value) < tolerance ? 0 : value;
      if(scale) {
        value *= 1 - tolerance;
      }
      return value;
    });
  }

  default DriveAxis applyDeadband(double tolerance) {
    return applyDeadband(tolerance, false);
  }

  default DriveAxis applyInvert() {
    return map(value -> -value);
  }

  default DriveAxis applyExponentialScaling(int exponent) {
    if(exponent % 2 == 0) {
      // even exponent
      return map(value -> Math.pow(value, exponent) * Math.signum(value));
    } else {
      // odd exponent: no signum required
      return map(value -> Math.pow(value, exponent));
    }
  }
}
