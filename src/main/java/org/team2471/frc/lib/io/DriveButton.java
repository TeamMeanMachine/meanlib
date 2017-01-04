package org.team2471.frc.lib.io;

import java.util.function.Function;

@FunctionalInterface
public interface DriveButton {
  boolean get();

  default DriveButton map(Function<Boolean, Boolean> function) {
    return () -> function.apply(get());
  }

  default DriveButton withInvert() {
    return map(value -> !value);
  }
}
