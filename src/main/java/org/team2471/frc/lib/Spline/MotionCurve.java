package org.team2471.frc.lib.Spline;

public class MotionCurve {
  private MotionKey m_HeadKey;
  private MotionKey m_TailKey;

  public MotionKey getHeadKey() {
    return m_HeadKey;
  }

  public void setHeadKey(MotionKey headKey) {
    this.m_HeadKey = headKey;
  }

  public MotionKey getTailKey() {
    return m_TailKey;
  }

  public void setTailKey(MotionKey tailKey) {
    this.m_TailKey = tailKey;
  }
}
