package org.team2471.frc.lib.motion_profiling;

import org.team2471.frc.lib.math.Vector2;

public class Path2DCurve {

    private Path2DPoint m_headPoint;
    private transient Path2DPoint m_tailPoint;
    private transient Path2DPoint m_lastAccessedPoint;
    private transient double m_cachedLength;
    private transient double m_lengthRemaining;

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

    public Path2DPoint addPointAfter(Vector2 vec, Path2DPoint after) {
        Path2DPoint path2DPoint = new Path2DPoint(vec.getX(), vec.getY());
        insertPointAfter(after, path2DPoint);
        return path2DPoint;
    }

    public Path2DPoint addPointToEnd(double x, double y)  // adds a path point to the end
    {
        Path2DPoint path2DPoint = new Path2DPoint(x, y);
        insertPointAfter(m_tailPoint, path2DPoint);
        return path2DPoint;
    }

    public void addPointToEnd(double x, double y, double xTangent, double yTangent) {
        Path2DPoint path2DPoint = new Path2DPoint(x, y);
        insertPointAfter(m_tailPoint, path2DPoint);
        Vector2 tangent = new Vector2(xTangent, yTangent);
        path2DPoint.setNextTangent(tangent);
    }

    public void addPointAngleAndMagnitudeToEnd(double x, double y, double angle, double magnitude) {
        Path2DPoint path2DPoint = new Path2DPoint(x, y);
        insertPointAfter(m_tailPoint, path2DPoint);
        Vector2 angleAndMagnitude = new Vector2(angle, magnitude);
        path2DPoint.setNextAngleAndMagnitude(angleAndMagnitude);
        path2DPoint.setPrevAngleAndMagnitude(angleAndMagnitude);
    }

    public void removePoint(Path2DPoint path2DPoint) {
        if (path2DPoint.getPrevPoint() != null) {
            path2DPoint.getPrevPoint().setNextPoint(path2DPoint.getNextPoint());
            path2DPoint.getPrevPoint().onPositionChanged();
        } else {
            m_headPoint = path2DPoint.getNextPoint();
        }

        if (path2DPoint.getNextPoint() != null) {
            path2DPoint.getNextPoint().setPrevPoint(path2DPoint.getPrevPoint());
            path2DPoint.getNextPoint().onPositionChanged();
        } else {
            m_tailPoint = path2DPoint.getPrevPoint();
        }
    }

    public Vector2 getPositionAtDistance(double distance) {
        Path2DPoint point = getPointBefore(distance);
        if (point == null) {
            if (m_tailPoint != null)
                return new Vector2(m_tailPoint.getPosition().getX(), m_tailPoint.getPosition().getY());
            else
                return new Vector2(0.0, 0.0);
        }
        return point.getPositionAtDistance(m_lengthRemaining);
    }

    public Vector2 getTangentAtDistance(double distance) {
        Path2DPoint point = getPointBefore(distance);
        if (point == null) {  // distance exceeds path length
            if (m_tailPoint!=null)
                return new Vector2(m_tailPoint.getNextTangent().getX(), m_tailPoint.getNextTangent().getY());
            else
                return new Vector2(0.0, 0.0);
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

    public Path2DPoint getTailPoint() {
        return m_tailPoint;
    }

    void fixUpTailAndPrevPointers() {
        Path2DPoint prevPoint = null;
        for (Path2DPoint point = m_headPoint; point != null; point = point.getNextPoint()) {
            point.setPrevPoint(prevPoint);
            point.setPath2DCurve(this);
            prevPoint = point;
        }
        m_tailPoint = prevPoint;
    }
}
