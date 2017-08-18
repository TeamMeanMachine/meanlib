package org.team2471.frc.lib.motion_profiling;

import org.team2471.frc.lib.vector.Vector2;

public class Path2DCurve {

  private Path2DPoint m_headPoint;
  private Path2DPoint m_tailPoint;
  private Path2DPoint m_lastAccessedPoint;
  private double m_cachedLength;
  private double m_lengthRemaining;

  public Path2DCurve() {
    m_headPoint = null;
    m_tailPoint = null;
    m_lastAccessedPoint = null;
    m_cachedLength = -1.0;
  }

  private void insertPointBefore(Path2DPoint atKey, Path2DPoint newKey) {
    newKey.setPath2DCurve(this);

    if (atKey == m_headPoint) {
      m_headPoint = newKey;
      if (m_tailPoint == null)
        m_tailPoint = m_headPoint;
    }

    m_lastAccessedPoint = newKey;
    m_cachedLength = -1.0;

    if (atKey != null) {
      newKey.insertBefore(atKey);
    }

    newKey.onPositionChanged();
  }

  private void insertPointAfter(Path2DPoint atPoint, Path2DPoint newPoint) {

    newPoint.setPath2DCurve(this);

    if (atPoint == m_tailPoint) {
      m_tailPoint = newPoint;
      if (m_headPoint == null)
        m_headPoint = m_tailPoint;
    }

    m_lastAccessedPoint = newPoint;
    m_cachedLength = -1.0;

    if (atPoint != null) {
      newPoint.insertAfter(atPoint);
    }

    newPoint.onPositionChanged();
  }

  void addPointToEnd(double x, double y)  // adds a path point to the end
  {
    Path2DPoint path2DPoint = new Path2DPoint(x, y);
    insertPointAfter(m_tailPoint, path2DPoint);
  }

  public void addPointToEnd(double x, double y, double xTangent, double yTangent) {
    Path2DPoint path2DPoint = new Path2DPoint(x, y);
    insertPointAfter(m_tailPoint, path2DPoint);
    Vector2 tangent = new Vector2(xTangent, yTangent);
    path2DPoint.setNextTangent(tangent);
    path2DPoint.setPrevTangent(tangent);
  }

  public void addPointAngleAndMagnitudeToEnd(double x, double y, double angle, double magnitude) {
    angle += 90.0;  // 0 degrees is in front of robot (positive y)
    angle *= Math.PI / 180.0;  // degrees to radians
    Path2DPoint path2DPoint = new Path2DPoint(x, y);
    insertPointAfter(m_tailPoint, path2DPoint);
    Vector2 angleAndMagnitude = new Vector2(angle, magnitude);
    path2DPoint.setNextAngleAndMagnitude(angleAndMagnitude);
    path2DPoint.setPrevAngleAndMagnitude(angleAndMagnitude);
  }

  public Vector2 getPositionAtDistance(double distance) {
    Path2DPoint point = getPointBefore(distance);
    if (point == null) {  // distance exceeds path length
      return m_tailPoint.getPosition();
    }
    return point.getPositionAtDistance(m_lengthRemaining);
  }

  public Vector2 getTangentAtDistance(double distance) {
    Path2DPoint point = getPointBefore(distance);
    if (point == null) {  // distance exceeds path length
      return m_tailPoint.getNextTangent();
    }
    return point.getTangentAtDistance(m_lengthRemaining);
  }

  private Path2DPoint getPointBefore(double distance) {
    double length = 0;
    for (Path2DPoint point = m_headPoint; point != null && point.getNextPoint() != null; point = point.getNextPoint()) {  // should make this incremental
      length += point.getSegmentLength();
      if (length > distance) {
        length -= point.getSegmentLength();
        m_lengthRemaining = distance - length;
        return point;
      }
    }
    return null;
  }

  public double getLength() {
    if (m_cachedLength > 0) {
      return m_cachedLength;
    }

    m_cachedLength = 0;
    for (Path2DPoint point = m_headPoint; point != null && point.getNextPoint() != null; point = point.getNextPoint()) {
      m_cachedLength += point.getSegmentLength();
    }
    return m_cachedLength;
  }

  public void onPositionChanged() {
    m_cachedLength = -1.0;
  }

  public Path2DPoint getHeadPoint() {
    return m_headPoint;
  }
}