package org.team2471.frc.lib.control;


import java.util.function.BooleanSupplier;

import edu.wpi.first.wpilibj.buttons.Trigger;

public class CommandTrigger extends Trigger {
  private final BooleanSupplier condition;

  public CommandTrigger(BooleanSupplier condition) {
    this.condition = condition;
  }

  @Override
  public boolean get() {
    return condition.getAsBoolean();
  }
}
