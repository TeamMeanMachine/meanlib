package org.team2471.frc.lib.io;

import org.team2471.frc.lib.control.CommandTrigger;

import java.util.function.BooleanSupplier;
import java.util.function.Function;

@FunctionalInterface
public interface ControllerButton {
  boolean get();

  default ControllerButton map(Function<Boolean, Boolean> function) {
    return () -> function.apply(get());
  }

  default ControllerButton withInvert() {
    return map(value -> !value);
  }

  default ControllerButton and(BooleanSupplier condition) {
    return map(bool -> bool && condition.getAsBoolean());
  }

  default ControllerButton or(BooleanSupplier condition) {
    return map(bool -> bool || condition.getAsBoolean());
  }

  default CommandTrigger asTrigger() {
    return new CommandTrigger(this::get);
  }
}
