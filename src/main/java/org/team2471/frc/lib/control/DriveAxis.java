package org.team2471.frc.lib.control;

import java.util.function.DoubleFunction;

@FunctionalInterface
public interface DriveAxis {
  double get();

  default DriveAxis map(DoubleFunction<Double> function) {
    return () -> function.apply(get());
  }

  default DriveAxis withDeadband(double tolerance, boolean scale) {
    return map(value -> {
      value = Math.abs(value) < tolerance ? 0 : value;
      if(scale && value != 0) {
        value = (value - tolerance * Math.signum(tolerance)) + (1 + tolerance);
      }
      return value;
    });
  }

  default DriveAxis withDeadband(double tolerance) {
    return withDeadband(tolerance, false);
  }

  default DriveAxis withInvert() {
    return map(value -> -value);
  }

  default DriveAxis withExponentialScaling(int exponent) {
    if(exponent % 2 == 0) {
      // even exponent
      return map(value -> Math.pow(value, exponent) * Math.signum(value));
    } else {
      // odd exponent: no signum required
      return map(value -> Math.pow(value, exponent));
    }
  }
}
