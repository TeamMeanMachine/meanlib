package org.team2471.frc.lib.io;

@FunctionalInterface
public interface ControllerDPad {
  double get();

  default boolean isIdle() {
    return get() == -1;
  }

  default boolean isUp() {
    return get() == 0;
  }

  default boolean isUpperRight() {
    return get() == 45;
  }

  default boolean isRight() {
    return get() == 90;
  }

  default boolean isLowerRight() {
    return get() == 135;
  }

  default boolean isDown() {
    return get() == 180;
  }

  default boolean isLowerLeft() {
    return get() == 225;
  }

  default boolean isLeft() {
    return get() == 270;
  }

  default boolean isUpperLeft() {
    return get() == 315;
  }
}
