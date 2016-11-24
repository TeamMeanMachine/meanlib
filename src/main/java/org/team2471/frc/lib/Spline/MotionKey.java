package org.team2471.frc.lib.Spline;

public class MotionKey {
  private double m_dTime;
  private double m_dValue;

  private MotionKey m_nextKey;
  private MotionKey m_prevKey;
  private MotionCurve m_motionCurve;

  public MotionKey() {
    m_nextKey = null;
    m_prevKey = null;
    m_dTime = 0.0;
    m_dValue = 0.0;
  }

  public MotionKey(double time, double value) {  // adds the new key to the end - todo: seek to correct value location, insert there
    m_nextKey = null;
    m_prevKey = m_motionCurve.getTailKey();
    m_motionCurve.setTailKey(this);
    m_dTime = time;
    m_dValue = value;
  }



}
