package org.team2471.frc.lib.motion_profiling;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import org.team2471.frc.lib.vector.Vector2;

public class Path2D {

    public String name;

    private Path2DCurve m_xyCurve;    // positive y is forward in robot space, and positive x is to the robot's right
    private MotionCurve m_easeCurve;  // the ease curve is the percentage along the path the robot as a function of time

    public enum RobotDirection {
        FORWARD, BACKWARD
    }

    private double speed = 1.0;
    private RobotDirection robotDirection = RobotDirection.FORWARD;
    private boolean m_mirrored = false;

    // calculation storage
    private transient Vector2 m_prevCenterPositionForLeft;
    private transient Vector2 m_prevCenterPositionForRight;
    private transient Vector2 m_prevLeftPosition;
    private transient Vector2 m_prevRightPosition;
    private transient double leftDistance;
    private transient double rightDistance;
    private transient Autonomous autonomous;

    public Path2D() {
        m_xyCurve = new Path2DCurve();
        m_easeCurve = new MotionCurve();
    }

    public Path2D(String name) {
        this.name = name;
        m_xyCurve = new Path2DCurve();
        m_easeCurve = new MotionCurve();
    }

    public static Path2D fromJsonString(String jsonString) {
        return null; // TODO: parse the json string and return the path
    }

    public void addPointAndTangent(double x, double y, double xTangent, double yTangent) {
        m_xyCurve.addPointToEnd(x, y, xTangent, yTangent);
    }

    public boolean hasPoints() {
        return m_xyCurve.getHeadPoint() != null;
    }

    public Path2DPoint addVector2(Vector2 point) {
        return addPoint(point.getX(), point.getY());
    }

    public Path2DPoint addVector2After(Vector2 point, Path2DPoint after) {
        return m_xyCurve.addPointAfter(point, after);
    }

    public Path2DPoint addPoint(double x, double y) {
        return m_xyCurve.addPointToEnd(x, y);
    }

    public void addPointAngleAndMagnitude(double x, double y, double angle, double magnitude) {
        m_xyCurve.addPointAngleAndMagnitudeToEnd(x, y, angle, magnitude);
    }

    public void removePoint(Path2DPoint path2DPoint) {
        m_xyCurve.removePoint(path2DPoint);
    }

    public void addEasePoint(double time, double value) {
        m_easeCurve.storeValue(time, value);
    }

    public void removeAllEasePoints() {
        m_easeCurve.removeAllPoints();
    }

    public void addEasePointSlopeAndMagnitude(double time, double value, double slope, double magnitude) {
        m_easeCurve.storeValueSlopeAndMagnitude(time, value, slope, magnitude);
    }

    public Vector2 getPosition(double time) {
        if (m_easeCurve.getHeadKey() != null) {
            if (speed > 0)
                return getPositionAtEase(m_easeCurve.getValue(time * speed));
            else
                return getPositionAtEase(m_easeCurve.getValue(getDuration() - time * -speed));
        } else {
            if (speed > 0)
                return getPositionAtEase(time / 5.0 * speed);  // take 5 seconds to finish path (linear motion)
            else
                return getPositionAtEase(getDuration() - time / 5.0 * -speed);
        }
    }

    public Vector2 getTangent(double time) {
        if (m_easeCurve.getHeadKey() != null) {
            if (speed > 0)
                return getTangentAtEase(m_easeCurve.getValue(time * speed));
            else
                return getTangentAtEase(m_easeCurve.getValue(getDuration() - time * -speed));
        } else {
            if (speed > 0)
                return getTangentAtEase(time / 5.0 * speed);  // take 5 seconds to finish path (linear motion)
            else
                return getTangentAtEase(getDuration() - time / 5.0 * -speed);
        }
    }

    public Vector2 getPositionAtEase(double ease) {
        double totalDistance = m_xyCurve.getLength();
        Vector2 rValue = m_xyCurve.getPositionAtDistance(ease * totalDistance);
        if (m_mirrored)
            rValue.setX(-rValue.getX());
        return rValue;
    }

    public Vector2 getTangentAtEase(double ease) {
        double totalDistance = m_xyCurve.getLength();
        Vector2 rValue = m_xyCurve.getTangentAtDistance(ease * totalDistance);
        if (m_mirrored)
            rValue.setX(-rValue.getX());
        return rValue;
    }

    public Vector2 getSidePosition(double time, double xOffset) {  // offset can be positive or negative (half the width of the robot)
        Vector2 centerPosition = getPosition(time);
        Vector2 tangent = getTangent(time);
        tangent = Vector2.Companion.normalize(tangent);
        tangent = Vector2.Companion.perpendicular(tangent);
        if (speed<0.0)
            xOffset = -xOffset;
        tangent = Vector2.Companion.multiply(tangent, xOffset);
        Vector2 sidePosition = Vector2.Companion.add(centerPosition, tangent);
        return sidePosition;
    }

    public Vector2 getLeftPosition(double time) {
        if (robotDirection==RobotDirection.FORWARD)
            return getSidePosition(time, -autonomous.getTrackWidth() / 2.0);
        else
            return getSidePosition(time, autonomous.getTrackWidth() / 2.0);
    }

    public Vector2 getRightPosition(double time) {
        if (robotDirection==RobotDirection.FORWARD)
            return getSidePosition(time, autonomous.getTrackWidth() / 2.0);
        else
            return getSidePosition(time, -autonomous.getTrackWidth() / 2.0);
    }

    public double getLeftPositionDelta(double time) {
        if (m_prevLeftPosition == null) {
            m_prevCenterPositionForLeft = getPosition(time);
            m_prevLeftPosition = getLeftPosition(time);
            return 0.0;
        }

        Vector2 centerPosition = getPosition(time);
        Vector2 leftPosition = getLeftPosition(time);
        Vector2 deltaCenter = Vector2.Companion.subtract(centerPosition, m_prevCenterPositionForLeft);
        Vector2 deltaLeft = Vector2.Companion.subtract(leftPosition, m_prevLeftPosition);
        m_prevCenterPositionForLeft = centerPosition;
        m_prevLeftPosition = leftPosition;

        double result;
        if (Vector2.Companion.dot(deltaCenter, deltaLeft) > 0) {
            result = Vector2.Companion.length(deltaLeft) * autonomous.getScrubFactor();
        } else {
            result = -Vector2.Companion.length(deltaLeft) * autonomous.getScrubFactor();
        }
        if (robotDirection==RobotDirection.FORWARD)
            return result;
        else
            return -result;
    }

    public double getRightPositionDelta(double time) {
        if (m_prevRightPosition == null) {
            m_prevCenterPositionForRight = getPosition(time);
            m_prevRightPosition = getRightPosition(time);
            return 0.0;
        }

        Vector2 centerPosition = getPosition(time);
        Vector2 rightPosition = getRightPosition(time);
        Vector2 deltaCenter = Vector2.Companion.subtract(centerPosition, m_prevCenterPositionForRight);
        Vector2 deltaRight = Vector2.Companion.subtract(rightPosition, m_prevRightPosition);
        m_prevCenterPositionForRight = centerPosition;
        m_prevRightPosition = rightPosition;

        double result;
        if (Vector2.Companion.dot(deltaCenter, deltaRight) > 0) {
            result = Vector2.Companion.length(deltaRight) * autonomous.getScrubFactor();
        } else {
            result = -Vector2.Companion.length(deltaRight) * autonomous.getScrubFactor();
        }
        if (robotDirection==RobotDirection.FORWARD)
            return result;
        else
            return -result;
    }

    public void resetDistances() {
        leftDistance = rightDistance = 0.0;
        m_prevCenterPositionForLeft = null;
        m_prevCenterPositionForRight = null;
        m_prevLeftPosition = null;
        m_prevRightPosition = null;
    }

    public double getLeftDistance(double time) {
        leftDistance += getLeftPositionDelta(time);
        return leftDistance;
    }

    public double getRightDistance(double time) {
        rightDistance += getRightPositionDelta(time);
        return rightDistance;
    }

    public double getDuration() {
        if (m_easeCurve != null)
            return m_easeCurve.getLength();
        else
            return 5.0;
    }

    public double getDurationWithSpeed() {
        if (m_easeCurve != null)
            return m_easeCurve.getLength() / Math.abs(speed);
        else
            return 5.0;
    }

    public void setDuration(double seconds) {
        if (m_easeCurve != null && m_easeCurve.getTailKey() != null)
            m_easeCurve.getTailKey().setTime(seconds);
    }

    public MotionCurve getEaseCurve() {
        return m_easeCurve;
    }

    public RobotDirection getRobotDirection() {
        return robotDirection;
    }

    public void setRobotDirection(RobotDirection robotDirection) {
        this.robotDirection = robotDirection;
    }

    public boolean isMirrored() {
        return m_mirrored;
    }

    public void setMirrored(boolean mirrored) {
        this.m_mirrored = mirrored;
    }

    public String toString() {
        String rValue = "";
        for (Path2DPoint point = m_xyCurve.getHeadPoint(); point != null; point = point.getNextPoint()) {
            rValue += point.toString();
        }
        return rValue;
    }

    public Path2DCurve getXYCurve() {
        return m_xyCurve;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String toJsonString() {
        Moshi moshi = new Moshi.Builder().build();
        JsonAdapter<Path2D> jsonAdapter = moshi.adapter(Path2D.class);

        String json = jsonAdapter.toJson(this);
        System.out.println(json);
        return json;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public Autonomous getAutonomous() {
        return autonomous;
    }

    public void setAutonomous(Autonomous autonomous) {
        this.autonomous = autonomous;
    }

    void fixUpTailAndPrevPointers() {
        m_xyCurve.fixUpTailAndPrevPointers();
        m_easeCurve.fixUpTailAndPrevPointers();
    }
}

