package org.team2471.frc.lib.spline;

public class MotionCurve {
  private MotionKey headKey;
  private MotionKey tailKey;

  public MotionKey getHeadKey() {
    return headKey;
  }

  public void setHeadKey(MotionKey headKey) {
    this.headKey = headKey;
  }

  public MotionKey getTailKey() {
    return tailKey;
  }

  public void setTailKey(MotionKey tailKey) {
    this.tailKey = tailKey;
  }
}
