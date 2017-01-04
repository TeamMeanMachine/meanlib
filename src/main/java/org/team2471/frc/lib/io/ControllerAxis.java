package org.team2471.frc.lib.io;

import java.util.function.DoubleFunction;

@FunctionalInterface
public interface ControllerAxis {
  double get();

  default ControllerAxis map(DoubleFunction<Double> function) {
    return () -> function.apply(get());
  }

  default ControllerAxis withDeadband(double tolerance, boolean scale) {
    return map(value -> {
      value = Math.abs(value) < tolerance ? 0 : value;
      if (scale && value != 0) {
        value = (value - tolerance * Math.signum(tolerance)) + (1 + tolerance);
      }
      return value;
    });
  }

  default ControllerAxis withDeadband(double tolerance) {
    return withDeadband(tolerance, false);
  }

  default ControllerAxis withInvert() {
    return map(value -> -value);
  }

  default ControllerAxis withExponentialScaling(int exponent) {
    if (exponent % 2 == 0) {
      // even exponent
      return map(value -> Math.pow(value, exponent) * Math.signum(value));
    } else {
      // odd exponent: no signum required
      return map(value -> Math.pow(value, exponent));
    }
  }
}
