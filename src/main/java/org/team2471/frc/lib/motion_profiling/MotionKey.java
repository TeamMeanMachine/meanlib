package org.team2471.frc.lib.motion_profiling;

import org.team2471.frc.lib.vector.Vector2;

import static org.team2471.frc.lib.motion_profiling.MotionKey.SlopeMethod.SLOPE_MANUAL;
import static org.team2471.frc.lib.motion_profiling.MotionKey.SlopeMethod.SLOPE_PLATEAU;

public class MotionKey {
    private transient final double CLAMPTOLERANCE = 0.005;
    private Vector2 m_timeAndValue;
    private Vector2 m_prevAngleAndMagnitude;
    private Vector2 m_nextAngleAndMagnitude;
    private Vector2 m_prevTangent;
    private Vector2 m_nextTangent;
    private SlopeMethod m_prevSlopeMethod;
    private SlopeMethod m_nextSlopeMethod;
    private boolean m_markBeginOrEndKeysToZeroSlope;
    private MotionKey m_nextKey;

    private transient MotionCurve m_motionCurve;
    private transient MotionKey m_prevKey;
    private transient boolean m_bTangentsDirty;
    private transient boolean m_bCoefficientsDirty;
    private transient CubicCoefficients1D m_xCoeff;
    private transient CubicCoefficients1D m_yCoeff;

    public MotionKey() {
        m_timeAndValue = new Vector2(0, 0);
        m_prevAngleAndMagnitude = new Vector2(0, 1);
        m_nextAngleAndMagnitude = new Vector2(0, 1);
        m_prevTangent = new Vector2(0, 0);
        m_nextTangent = new Vector2(0, 0);

        m_timeAndValue.set(0, 0);
        m_prevAngleAndMagnitude.set(0, 1);
        m_nextAngleAndMagnitude.set(0, 1);
        m_bTangentsDirty = true;
        m_bCoefficientsDirty = true;
        m_prevSlopeMethod = SlopeMethod.SLOPE_SMOOTH;
        m_nextSlopeMethod = SlopeMethod.SLOPE_SMOOTH;
        m_markBeginOrEndKeysToZeroSlope = true;
        m_motionCurve = null;
        m_nextKey = null;
        m_prevKey = null;
    }

    public void onPositionChanged() {
        getMotionCurve().onKeyPositionChanged(this);  // tell the curve too

        setTangentsDirty(true);
        setCoefficientsDirty(true);

        if (getPrevKey() != null) {
            getPrevKey().setTangentsDirty(true);
            getPrevKey().setCoefficientsDirty(true);
            if (getPrevKey().getPrevKey() != null && getPrevKey().getPrevKey().getNextSlopeMethod() == SLOPE_PLATEAU) {  // Need to go two away if it is Plateau because they use Prev and Next Tangents
                getPrevKey().getPrevKey().setTangentsDirty(true);
                getPrevKey().getPrevKey().setCoefficientsDirty(true);
            }
        }

        if (getNextKey() != null) {
            getNextKey().setTangentsDirty(true);
            if (getNextKey().getNextKey() != null && getNextKey().getNextKey().getPrevSlopeMethod() == SLOPE_PLATEAU) {  // Need to go two away if it is Plateau because they use Prev and Next Tangents
                getNextKey().getNextKey().setTangentsDirty(true);
                getNextKey().setCoefficientsDirty(true);
            }
        }
    }

    public double getTime() {
        return m_timeAndValue.getX();
    }

    public void setTime(double time) {
        m_timeAndValue.setX(time);
        onPositionChanged();
    }

    public double getValue() {
        return m_timeAndValue.getY();
    }

    public void setValue(double value) {
        m_timeAndValue.setY(value);
        onPositionChanged();
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

    public Vector2 getTimeAndValue() {
        return m_timeAndValue;
    }

    public void setTimeAndValue(Vector2 m_timeAndValue) {
        this.m_timeAndValue = m_timeAndValue;
        onPositionChanged();
    }

    public Vector2 getPrevAngleAndMagnitude() {
        return m_prevAngleAndMagnitude;
    }

    public void setPrevAngleAndMagnitude(Vector2 m_prevAngleAndMagnitude) {
        m_markBeginOrEndKeysToZeroSlope = false;
        this.m_prevAngleAndMagnitude = m_prevAngleAndMagnitude;
        m_prevSlopeMethod = SlopeMethod.SLOPE_MANUAL;
        setTangentsDirty(true);
        onPositionChanged();
    }

    public Vector2 getNextAngleAndMagnitude() {
        return m_nextAngleAndMagnitude;
    }

    public void setNextAngleAndMagnitude(Vector2 m_nextAngleAndMagnitude) {
        m_markBeginOrEndKeysToZeroSlope = false;
        this.m_nextAngleAndMagnitude = m_nextAngleAndMagnitude;
        m_nextSlopeMethod = SlopeMethod.SLOPE_MANUAL;
        setTangentsDirty(true);
        onPositionChanged();
    }

    public Vector2 getPrevTangent() {
        if (areTangentsDirty())
            calculateTangents();

        return m_prevTangent;
    }

    public void setPrevTangent(Vector2 m_PrevTangent) {
        this.m_prevTangent = m_PrevTangent;
    }

    public Vector2 getNextTangent() {
        if (areTangentsDirty())
            calculateTangents();

        return m_nextTangent;
    }

    public void setNextTangent(Vector2 m_NextTangent) {
        this.m_nextTangent = m_NextTangent;
    }

    public MotionCurve getMotionCurve() {
        return m_motionCurve;
    }

    public void setMotionCurve(MotionCurve m_motionCurve) {
        this.m_motionCurve = m_motionCurve;
    }

    public MotionKey getNextKey() {
        return m_nextKey;
    }

    public void setNextKey(MotionKey m_nextKey) {
        this.m_nextKey = m_nextKey;
    }

    public MotionKey getPrevKey() {
        return m_prevKey;
    }

    public void setPrevKey(MotionKey m_prevKey) {
        this.m_prevKey = m_prevKey;
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
        m_nextSlopeMethod = slopeMethod;
    }

    public double getPrevMagnitude() {
        return m_prevAngleAndMagnitude.getY();
    }

    public double getNextMagnitude() {
        return m_nextAngleAndMagnitude.getY();
    }

    void insertBefore(MotionKey newKey) {
        m_prevKey = newKey.m_prevKey;
        if (newKey.m_prevKey != null)
            newKey.m_prevKey.m_nextKey = this;
        newKey.m_prevKey = this;
        m_nextKey = newKey;
    }

    void insertAfter(MotionKey newKey) {
        m_nextKey = newKey.m_nextKey;
        if (newKey.m_nextKey != null)
            newKey.m_nextKey.m_prevKey = this;
        newKey.m_nextKey = this;
        m_prevKey = newKey;
    }

    private void calculateTangents() {
        setTangentsDirty(false);

        boolean bCalcSmoothPrev = false;
        boolean bCalcSmoothNext = false;

        switch (getPrevSlopeMethod()) {
            case SLOPE_MANUAL:
                m_prevTangent.set(Math.cos(getPrevAngleAndMagnitude().getX()), Math.sin(getPrevAngleAndMagnitude().getX()));
                if (m_prevKey != null)
                    Vector2.Companion.multiply(m_prevTangent, getTimeAndValue().getX() - m_prevKey.getTimeAndValue().getX());
                break;
            case SLOPE_LINEAR:
                if (m_prevKey != null)
                    m_prevTangent = Vector2.Companion.subtract(getTimeAndValue(), m_prevKey.getTimeAndValue());
                break;
            case SLOPE_FLAT:
                if (m_prevKey != null)
                    m_prevTangent.set((getTimeAndValue().getX() - m_prevKey.getTimeAndValue().getX()) * 0.5, 0.0);
                break;
            case SLOPE_SMOOTH:
                bCalcSmoothPrev = true;
                break;
            case SLOPE_CLAMPED: {
                double fClampTolerence = (getMotionCurve().getMaxValue() - getMotionCurve().getMinValue()) * CLAMPTOLERANCE;
                if (m_prevKey != null && Math.abs(m_prevKey.getValue() - getValue()) <= fClampTolerence) // make Flat
                    m_prevTangent.set(getTime() - m_prevKey.getTime(), 0.0f);
                else if (m_nextKey != null && Math.abs(m_nextKey.getValue() - getValue()) <= fClampTolerence) // Make Flat
                {
                    if (m_prevKey != null)
                        m_prevTangent.set(getTime() - m_prevKey.getTime(), 0.0f);
                    else
                        m_prevTangent.set(0.0f, 0.0f);
                } else
                    bCalcSmoothPrev = true;
                break;
            }
            case SLOPE_PLATEAU:
                if (m_prevKey == null || m_nextKey == null) {
                    if (m_prevKey != null)
                        m_prevTangent.set(getTime() - m_prevKey.getTime(), 0.0f); // Make Flat
                    else
                        m_prevTangent.set(0.0f, 0.0f);
                } else // we have a prev and a next, lets see if both the prev's out tangent and the next's in tangent are both either greater or less than our value, if so lets make out tangent flat
                {
                    double fPrevTangentValue;
                    if (m_prevKey.getNextSlopeMethod() == SLOPE_PLATEAU)
                        fPrevTangentValue = m_prevKey.getValue(); // This way we don't get an infinite recursion
                    else {
                        Vector2 vPrevPos = m_prevKey.getTimeAndValue();
                        Vector2 vPrevTangent = Vector2.Companion.add(Vector2.Companion.multiply(m_prevKey.getNextTangent(), 1.0 / 3.0), vPrevPos);
                        fPrevTangentValue = vPrevTangent.getY();
                    }

                    double fNextTangentValue;
                    if (m_nextKey.getPrevSlopeMethod() == SLOPE_PLATEAU)
                        fNextTangentValue = m_nextKey.getValue(); // This way we don't get an infinite recursion
                    else {
                        Vector2 vNextPos = m_nextKey.getTimeAndValue();
                        Vector2 vNextTangent = Vector2.Companion.subtract(vNextPos, Vector2.Companion.multiply(m_nextKey.getPrevTangent(), 1.0 / 3.0));
                        fNextTangentValue = vNextTangent.getY();
                    }

                    double fValue = getValue();
                    if (fPrevTangentValue > fValue && fNextTangentValue > fValue)
                        m_prevTangent.set(getTime() - m_prevKey.getTime(), 0.0f); // Make Flat
                    else if (fPrevTangentValue < fValue && fNextTangentValue < fValue)
                        m_prevTangent.set(getTime() - m_prevKey.getTime(), 0.0f); // Make Flat
                    else
                        bCalcSmoothPrev = true;
                }
                break;
            case SLOPE_STEPPED:
            case SLOPE_STEPPED_NEXT:
                assert (false); // Not a valid method for PREV Interp Method, it is only valid for NEXT key direction
                break;
        }

        switch (getNextSlopeMethod()) {
            case SLOPE_MANUAL:
                m_nextTangent.set(Math.cos(getNextAngleAndMagnitude().getX()), Math.sin(getNextAngleAndMagnitude().getX()));
                if (m_nextKey != null)
                    Vector2.Companion.multiply(m_nextTangent, m_nextKey.getTimeAndValue().getX() - getTimeAndValue().getX());
                break;
            case SLOPE_LINEAR:
                if (m_nextKey != null)
                    m_nextTangent = Vector2.Companion.subtract(m_nextKey.getTimeAndValue(), getTimeAndValue());
                break;
            case SLOPE_FLAT:
                if (m_nextKey != null)
                    m_nextTangent.set(m_nextKey.getTimeAndValue().getX() - getTimeAndValue().getX(), 0.0f);
                break;
            case SLOPE_SMOOTH:
                bCalcSmoothNext = true;
                break;
            case SLOPE_CLAMPED: {
                double fClampTolerence = (getMotionCurve().getMaxValue() - getMotionCurve().getMinValue()) * CLAMPTOLERANCE;
                if (m_prevKey != null && Math.abs(m_prevKey.getValue() - getValue()) <= fClampTolerence) // make Flat
                {
                    if (m_nextKey != null)
                        m_nextTangent.set(m_nextKey.getTime() - getTime(), 0.0f);
                    else
                        m_nextTangent.set(0.0f, 0.0f);
                } else if (m_nextKey != null && Math.abs(m_nextKey.getValue() - getValue()) <= fClampTolerence) // Make Flat
                    m_nextTangent.set(m_nextKey.getTime() - getTime(), 0.0f);
                else
                    bCalcSmoothNext = true;
                break;
            }
            case SLOPE_PLATEAU:
                if (m_prevKey == null || m_nextKey == null) {
                    if (m_nextKey != null)
                        m_nextTangent.set(m_nextKey.getTime() - getTime(), 0.0f); // Make it flat
                    else
                        m_nextTangent.set(0.0f, 0.0f);
                } else // we have a prev and a next, lets see if both the prev's out tangent and the next's in tangent are both either greater or less than our value, if so lets make out tangent flat
                {
                    double fPrevTangentValue;
                    if (m_prevKey.getNextSlopeMethod() == SLOPE_PLATEAU)
                        fPrevTangentValue = m_prevKey.getValue(); // This way we don't get an infinite recursion
                    else {
                        Vector2 vPrevPos = new Vector2(m_prevKey.getTime(), m_prevKey.getValue());
                        Vector2 vPrevTangent = Vector2.Companion.add(Vector2.Companion.multiply(m_prevKey.getNextTangent(), 1.0 / 3.0), vPrevPos);
                        fPrevTangentValue = vPrevTangent.getY();
                    }

                    double fNextTangentValue;
                    if (m_nextKey.getPrevSlopeMethod() == SLOPE_PLATEAU)
                        fNextTangentValue = m_nextKey.getValue(); // This way we don't get an infinite recursion
                    else {
                        Vector2 vNextPos = new Vector2(m_nextKey.getTime(), m_nextKey.getValue());
                        Vector2 vNextTangent = Vector2.Companion.subtract(vNextPos, Vector2.Companion.multiply(m_nextKey.getPrevTangent(), 1.0 / 3.0));
                        fNextTangentValue = vNextTangent.getY();
                    }

                    double fValue = getValue();
                    if (fPrevTangentValue > fValue && fNextTangentValue > fValue)
                        m_nextTangent.set(m_nextKey.getTime() - getTime(), 0.0f); // Make it flat
                    else if (fPrevTangentValue < fValue && fNextTangentValue < fValue)
                        m_nextTangent.set(m_nextKey.getTime() - getTime(), 0.0f); // Make it flat
                    else
                        bCalcSmoothNext = true;
                }
                break;
            case SLOPE_STEPPED:
            case SLOPE_STEPPED_NEXT:
                break; // nothing to do, no tangents
        }

        if (bCalcSmoothPrev || bCalcSmoothNext) {
            if (m_prevKey != null && m_nextKey != null) {
                Vector2 delta = Vector2.Companion.subtract(m_nextKey.getTimeAndValue(), m_prevKey.getTimeAndValue());
                double weight = Math.abs(delta.getX());
                if (weight == 0) // if keys are on top of one another (no tangents)
                {
                    if (bCalcSmoothPrev)
                        m_prevTangent.set(0, 0);
                    if (bCalcSmoothNext)
                        m_nextTangent.set(0, 0);
                } else {
                    delta = Vector2.Companion.divide(delta, weight);

                    if (bCalcSmoothPrev) {
                        double prevWeight = getTimeAndValue().getX() - m_prevKey.getTimeAndValue().getX();
                        m_prevTangent = Vector2.Companion.multiply(delta, prevWeight);
                    }
                    if (bCalcSmoothNext) {
                        double nextWeight = m_nextKey.getTimeAndValue().getX() - getTimeAndValue().getX();
                        m_nextTangent = Vector2.Companion.multiply(delta, nextWeight);
                    }
                }
            } else {
                if (m_nextKey != null) {
                    if (bCalcSmoothPrev)
                        m_prevTangent = Vector2.Companion.subtract(m_nextKey.getTimeAndValue(), getTimeAndValue());

                    if (bCalcSmoothNext)
                        m_nextTangent = Vector2.Companion.subtract(m_nextKey.getTimeAndValue(), getTimeAndValue());
                }

                if (m_prevKey != null) {
                    if (bCalcSmoothPrev)
                        m_prevTangent = Vector2.Companion.subtract(getTimeAndValue(), m_prevKey.getTimeAndValue());

                    if (bCalcSmoothNext)
                        m_nextTangent = Vector2.Companion.subtract(getTimeAndValue(), m_prevKey.getTimeAndValue());
                }
            }
        }

        m_prevTangent = Vector2.Companion.multiply(m_prevTangent, getPrevAngleAndMagnitude().getY()); // / 3.0 it seems like this is more of a UI only thing, and shouldn't really be done in this case.  But maybe I'm wrong.  Subtract the points, then take a third to get a good default tangent.  Does that still appear too long in the UI?  So we divide by 3 again.
        m_nextTangent = Vector2.Companion.multiply(m_nextTangent, getNextAngleAndMagnitude().getY()); // / 3.0
    }

    public CubicCoefficients1D getXCoefficients() {
        if (areCoefficientsDirty()) {
            calculateCoefficients();
        }
        return m_xCoeff;
    }

    public CubicCoefficients1D getYCoefficients() {
        if (areCoefficientsDirty()) {
            calculateCoefficients();
        }
        return m_yCoeff;
    }

    private void calculateCoefficients() {
        setCoefficientsDirty(false);

        double pointax = getTime();
        double pointbx = m_nextKey.getTime();
        double xspan = pointbx - pointax;

        double pointay = getValue();
        double pointby = m_nextKey.getValue();
        double pointcy = getNextTangent().getY();
        double pointdy = m_nextKey.getPrevTangent().getY();

        m_yCoeff = new CubicCoefficients1D(pointay, pointby, pointcy, pointdy);

        // if the weights are default, then the x cubic is linear and there is no need to evaluate it
        if (getNextMagnitude() == 1.0f && m_nextKey.getPrevMagnitude() == 1.0f)
            return;

        // Spline - non default tangents means that we need a second parametric cubic for x as a function of t
        double pointcx = getNextTangent().getX();
        double pointdx = m_nextKey.getPrevTangent().getX();

        double xspan3 = xspan * 3;

        // if c going beyond b limit
        if (pointcx > xspan3) {
            double ratio = xspan3 / pointcx;
            pointcx = xspan3;
        }

        // if d going beyond a limit
        if (pointdx > xspan3) {
            double ratio = xspan3 / pointdx;
            pointdx = xspan3;
        }

        m_xCoeff = new CubicCoefficients1D(pointax, pointbx, pointcx, pointdx);
    }

    public boolean isMarkbeginOrEndKeysToZeroSlope() {
        return m_markBeginOrEndKeysToZeroSlope;
    }

    public void setMarkBeginOrEndKeysToZeroSlope(boolean m_setBeginOrEndKeysToZeroSlope) {
        this.m_markBeginOrEndKeysToZeroSlope = m_setBeginOrEndKeysToZeroSlope;
    }

    public MotionKey getM_nextKey() {
        return m_nextKey;
    }

    public MotionKey getM_prevKey() {
        return m_prevKey;
    }

    public enum SlopeMethod {
        SLOPE_MANUAL, SLOPE_LINEAR, SLOPE_FLAT, SLOPE_SMOOTH, SLOPE_CLAMPED, SLOPE_PLATEAU,
        SLOPE_STEPPED, SLOPE_STEPPED_NEXT
    }

    public void setMagnitude(double magnitude) {
        m_prevAngleAndMagnitude.setY(magnitude);
        m_nextAngleAndMagnitude.setY(magnitude);
        m_nextSlopeMethod = m_prevSlopeMethod = SLOPE_MANUAL;
        onPositionChanged();
    }
    public double getMagnitude() {
        if (getPrevKey()!=null)
            return getPrevMagnitude();
        return getNextMagnitude();
    }

    public void setAngle(double angle) {
        m_prevAngleAndMagnitude.setX(angle);
        m_nextAngleAndMagnitude.setX(angle);
        m_nextSlopeMethod = m_prevSlopeMethod = SLOPE_MANUAL;
        onPositionChanged();
    }
    public double getAngle() {
        if (getPrevKey()!=null)
            return getPrevAngle();
        return getNextAngle();
    }

    public void setPrevMagnitude(double magnitude) {
        m_prevAngleAndMagnitude.setY(magnitude);
        m_prevSlopeMethod = SLOPE_MANUAL;
    }
    public void setNextMagnitude(double magnitude) {
        m_nextAngleAndMagnitude.setY(magnitude);
        m_nextSlopeMethod = SLOPE_MANUAL;
    }

    public double getPrevAngle() {
        return m_prevAngleAndMagnitude.getX();
    }
    public void setPrevAngle(double angle) {
        m_prevAngleAndMagnitude.setX(angle);
        m_prevSlopeMethod = SLOPE_MANUAL;
    }
    public double getNextAngle() {
        return m_nextAngleAndMagnitude.getX();
    }
    public void setNextAngle(double angle) {
        m_nextAngleAndMagnitude.setX(angle);
        m_nextSlopeMethod = SLOPE_MANUAL;
    }
}
