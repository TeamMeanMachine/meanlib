package org.team2471.frc.lib.motion_profiling;

import com.sun.javafx.tk.Toolkit;
import edu.wpi.first.wpilibj.networktables.NetworkTable;
import org.team2471.frc.lib.vector.Vector2;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class Path2D {

  private Path2DCurve m_xyCurve;    // positive y is forward in robot space, and positive x is to the robot's right
  private MotionCurve m_easeCurve;  // the ease curve is the percentage along the path the robot as a function of time

  private double m_robotWidth = 35.0 / 12.0 * 1.096;  // average FRC robots are 28 inches wide, converted to feet. // seems like this belongs in the Command
  private Vector2 m_prevCenterPositionForLeft;
  private Vector2 m_prevCenterPositionForRight;
  private Vector2 m_prevLeftPosition;
  private Vector2 m_prevRightPosition;
  private double travelDirection;
  private boolean m_mirrored;
  private String name;

  public Path2D() {
    m_xyCurve = new Path2DCurve();
    m_easeCurve = new MotionCurve();
    travelDirection = 1.0;
  }

  public Path2D(String name) {
    this.name = name;
    m_xyCurve = new Path2DCurve();
    m_easeCurve = new MotionCurve();
    travelDirection = 1.0;
  }

  public void reset() {
    m_prevCenterPositionForLeft = null;
    m_prevCenterPositionForRight = null;
    m_prevLeftPosition = null;
    m_prevRightPosition = null;
  }

  public void addPointAndTangent(double x, double y, double xTangent, double yTangent) {
    m_xyCurve.addPointToEnd(x, y, xTangent, yTangent);
  }

  public boolean hasPoints()
  {
    return m_xyCurve.getHeadPoint()!=null;
  }

  public Path2DPoint addVector2(Vector2 point) {
    return addPoint(point.getX(), point.getY());
  }

  public Path2DPoint addVector2After(Vector2 point, Path2DPoint after)
  {
    return m_xyCurve.addPointAfter(point, after);
  }

  public Path2DPoint addPoint(double x, double y) {
    return m_xyCurve.addPointToEnd(x, y);
  }

  public void addPointAngleAndMagnitude(double x, double y, double angle, double magnitude) {
    m_xyCurve.addPointAngleAndMagnitudeToEnd(x, y, angle, magnitude);
  }

  public void removePoint( Path2DPoint path2DPoint ) {
    m_xyCurve.removePoint( path2DPoint );
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
    if (m_xyCurve.getHeadPoint()!=null)
      return getPositionAtEase(m_easeCurve.getValue(time));
    else
      return getPositionAtEase(time/5.0);  // take 5 seconds to finish path (linear motion)
  }

  public Vector2 getTangent(double time) {
    return getTangentAtEase(m_easeCurve.getValue(time));
  }

  public Vector2 getPositionAtEase(double ease) {
    double totalDistance = m_xyCurve.getLength();
    return m_xyCurve.getPositionAtDistance(ease * totalDistance);
  }

  public Vector2 getTangentAtEase(double ease) {
    double totalDistance = m_xyCurve.getLength();
    return m_xyCurve.getTangentAtDistance(ease * totalDistance);
  }

  public Vector2 getSidePosition(double time, double xOffset) {  // offset can be positive or negative (half the width of the robot)
    Vector2 centerPosition = getPosition(time);  // this could compute the position for a specific offset vector on the robot
    Vector2 tangent = getTangent(time);
    tangent = Vector2.Companion.normalize(tangent);
    tangent = Vector2.Companion.perpendicular(tangent);
    tangent = Vector2.Companion.multiply(tangent, xOffset);
    Vector2 sidePosition = Vector2.Companion.add(centerPosition, tangent);
    return sidePosition;
  }

  public Vector2 getLeftPosition(double time) {
    return getSidePosition(time, -m_robotWidth / 2.0);
  }

  public Vector2 getRightPosition(double time) {
    return getSidePosition(time, m_robotWidth / 2.0);
  }

  private double privateGetLeftPositionDelta(double time) {
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

    if (Vector2.Companion.dot(deltaCenter, deltaLeft) > 0) {
      return Vector2.Companion.length(deltaLeft);
    } else {
      return -Vector2.Companion.length(deltaLeft);
    }
  }

  private double privateGetRightPositionDelta(double time) {
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

    if (Vector2.Companion.dot(deltaCenter, deltaRight) > 0) {
      return Vector2.Companion.length(deltaRight);
    } else {
      return -Vector2.Companion.length(deltaRight);
    }
  }

  public double getLeftPositionDelta(double time) {
    if (travelDirection>0)
      return privateGetLeftPositionDelta(time);
    else
      return -privateGetRightPositionDelta(time);
  }

  public double getRightPositionDelta(double time) {
    if (travelDirection>0)
      return privateGetRightPositionDelta(time);
    else
      return -privateGetLeftPositionDelta(time);
  }

  public double getRobotWidth() {
    return m_robotWidth;
  }

  public void setRobotWidth(double robotWidth) {
    m_robotWidth = robotWidth;
  }

  public double getPathLength() {
    return m_xyCurve.getLength();
  }

  public double getDuration() {
    return m_easeCurve.getLength();
  }

  public MotionCurve getEaseCurve() {
    return m_easeCurve;
  }

  public double getTravelDirection() {
    return travelDirection;
  }

  public void setTravelDirection(double travelDirection) {
    this.travelDirection = travelDirection;
  }

  public boolean isMirrored() {
    return m_mirrored;
  }

  public void setMirrored(boolean mirrored) {
    this.m_mirrored = mirrored;
  }

  public void writeToNetworkTable() {
    NetworkTable table = NetworkTable.getTable("PathVisualizer");
    table.putString( name, toString() );
  }

  public void readFromNetworkTable() {
    NetworkTable table = NetworkTable.getTable("PathVisualizer");
    String pathString = table.getString( name, "" );
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

  public String toJSonString() {
      Collection<Double> doublesList = new ArrayList<>();
        doublesList.add(m_robotWidth);
        doublesList.add(travelDirection);
      Collection<Path2DCurve> path2DCurveCollection = new ArrayList<>();
        path2DCurveCollection.add(m_xyCurve);
      Collection<MotionCurve> motionCurveCollection = new ArrayList<>();
        motionCurveCollection.add(m_easeCurve);

      List<Collection> list = new ArrayList<>();
        list.add(doublesList);
        list.add(path2DCurveCollection);
        list.add(motionCurveCollection);

    Gson gson = new Gson();
    Type type = new TypeToken<List<Toolkit.Task>>() {}.getType();
    String json = gson.toJson(list, type);
    System.out.println(json);
  return json; }
}

