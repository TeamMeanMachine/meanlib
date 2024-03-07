package org.team2471.frc.lib.motion_profiling;

import com.google.gson.Gson;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.trajectory.Trajectory;
import edu.wpi.first.math.trajectory.TrajectoryConfig;
import edu.wpi.first.math.trajectory.TrajectoryGenerator;
import edu.wpi.first.math.spline.*;
import edu.wpi.first.math.util.Units;
import org.team2471.frc.lib.math.Vector2;

import java.util.ArrayList;

import static java.lang.Math.IEEEremainder;

public class Path2D {

    public String name;

    private final Path2DCurve m_xyCurve;    // positive y is forward in robot space, and positive x is to the robot's right
    private final MotionCurve m_easeCurve;  // the ease curve is the percentage along the path the robot as a function of time
    private final MotionCurve m_headingCurve; // the angle from the path which the robot is headed

    public Path2DCurve get_xyCurve() {
        return m_xyCurve;
    }

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
    private boolean m_reflected = false;

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
        Path2D path = new Gson().fromJson(jsonString, Path2D.class);

        MotionCurve.hydrateCurve(path.getHeadingCurve());
        MotionCurve.hydrateCurve(path.getEaseCurve());

        return path;
    }

    public void addPointAndTangent(double x, double y, double xTangent, double yTangent) {
        get_xyCurve().addPointToEnd(x, y, xTangent, yTangent);
    }

    public boolean hasPoints() {
        return get_xyCurve().getHeadPoint() != null;
    }

    public Path2DPoint addVector2(Vector2 point) {
        return addPoint(point.getX(), point.getY());
    }

    public Path2DPoint addVector2After(Vector2 point, Path2DPoint after) {
        return get_xyCurve().addPointAfter(point, after);
    }

    public Path2DPoint addPoint(double x, double y) {
        return get_xyCurve().addPointToEnd(x, y);
    }

    public void addPointAngleAndMagnitude(double x, double y, double angle, double magnitude) {
        get_xyCurve().addPointAngleAndMagnitudeToEnd(x, y, angle, magnitude);
    }

    public void removePoint(Path2DPoint path2DPoint) {
        get_xyCurve().removePoint(path2DPoint);
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

    private double javaWrap(double angle) { return IEEEremainder(angle, 360.0); }

    private double javaUnwrap(double angle, double nearByAngle) {
        return nearByAngle + javaWrap(angle - nearByAngle);
    }
    public void addHeadingPoint(double time, double value, boolean unwrap) {
        if (unwrap) {
            double preValue = m_headingCurve.getValue(time);
            value = javaUnwrap(value, preValue);
        }
        m_headingCurve.storeValue(time, value);
    }

    public void addHeadingPoint(double time, double value) {

        addHeadingPoint(time, value, true);
    }

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
        double totalDistance = get_xyCurve().getLength();
        Vector2 rValue = get_xyCurve().getPositionAtDistance(ease * totalDistance);
        //System.out.println("ease: " + ease + "Total Distance: " + totalDistance);
        if (isMirrored())
            rValue = rValue.mirrorXAxis();
        if (isReflected())
            rValue = rValue.reflectAcrossField(26.135);
        return rValue;
    }

    public Vector2 getTangentAtEase(double ease) {
        double totalDistance = get_xyCurve().getLength();
        Vector2 rValue = get_xyCurve().getTangentAtDistance(ease * totalDistance);
        if (isMirrored())
            rValue = rValue.mirrorXAxis();
        if (isReflected()) {
            rValue = rValue.reflectAcrossField(26.135); //new Vector2(-rValue.component1(), rValue.component2());
        }
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

    public boolean isReflected() {
        return m_reflected || (autonomous != null && autonomous.isReflected());
    }

    public void setReflected(boolean reflected) {
        m_reflected = reflected;
    }

    public String toString() {
        StringBuilder rValue = new StringBuilder();
        for (Path2DPoint point = get_xyCurve().getHeadPoint(); point != null; point = point.getNextPoint()) {
            rValue.append(point);
        }
        return rValue.toString();
    }

    public Path2DCurve getXYCurve() {
        return get_xyCurve();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String toJsonString() {
        try {
            Moshi moshi = new Moshi.Builder().build();
            JsonAdapter<Path2D> jsonAdapter = moshi.adapter(Path2D.class);

            String json = jsonAdapter.toJson(this);
            System.out.println(json);
            return json;
        } catch (Exception ex) {
            System.out.println("could not convert Path2D to json string");
            System.out.println(ex.getMessage());
            return "";
        }
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
        get_xyCurve().fixUpTailAndPrevPointers();
        m_easeCurve.fixUpTailAndPrevPointers();
        m_headingCurve.fixUpTailAndPrevPointers();
    }

    public double getLength() {
        return get_xyCurve().getLength();
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
        return tangent.times( m_easeCurve.getDerivative(time) * get_xyCurve().getLength());
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
            if (isReflected()) {
                return 180 - m_headingCurve.getValue(time);
            }
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
        var currPoint = get_xyCurve().getHeadPoint();
        var tailPoint = get_xyCurve().getTailPoint();
        var interiorWaypoints = new ArrayList<Pose2d>();
        // add first point
        var rot = Rotation2d.fromDegrees(currPoint.getPosition().getAngleAsDegrees());
        var thisPose = new Pose2d(new Translation2d(0.1, 0.2), rot);

        interiorWaypoints.add(thisPose);
        while (currPoint != tailPoint) {
            // add all subsequent points until we reach the end
            currPoint = currPoint.getNextPoint();
            interiorWaypoints.add(new Pose2d(Units.feetToMeters(currPoint.getPosition().getX()), Units.feetToMeters(currPoint.getPosition().getY()), Rotation2d.fromDegrees(currPoint.getPosition().getAngleAsDegrees())));
        }
        return TrajectoryGenerator.generateTrajectory(interiorWaypoints, config);
    }
    public Trajectory generateTrajectoryAdvanced(TrajectoryConfig config) {
        var currPoint = get_xyCurve().getHeadPoint();
        var tailPoint = get_xyCurve().getTailPoint();
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
