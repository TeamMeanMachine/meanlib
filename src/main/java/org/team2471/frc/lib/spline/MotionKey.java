package org.team2471.frc.lib.spline;

public class MotionKey {
  private double dTime;
  private double dValue;

  private MotionKey nextKey;
  private MotionKey previousKey;
  private MotionCurve motionCurve;

  public MotionKey() {
    nextKey = null;
    previousKey = null;
    dTime = 0.0;
    dValue = 0.0;
  }

  public MotionKey(double time, double value) {  // adds the new key to the end - todo: seek to correct value location, insert there
    nextKey = null;
    previousKey = motionCurve.getTailKey();
    motionCurve.setTailKey(this);
    dTime = time;
    dValue = value;
  }
}
