package org.team2471.frc.lib.motion_profiling;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import edu.wpi.first.wpilibj.geometry.Pose2d;
import edu.wpi.first.wpilibj.geometry.Rotation2d;
import edu.wpi.first.wpilibj.geometry.Translation2d;
import edu.wpi.first.wpilibj.trajectory.Trajectory;
import edu.wpi.first.wpilibj.trajectory.TrajectoryConfig;
import edu.wpi.first.wpilibj.trajectory.TrajectoryGenerator;
import edu.wpi.first.wpilibj.trajectory.TrajectoryUtil;
import edu.wpi.first.wpilibj.spline.*;
import edu.wpi.first.wpilibj.util.Units;
import org.team2471.frc.lib.math.Vector2;
import java.util.ArrayList;

public class Path2D {

    public String name;

    private final Path2DCurve m_xyCurve;    // positive y is forward in robot space, and positive x is to the robot's right
    private final MotionCurve m_easeCurve;  // the ease curve is the percentage along the path the robot as a function of time
    private final MotionCurve m_headingCurve; // the angle from the path which the robot is headed

    public enum RobotDirection {
        FORWARD, BACKWARD
    }

    public enum CurveType {
        EASE, HEADING, BOTH
    }

    private double speed = 1.0;
    private RobotDirection robotDirection = RobotDirection.FORWARD;
    public CurveType curveType = CurveType.EASE;
//    private double trackWidth = 25.0 / 12.0;
//    private double scrubFactor = 1.12;
    private boolean m_mirrored = false;

    private transient Autonomous autonomous;

    public Path2D() {
        m_xyCurve = new Path2DCurve();
        m_easeCurve = new MotionCurve();
        m_headingCurve = new MotionCurve();
    }

    public Path2D(String name) {
        this.name = name;
        m_xyCurve = new Path2DCurve();
        m_easeCurve = new MotionCurve();
        m_headingCurve = new MotionCurve();

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

    public void removeEasePoint(MotionKey motionKey) {
        m_easeCurve.removeKey(motionKey);
    }

    public void removeHeadingPoint(MotionKey motionKey) {
        if (m_headingCurve.getTailKey().getTime() == motionKey.getTime() && m_headingCurve.getHeadKey().getTime() == motionKey.getTime()) {
            System.out.println("cannot delete only heading entry");
        } else {
            m_headingCurve.removeKey(motionKey);
        }
    }

    public void scaleEasePoints(double newTime) {
        m_easeCurve.scaleLength(newTime);
        m_headingCurve.scaleLength(newTime);
    }
    public void addHeadingPoint(double time, double value) { m_headingCurve.storeValue(time, value); }

    public void removeAllEasePoints() {
        m_easeCurve.removeAllPoints();
    }

    public void addEasePointSlopeAndMagnitude(double time, double value, double slope, double magnitude) {
        m_easeCurve.storeValueSlopeAndMagnitude(time, value, slope, magnitude);
    }

    public Vector2 getPosition(double time) {
        if (m_easeCurve.getHeadKey() != null) {
            if (speed > 0) {
                //System.out.println("ease curve speed > 0. Hi." + getPositionAtEase(m_easeCurve.getValue(time * speed)).toString() + " --Hi");
                //System.out.println("time: " + time);
                //System.out.println("speed: " + speed);
                return getPositionAtEase(m_easeCurve.getValue(time * speed));
            } else {
                //System.out.println("ease curve speed <= 0. Hi." + getPositionAtEase(m_easeCurve.getValue(getDuration() - time * -speed)).toString() + " --Hi");
                return getPositionAtEase(m_easeCurve.getValue(getDuration() - time * -speed));
            }
        } else {
            if (speed > 0) {
                //System.out.println("ease curve no headkey > 0");
                return getPositionAtEase(time / 5.0 * speed);  // take 5 seconds to finish path (linear motion)
            } else {
                //System.out.println("ease curve no headkey < 0");
                return getPositionAtEase(getDuration() - time / 5.0 * -speed);
            }
        }
    }

    public Vector2 getTangent(double time) {
        double flipTangent = getRobotDirection() == RobotDirection.FORWARD ? 1.0 : -1.0;
        Vector2 rValue;

        if (m_easeCurve.getHeadKey() != null) {
            if (speed > 0)
                rValue = getTangentAtEase(m_easeCurve.getValue(time * speed));
            else
                rValue = getTangentAtEase(m_easeCurve.getValue(getDuration() - time * -speed));
        } else {
            if (speed > 0)
                rValue = getTangentAtEase(time / 5.0 * speed);  // take 5 seconds to finish path (linear motion)
            else
                rValue = getTangentAtEase(getDuration() - time / 5.0 * -speed);
        }

        rValue = rValue.times(flipTangent);
        return rValue;
    }

    public Vector2 getRobotDirection(double time) {
        return getTangent(time)
            .rotateDegrees(-m_headingCurve.getValue(time));
    }

    public Vector2 getPositionAtEase(double ease) {
        double totalDistance = m_xyCurve.getLength();
        Vector2 rValue = m_xyCurve.getPositionAtDistance(ease * totalDistance);
        //System.out.println("ease: " + ease + "Total Distance: " + totalDistance);
        if (isMirrored())
            rValue = rValue.mirrorXAxis();
        return rValue;
    }

    public Vector2 getTangentAtEase(double ease) {
        double totalDistance = m_xyCurve.getLength();
        Vector2 rValue = m_xyCurve.getTangentAtDistance(ease * totalDistance);
        if (isMirrored())
            rValue = rValue.mirrorXAxis();
        return rValue;
    }

    public Vector2 getSidePosition(double time, double xOffset) {  // offset can be positive or negative (half the width of the robot)
        Vector2 centerPosition = getPosition(time);
        Vector2 tangent = getTangent(time)
                .normalize()
                .perpendicular()
                .times(xOffset)
                .times(Math.copySign(1.0, speed));
        return centerPosition.plus(tangent);
    }

    public double getDuration() {
        return m_easeCurve.getLength();
    }

    public double getDurationWithSpeed() {
        return m_easeCurve.getLength() / Math.abs(speed);
    }

    public void setDuration(double seconds) {
        if (m_easeCurve.getTailKey() != null)
            m_easeCurve.getTailKey().setTime(seconds);
    }

    public MotionCurve getEaseCurve() {
        return m_easeCurve;
    }

    public MotionCurve getHeadingCurve() {
        return m_headingCurve;
    }

    public RobotDirection getRobotDirection() {
        return robotDirection;
    }

    public void setRobotDirection(RobotDirection robotDirection) {
        this.robotDirection = robotDirection;
    }

    public CurveType getCurveType() { return curveType; }

    public void setCurveType(CurveType curveType) { this.curveType = curveType; }

    public boolean isMirrored() {
        return m_mirrored || (autonomous != null && autonomous.isMirrored());  // the path is mirrored if the path is marked mirrored or the autonomous is marked mirrored
    }

    public void setMirrored(boolean mirrored) {
        m_mirrored = mirrored;
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
        m_headingCurve.fixUpTailAndPrevPointers();
    }

    public double getLength() {
        return m_xyCurve.getLength();
    }

    public double getAccelerationAtEase(double ease) {
        double deltaEase = 1.0 / 100.0;
        Vector2 tangent1 = getTangentAtEase(ease);
        Vector2 tangent2 = getTangentAtEase(ease + deltaEase);
        Vector2 delta = tangent2.minus(tangent1);
        return delta.getLength() * Path2DPoint.STEPS / deltaEase / getDuration() / getDuration();  // this is how much it would curve over the entire path length
    }

    public Vector2 getVelocityAtEase(double ease) {
        Vector2 velocity = getTangentAtEase(ease);
        velocity = velocity.times(Path2DPoint.STEPS);
        return velocity;
    }

    public Vector2 getVelocityAtTime(double time) {
        Vector2 tangent = getTangent(time);
        tangent.normalize();
        return tangent.times( m_easeCurve.getDerivative(time) * m_xyCurve.getLength());
    }

//    public double getCurvatureAtEase(double ease) {
//        double radius = 0.0;
//        Vector2 velocity = getVelocityAtEase(ease);
//        return velocity.dot(velocity) / radius;
//    }

    public double getAbsoluteHeadingDegreesAt(double time) {
//        Vector2 tangent = getTangent(time);
//        double pathHeading = Math.toDegrees(Math.atan2(tangent.getX(), tangent.getY()));
//        return pathHeading + m_headingCurve.getValue(time);
        if (isMirrored())
            return -m_headingCurve.getValue(time);
        else
            return m_headingCurve.getValue(time);
    }
    private double[] getTrajectoryDerivative(Boolean isX, Path2DPoint currPoint){
        double val0 = isX ? currPoint.getPosition().getX() : currPoint.getPosition().getY();
        double val1 = isX ? currPoint.getPrevTangent().getX() : currPoint.getPrevTangent().getY();
        double val2 = isX ? currPoint.getNextTangent().getX() : currPoint.getNextTangent().getY();
        return new double[]{Units.feetToMeters(val0), Units.feetToMeters(val1),Units.feetToMeters(val2)};
    }
    public Trajectory generateTrajectoryBasic(TrajectoryConfig config) {
        var currPoint = m_xyCurve.getHeadPoint();
        var tailPoint = m_xyCurve.getTailPoint();
        var interiorWaypoints = new ArrayList<Pose2d>();
        // add first point
        interiorWaypoints.add(new Pose2d(Units.feetToMeters(currPoint.getPosition().getX()), Units.feetToMeters(currPoint.getPosition().getY()), Rotation2d.fromDegrees(currPoint.getPosition().getAngle())));
        while (currPoint != tailPoint) {
            // add all subsequent points until we reach the end
            currPoint = currPoint.getNextPoint();
            interiorWaypoints.add(new Pose2d(Units.feetToMeters(currPoint.getPosition().getX()), Units.feetToMeters(currPoint.getPosition().getY()), Rotation2d.fromDegrees(currPoint.getPosition().getAngle())));
        }
        return TrajectoryGenerator.generateTrajectory(interiorWaypoints, config);
    }
    public Trajectory generateTrajectoryAdvanced(TrajectoryConfig config) {
        var currPoint = m_xyCurve.getHeadPoint();
        var tailPoint = m_xyCurve.getTailPoint();
        // add first point
        var controlVectors = new TrajectoryGenerator.ControlVectorList();
        controlVectors.add(new Spline.ControlVector(getTrajectoryDerivative(true, currPoint), getTrajectoryDerivative(false, currPoint)));
        while (currPoint != tailPoint) {
            // add all subsequent points until we reach the end
            currPoint = currPoint.getNextPoint();
            controlVectors.add(new Spline.ControlVector(getTrajectoryDerivative(true, currPoint), getTrajectoryDerivative(false, currPoint)));
        }
        return TrajectoryGenerator.generateTrajectory(controlVectors, config);
    }
    public Trajectory generateTrajectory(Double maxVelocity, Double maxAcceleration) {
        TrajectoryConfig config = new TrajectoryConfig(maxVelocity, maxAcceleration);
        config.setReversed(m_mirrored);
        return generateTrajectoryAdvanced(config);
    }
}
