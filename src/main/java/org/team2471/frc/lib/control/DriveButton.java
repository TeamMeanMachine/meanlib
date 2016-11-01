package org.team2471.frc.lib.control;

import java.util.function.Function;

@FunctionalInterface
public interface DriveButton {
  boolean get();

  default DriveButton map(Function<Boolean, Boolean> function) {
    return () -> function.apply(get());
  }

  default DriveButton invert() {
    return map(value -> !value);
  }
}
