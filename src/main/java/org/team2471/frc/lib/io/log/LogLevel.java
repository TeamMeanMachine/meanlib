package org.team2471.frc.lib.io.log;

public enum LogLevel {
  NONE(-1),
  TRACE(0),
  DEBUG(1),
  INFO(2),
  WARN(3),
  ERROR(4),
  FATAL(5);

  private final int strength;

  LogLevel(int strength) {
    this.strength = strength;
  }

  public int getStrength() {
    return strength;
  }
}
