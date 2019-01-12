package org.team2471.frc.lib.motion_profiling;

import org.team2471.frc.lib.vector.Vector2;

import static org.team2471.frc.lib.motion_profiling.Path2DPoint.SlopeMethod.SLOPE_MANUAL;
import static org.team2471.frc.lib.motion_profiling.Path2DPoint.SlopeMethod.SLOPE_SMOOTH;

public class Path2DPoint implements Point2DInterface {
    public static transient final int STEPS = 600;
    private Vector2 m_position;
    private Vector2 m_prevAngleAndMagnitude;
    private Vector2 m_nextAngleAndMagnitude;
    private Vector2 m_prevTangent;
    private Vector2 m_nextTangent;
    private SlopeMethod m_prevSlopeMethod;
    private SlopeMethod m_nextSlopeMethod;
    private Path2DPoint m_nextPoint;

    private transient boolean m_bTangentsDirty;
    private transient boolean m_bCoefficientsDirty;
    private transient CubicCoefficients1D m_xCoeff;
    private transient CubicCoefficients1D m_yCoeff;
    private transient double m_segmentLength;
    private transient double partialLength, prevPartialLength;
    private transient Path2DCurve m_path2DCurve;
    private transient Path2DPoint m_prevPoint;

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

            if (getNextPoint() == null && getPrevPoint().getPrevPoint() != null) {
                getPrevPoint().getPrevPoint().setCoefficientsDirty(true);  // coefficients two back need recalculated
            }
        }

        if (getNextPoint() != null) {
            getNextPoint().setTangentsDirty(true);
            getNextPoint().setCoefficientsDirty(true);
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
        onPositionChanged();
    }

    public Vector2 getPrevAngleAndMagnitude() {
        return m_prevAngleAndMagnitude;
    }

    public void setPrevAngleAndMagnitude(Vector2 prevAngleAndMagnitude) {
        m_prevAngleAndMagnitude = new Vector2(prevAngleAndMagnitude.getX(), prevAngleAndMagnitude.getY());
        m_prevSlopeMethod = SLOPE_SMOOTH;
        onPositionChanged();
    }

    public Vector2 getNextAngleAndMagnitude() {
        return m_nextAngleAndMagnitude;
    }

    public void setNextAngleAndMagnitude(Vector2 nextAngleAndMagnitude) {  // this one takes the angle in world space - stored as an offset
        m_nextAngleAndMagnitude = new Vector2(nextAngleAndMagnitude.getX(), nextAngleAndMagnitude.getY());
        m_prevSlopeMethod = SLOPE_SMOOTH;
        onPositionChanged();
    }

    public Vector2 getPrevTangent() {
        if (areTangentsDirty())
            calculateTangents();

        return m_prevTangent;
    }

    public void setPrevTangent(Vector2 prevTangent) {
        if (getPrevPoint()!=null) {
            if (m_nextSlopeMethod == SLOPE_SMOOTH) {
                m_prevAngleAndMagnitude = new Vector2(0, 1);
                calculateTangents();  // determine the default tangents
                double defaultAngle = Math.toDegrees(Math.atan2(m_prevTangent.getY(), m_prevTangent.getX()));
                double goalAngle = Math.toDegrees(Math.atan2(prevTangent.getY(), prevTangent.getX()));
                double angle = goalAngle - defaultAngle;
                double magnitude = Vector2.Companion.length(prevTangent) / Vector2.Companion.length(m_prevTangent);
                m_prevAngleAndMagnitude = new Vector2(angle, magnitude);
                m_nextAngleAndMagnitude = new Vector2(angle, getNextMagnitude());
                m_nextSlopeMethod = SLOPE_SMOOTH;
                m_prevSlopeMethod = SLOPE_SMOOTH;
            } else if (m_nextSlopeMethod == SLOPE_MANUAL) {
                m_prevTangent = prevTangent;
            }
        } else {
            m_nextTangent = new Vector2(prevTangent.getX(), prevTangent.getY());
            m_nextSlopeMethod = SLOPE_MANUAL;
            m_prevTangent = new Vector2(prevTangent.getX(), prevTangent.getY());
            m_prevSlopeMethod = SLOPE_MANUAL;
        }

        onPositionChanged();
    }

    public Vector2 getNextTangent() {
        if (areTangentsDirty())
            calculateTangents();

        return m_nextTangent;
    }

    public void setNextTangent(Vector2 nextTangent) {
        if (m_nextPoint != null) {
            if (m_nextSlopeMethod == SLOPE_SMOOTH) {
                m_nextAngleAndMagnitude = new Vector2(0, 1);
                calculateTangents();  // determine the default tangents
                double defaultAngle = Math.toDegrees(Math.atan2(m_nextTangent.getY(), m_nextTangent.getX()));
                double goalAngle = Math.toDegrees(Math.atan2(nextTangent.getY(), nextTangent.getX()));
                double angle = goalAngle - defaultAngle;
                double magnitude = Vector2.Companion.length(nextTangent) / Vector2.Companion.length(m_nextTangent);
                m_nextAngleAndMagnitude = new Vector2(angle, magnitude);
                m_prevAngleAndMagnitude = new Vector2(angle, getPrevMagnitude());
                m_nextSlopeMethod = SLOPE_SMOOTH;
                m_prevSlopeMethod = SLOPE_SMOOTH;
            } else if (m_nextSlopeMethod == SLOPE_MANUAL) {
                m_nextTangent = nextTangent;
            }
        }
        else {
            m_nextTangent = new Vector2(nextTangent.getX(), nextTangent.getY());
            m_nextSlopeMethod = SLOPE_MANUAL;
            m_prevTangent = new Vector2(nextTangent.getX(), nextTangent.getY());
            m_prevSlopeMethod = SLOPE_MANUAL;
        }
        onPositionChanged();
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

    public void setNextPoint(Point2DInterface nextPoint) {
        setNextPoint( (Path2DPoint)nextPoint );
    }

    public void setPrevPoint(Point2DInterface prevPoint) {
        setPrevPoint( (Path2DPoint)prevPoint );
    }

    public SlopeMethod getPrevSlopeMethod() {
        return m_prevSlopeMethod;
    }

    public void setPrevSlopeMethod(SlopeMethod slopeMethod) {
        m_prevSlopeMethod = slopeMethod;
        m_bTangentsDirty = true;
    }

    public SlopeMethod getNextSlopeMethod() {
        return m_nextSlopeMethod;
    }

    public void setNextSlopeMethod(SlopeMethod slopeMethod) {
        m_prevSlopeMethod = slopeMethod;
        m_bTangentsDirty = true;
    }

    public double getPrevAngle() {
        return m_prevAngleAndMagnitude.getX();
    }

    public double getNextAngle() {
        return m_nextAngleAndMagnitude.getX();
    }

    public double getPrevMagnitude() {
        return m_prevAngleAndMagnitude.getY();
    }

    public double getNextMagnitude() {
        return m_nextAngleAndMagnitude.getY();
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

        final double defaultSplineDivisor = 2.0;

        switch (getPrevSlopeMethod()) {
            case SLOPE_LINEAR:
                if (m_prevPoint != null)
                    m_prevTangent = Vector2.Companion.subtract(getPosition(), m_prevPoint.getPosition());
                break;
            case SLOPE_SMOOTH:
                bCalcSmoothPrev = true;
                break;
            case SLOPE_MANUAL:
                // todo: should actually compute the angle and magnitude from the vector here
                break;
        }

        switch (getNextSlopeMethod()) {
            case SLOPE_LINEAR:
                if (m_nextPoint != null)
                    m_nextTangent = Vector2.Companion.subtract(m_nextPoint.getPosition(), getPosition());
                break;
            case SLOPE_SMOOTH:
                bCalcSmoothNext = true;
                break;
            case SLOPE_MANUAL:
                // todo: should actually compute the angle and magnitude from the vector here
                break;
        }

        if (bCalcSmoothPrev || bCalcSmoothNext) {
            if (m_prevPoint != null && m_nextPoint != null) {
                Vector2 delta = Vector2.Companion.subtract(m_nextPoint.getPosition(), m_prevPoint.getPosition());
                //double weight = Math.abs(delta.x);  // Bug for paths:  just works for 2d channels I think
                double weight = Vector2.Companion.length(delta);
                if (weight == 0) // if points are on top of one another (no tangents)
                {
                    if (bCalcSmoothPrev)
                        m_prevTangent.set(0, 0);
                    if (bCalcSmoothNext)
                        m_nextTangent.set(0, 0);
                } else {
                    delta = Vector2.Companion.divide(delta, weight);

                    if (bCalcSmoothPrev) {
                        double prevLength = Vector2.Companion.length(Vector2.Companion.subtract(getPosition(), m_prevPoint.getPosition())) / defaultSplineDivisor;
                        m_prevTangent = Vector2.Companion.multiply(delta, prevLength);
                    }
                    if (bCalcSmoothNext) {
                        double nextLength = Vector2.Companion.length(Vector2.Companion.subtract(m_nextPoint.getPosition(), getPosition())) / defaultSplineDivisor;
                        m_nextTangent = Vector2.Companion.multiply(delta, nextLength);
                    }
                }
            } else {
                if (m_nextPoint != null) {
                    if (bCalcSmoothPrev) {
                        m_prevTangent = Vector2.Companion.subtract(m_nextPoint.getPosition(), getPosition());
                        m_prevTangent = Vector2.Companion.multiply(m_prevTangent, 1.0 / defaultSplineDivisor);
                    }
                    if (bCalcSmoothNext) {
                        m_nextTangent = Vector2.Companion.subtract(m_nextPoint.getPosition(), getPosition());
                        m_nextTangent = Vector2.Companion.multiply(m_nextTangent, 1.0 / defaultSplineDivisor);
                    }
                }

                if (m_prevPoint != null) {
                    if (bCalcSmoothPrev) {
                        m_prevTangent = Vector2.Companion.subtract(getPosition(), m_prevPoint.getPosition());
                        m_prevTangent = Vector2.Companion.multiply(m_prevTangent, 1.0 / defaultSplineDivisor);
                    }
                    if (bCalcSmoothNext) {
                        m_nextTangent = Vector2.Companion.subtract(getPosition(), m_prevPoint.getPosition());
                        m_nextTangent = Vector2.Companion.multiply(m_nextTangent, 1.0 / defaultSplineDivisor);
                    }
                }
            }
            m_prevTangent = Vector2.Companion.multiply(m_prevTangent, getPrevMagnitude());
            m_nextTangent = Vector2.Companion.multiply(m_nextTangent, getNextMagnitude());

            m_prevTangent.rotateRadians(Math.toRadians(getPrevAngle()));
            m_nextTangent.rotateRadians(Math.toRadians(getNextAngle()));
        }
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
        if (areTangentsDirty())
            calculateTangents();
        if (getNextPoint() != null && getNextPoint().areTangentsDirty())
            getNextPoint().calculateTangents();

        setCoefficientsDirty(false);

        double pointax = getPosition().getX();
        double pointbx = m_nextPoint.getPosition().getX();
        double pointcx = getNextTangent().getX();
        double pointdx = m_nextPoint.getPrevTangent().getX();
        m_xCoeff = new CubicCoefficients1D(pointax, pointbx, pointcx, pointdx);

        double pointay = getPosition().getY();
        double pointby = m_nextPoint.getPosition().getY();
        double pointcy = getNextTangent().getY();
        double pointdy = m_nextPoint.getPrevTangent().getY();
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
            m_segmentLength += Vector2.Companion.length(Vector2.Companion.subtract(pos, prevPos));
            prevPos.set(pos.getX(), pos.getY());
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
            partialLength += Vector2.Companion.length(Vector2.Companion.subtract(pos, prevPos));
        }

        double intoSegment = (distance - prevPartialLength) / (partialLength - prevPartialLength);  // linearly interpolate t based on distance of the surrounding steps

        return Vector2.Companion.add(Vector2.Companion.multiply(prevPos, 1.0f - intoSegment), Vector2.Companion.multiply(pos, intoSegment));
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
            partialLength += Vector2.Companion.length(Vector2.Companion.subtract(pos, prevPos));
        }

        return Vector2.Companion.subtract(pos, prevPos);
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

    public enum SlopeMethod {
        SLOPE_SMOOTH, SLOPE_MANUAL, SLOPE_LINEAR
    }
}
