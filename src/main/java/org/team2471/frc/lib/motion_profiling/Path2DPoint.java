package org.team2471.frc.lib.motion_profiling;

import org.team2471.frc.lib.vector.Vector2;

import static org.team2471.frc.lib.motion_profiling.Path2DPoint.SlopeMethod.SLOPE_TANGENT_SPECIFIED;

public class Path2DPoint {
  private final int STEPS = 600;
  private Vector2 m_position;
  private Vector2 m_prevAngleAndMagnitude;
  private Vector2 m_nextAngleAndMagnitude;
  private Vector2 m_prevTangent;
  private Vector2 m_nextTangent;
  private boolean m_bTangentsDirty;
  private boolean m_bCoefficientsDirty;
  private CubicCoefficients1D m_xCoeff;
  private CubicCoefficients1D m_yCoeff;
  private double m_segmentLength;
  private double partialLength, prevPartialLength;
  private SlopeMethod m_prevSlopeMethod;
  private SlopeMethod m_nextSlopeMethod;

  private Path2DCurve m_path2DCurve;
  private Path2DPoint m_nextPoint;
  private Path2DPoint m_prevPoint;

  public Path2DPoint() {
    init();
  }

  public Path2DPoint(double x, double y) {
    init();
    m_position.set(x, y);
  }

  private void init() {
    m_position = new Vector2(0, 0);
    m_prevAngleAndMagnitude = new Vector2(0, 1.9);
    m_nextAngleAndMagnitude = new Vector2(0, 1.9);
    m_prevTangent = new Vector2(0, 0);
    m_nextTangent = new Vector2(0, 0);

    m_bTangentsDirty = true;
    m_bCoefficientsDirty = true;
    m_prevSlopeMethod = SlopeMethod.SLOPE_SMOOTH;
    m_nextSlopeMethod = SlopeMethod.SLOPE_SMOOTH;
    m_path2DCurve = null;
    m_nextPoint = null;
    m_prevPoint = null;
    m_segmentLength = 0;
    partialLength = -1;
  }

  public void onPositionChanged() {
    getPath2DCurve().onPositionChanged();  // tell the path too

    setTangentsDirty(true);
    setCoefficientsDirty(true);

    if (getPrevPoint() != null) {
      getPrevPoint().setTangentsDirty(true);
      getPrevPoint().setCoefficientsDirty(true);
    }

    if (getNextPoint() != null) {
      getNextPoint().setTangentsDirty(true);
    }
  }

  public boolean areTangentsDirty() {
    return m_bTangentsDirty;
  }

  public void setTangentsDirty(boolean bTangentsDirty) {
    m_bTangentsDirty = bTangentsDirty;
  }

  public boolean areCoefficientsDirty() {
    return m_bCoefficientsDirty;
  }

  public void setCoefficientsDirty(boolean bCoefficientsDirty) {
    m_bCoefficientsDirty = bCoefficientsDirty;
  }

  public Vector2 getPosition() {
    return m_position;
  }

  public void setPosition(Vector2 position) {
    this.m_position = position;
  }

  public Vector2 getPrevAngleAndMagnitude() {
    return m_prevAngleAndMagnitude;
  }

  public void setPrevAngleAndMagnitude(Vector2 m_prevAngleAndMagnitude) {
    this.m_prevAngleAndMagnitude = m_prevAngleAndMagnitude;
    setTangentsDirty(true);
  }

  public Vector2 getNextAngleAndMagnitude() {
    return m_nextAngleAndMagnitude;
  }

  public void setNextAngleAndMagnitude(Vector2 m_nextAngleAndMagnitude) {
    this.m_nextAngleAndMagnitude = m_nextAngleAndMagnitude;
    setTangentsDirty(true);
  }

  public Vector2 getPrevTangent() {
    if (areTangentsDirty())
      calculateTangents();

    return m_prevTangent;
  }

  public void setPrevTangent(Vector2 m_PrevTangent) {
    this.m_prevTangent = m_PrevTangent;
    m_prevSlopeMethod = SLOPE_TANGENT_SPECIFIED;
    setNextAngleAndMagnitude(new Vector2(0, 1));
  }

  public Vector2 getNextTangent() {
    if (areTangentsDirty())
      calculateTangents();

    return m_nextTangent;
  }

  public void setNextTangent(Vector2 m_NextTangent) {
    this.m_nextTangent = m_NextTangent;
    m_nextSlopeMethod = SLOPE_TANGENT_SPECIFIED;
    setNextAngleAndMagnitude(new Vector2(0, 1));
  }

  public Path2DCurve getPath2DCurve() {
    return m_path2DCurve;
  }

  public void setPath2DCurve(Path2DCurve path2DCurve) {
    m_path2DCurve = path2DCurve;
  }

  public Path2DPoint getNextPoint() {
    return m_nextPoint;
  }

  public void setNextPoint(Path2DPoint m_nextPoint) {
    this.m_nextPoint = m_nextPoint;
  }

  public Path2DPoint getPrevPoint() {
    return m_prevPoint;
  }

  public void setPrevPoint(Path2DPoint m_prevPoint) {
    this.m_prevPoint = m_prevPoint;
  }

  public SlopeMethod getPrevSlopeMethod() {
    return m_prevSlopeMethod;
  }

  public void setPrevSlopeMethod(SlopeMethod slopeMethod) {
    m_prevSlopeMethod = slopeMethod;
  }

  public SlopeMethod getNextSlopeMethod() {
    return m_nextSlopeMethod;
  }

  public void setNextSlopeMethod(SlopeMethod slopeMethod) {
    m_prevSlopeMethod = slopeMethod;
  }

  public double getPrevMagnitude() {
    return m_prevAngleAndMagnitude.y;
  }

  public double getNextMagnitude() {
    return m_nextAngleAndMagnitude.y;
  }

  void insertBefore(Path2DPoint newPoint) {
    m_prevPoint = newPoint.m_prevPoint;
    if (newPoint.m_prevPoint != null)
      newPoint.m_prevPoint.m_nextPoint = this;
    newPoint.m_prevPoint = this;
    m_nextPoint = newPoint;
  }

  void insertAfter(Path2DPoint newPoint) {
    m_nextPoint = newPoint.m_nextPoint;
    if (newPoint.m_nextPoint != null)
      newPoint.m_nextPoint.m_prevPoint = this;
    newPoint.m_nextPoint = this;
    m_prevPoint = newPoint;
  }

  private void calculateTangents() {
    setTangentsDirty(false);

    boolean bCalcSmoothPrev = false;
    boolean bCalcSmoothNext = false;

    switch (getPrevSlopeMethod()) {
      case SLOPE_MANUAL:
        m_prevTangent.set(Math.cos(getPrevAngleAndMagnitude().x), Math.sin(getPrevAngleAndMagnitude().x));
        if (m_prevPoint != null)
          Vector2.multiply(m_prevTangent, getPosition().x - m_prevPoint.getPosition().x);
        break;
      case SLOPE_LINEAR:
        if (m_prevPoint != null)
          m_prevTangent = Vector2.subtract(getPosition(), m_prevPoint.getPosition());
        break;
      case SLOPE_SMOOTH:
        bCalcSmoothPrev = true;
        break;
      case SLOPE_TANGENT_SPECIFIED:
        break;
    }

    switch (getNextSlopeMethod()) {
      case SLOPE_MANUAL:
        m_nextTangent.set(Math.cos(getNextAngleAndMagnitude().x), Math.sin(getNextAngleAndMagnitude().x));
        if (m_nextPoint != null)
          Vector2.multiply(m_nextTangent, m_nextPoint.getPosition().x - getPosition().x);
        break;
      case SLOPE_LINEAR:
        if (m_nextPoint != null)
          m_nextTangent = Vector2.subtract(m_nextPoint.getPosition(), getPosition());
        break;
      case SLOPE_SMOOTH:
        bCalcSmoothNext = true;
        break;
      case SLOPE_TANGENT_SPECIFIED:
        break;
    }

    if (bCalcSmoothPrev || bCalcSmoothNext) {
      if (m_prevPoint != null && m_nextPoint != null) {
        Vector2 delta = Vector2.subtract(m_nextPoint.getPosition(), m_prevPoint.getPosition());
        double weight = Math.abs(delta.x);
        if (weight == 0) // if points are on top of one another (no tangents)
        {
          if (bCalcSmoothPrev)
            m_prevTangent.set(0, 0);
          if (bCalcSmoothNext)
            m_nextTangent.set(0, 0);
        } else {
          delta = Vector2.divide(delta, weight);

          if (bCalcSmoothPrev) {
            double prevWeight = Vector2.length(Vector2.subtract(getPosition(), m_prevPoint.getPosition()));
            m_prevTangent = Vector2.multiply(delta, prevWeight);
          }
          if (bCalcSmoothNext) {
            double nextWeight = Vector2.length(Vector2.subtract(m_nextPoint.getPosition(), getPosition()));
            m_nextTangent = Vector2.multiply(delta, nextWeight);
          }
        }
      } else {
        if (m_nextPoint != null) {
          if (bCalcSmoothPrev)
            m_prevTangent = Vector2.subtract(m_nextPoint.getPosition(), getPosition());

          if (bCalcSmoothNext)
            m_nextTangent = Vector2.subtract(m_nextPoint.getPosition(), getPosition());
        }

        if (m_prevPoint != null) {
          if (bCalcSmoothPrev)
            m_prevTangent = Vector2.subtract(getPosition(), m_prevPoint.getPosition());

          if (bCalcSmoothNext)
            m_nextTangent = Vector2.subtract(getPosition(), m_prevPoint.getPosition());
        }
      }
    }

    m_prevTangent = Vector2.multiply(m_prevTangent, getPrevAngleAndMagnitude().y);
    m_nextTangent = Vector2.multiply(m_nextTangent, getNextAngleAndMagnitude().y);
  }

  public CubicCoefficients1D getXCoefficients() {
    if (areCoefficientsDirty()) {
      calculateCoefficientsAndLength();
    }
    return m_xCoeff;
  }

  public CubicCoefficients1D getYCoefficients() {
    if (areCoefficientsDirty()) {
      calculateCoefficientsAndLength();
    }
    return m_yCoeff;
  }

  private void calculateCoefficientsAndLength() {
    setCoefficientsDirty(false);

    double pointax = getPosition().x;
    double pointbx = m_nextPoint.getPosition().x;
    double pointcx = getNextTangent().x;
    double pointdx = m_nextPoint.getPrevTangent().x;
    m_xCoeff = new CubicCoefficients1D(pointax, pointbx, pointcx, pointdx);

    double pointay = getPosition().y;
    double pointby = m_nextPoint.getPosition().y;
    double pointcy = getNextTangent().y;
    double pointdy = m_nextPoint.getPrevTangent().y;
    m_yCoeff = new CubicCoefficients1D(pointay, pointby, pointcy, pointdy);

    // calculate segment length
    Vector2 pos = new Vector2(0, 0);
    Vector2 prevPos = new Vector2(0, 0);
    m_xCoeff.initFD(STEPS);
    m_yCoeff.initFD(STEPS);
    m_segmentLength = 0;
    prevPos.set(m_xCoeff.getFDValue(), m_yCoeff.getFDValue());

    for (int i = 0; i < STEPS; i++) {
      pos.set(m_xCoeff.bumpFDFaster(), m_yCoeff.bumpFDFaster());
      m_segmentLength += Vector2.length(Vector2.subtract(pos, prevPos));
      prevPos.set(pos.x, pos.y);
    }
  }

  public double getSegmentLength() {
    if (areCoefficientsDirty()) {
      calculateCoefficientsAndLength();
    }
    return m_segmentLength;
  }

  public Vector2 getPositionAtDistance(double distance) {

    Vector2 pos = new Vector2(0, 0);
    Vector2 prevPos = new Vector2(0, 0);

    if (partialLength < 0 || partialLength > distance) {
      m_xCoeff.initFD(STEPS);
      m_yCoeff.initFD(STEPS);
      partialLength = 0;
    }

    while (partialLength <= distance) {
      pos.set(m_xCoeff.bumpFD(), m_yCoeff.bumpFD());
      prevPos.set(m_xCoeff.getFdPrevValue(), m_yCoeff.getFdPrevValue());
      prevPartialLength = partialLength;
      partialLength += Vector2.length(Vector2.subtract(pos, prevPos));
    }

    double intoSegment = (distance - prevPartialLength) / (partialLength - prevPartialLength);  // linearly interpolate t based on distance of the surrounding steps

    return Vector2.add(Vector2.multiply(prevPos, 1.0f - intoSegment), Vector2.multiply(pos, intoSegment));
  }

  public Vector2 getTangentAtDistance(double distance) {
    Vector2 pos = new Vector2(0, 0);
    Vector2 prevPos = new Vector2(0, 0);

    if (partialLength < 0 || partialLength > distance) {
      m_xCoeff.initFD(STEPS);
      m_yCoeff.initFD(STEPS);
      partialLength = 0;
    }

    while (partialLength <= distance) {
      pos.set(m_xCoeff.bumpFD(), m_yCoeff.bumpFD());
      prevPos.set(m_xCoeff.getFdPrevValue(), m_yCoeff.getFdPrevValue());
      prevPartialLength = partialLength;
      partialLength += Vector2.length(Vector2.subtract(pos, prevPos));
    }

    return Vector2.subtract(pos, prevPos);
  }

  public enum SlopeMethod {
    SLOPE_MANUAL, SLOPE_LINEAR, SLOPE_SMOOTH, SLOPE_TANGENT_SPECIFIED
  }

  public String toString() {
    String rValue = "";
    rValue += m_position.toString();
    rValue += m_prevAngleAndMagnitude.toString();
    rValue += m_nextAngleAndMagnitude.toString();
    rValue += m_prevTangent.toString();
    rValue += m_nextTangent.toString();
    rValue += m_prevSlopeMethod.toString();
    rValue += m_nextSlopeMethod.toString();
    return rValue;
  }
}
