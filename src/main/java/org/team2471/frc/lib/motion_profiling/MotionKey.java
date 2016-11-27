package org.team2471.frc.lib.motion_profiling;

import org.team2471.frc.lib.vector.Vector2;

public class MotionKey {
    private Vector2 m_timeAndValue;
    private Vector2 m_prevAngleAndMagnitude;
    private Vector2 m_nextAngleAndMagnitude;
    private Vector2 m_prevTangent;
    private Vector2 m_nextTangent;
    private boolean m_bTangentsDirty;

    public enum SlopeMethod { SLOPE_MANUAL, SLOPE_LINEAR, SLOPE_FLAT, SLOPE_SMOOTH, SLOPE_CLAMPED, SLOPE_PLATEAU,
        SLOPE_STEPPED, SLOPE_STEPPEDNEXT };
    private SlopeMethod m_prevSlopeMethod;
    private SlopeMethod m_nextSlopeMethod;

    private MotionCurve m_motionCurve;
    private MotionKey m_nextKey;
    private MotionKey m_prevKey;

    public MotionKey() {
        m_timeAndValue.set( 0, 0 );
        m_prevAngleAndMagnitude.set(0,1);
        m_nextAngleAndMagnitude.set(0,1);
        m_bTangentsDirty = true;
        m_prevSlopeMethod = SlopeMethod.SLOPE_SMOOTH;
        m_nextSlopeMethod = SlopeMethod.SLOPE_SMOOTH;
        m_motionCurve = null;
        m_nextKey = null;
        m_prevKey = null;
    }

    void onPositionChanged()
    {
    }
    
    public double getTime() {return m_timeAndValue.x;}
    public double getValue() {return m_timeAndValue.y;}
    public boolean areTangentsDirty() {return m_bTangentsDirty;}
    public void setTangentsDirty(boolean bTangentsDirty) {m_bTangentsDirty = bTangentsDirty;}
    public Vector2 getTimeAndValue() {return m_timeAndValue;}
    public void setTimeAndValue(Vector2 m_timeAndValue) {this.m_timeAndValue = m_timeAndValue;}
    public Vector2 getPrevAngleAndMagnitude() {return m_prevAngleAndMagnitude;}
    public void setPrevAngleAndMagnitude(Vector2 m_prevAngleAndMagnitude) {this.m_prevAngleAndMagnitude = m_prevAngleAndMagnitude;}
    public Vector2 getNextAngleAndMagnitude() {return m_nextAngleAndMagnitude;}
    public void setNextAngleAndMagnitude(Vector2 m_nextAngleAndMagnitude) {this.m_nextAngleAndMagnitude = m_nextAngleAndMagnitude;}
    public Vector2 getPrevTangent() {return m_prevTangent;}
    public void setPrevTangent(Vector2 m_PrevTangent) {this.m_prevTangent = m_PrevTangent;}
    public Vector2 getNextTangent() {return m_nextTangent;}
    public void setNextTangent(Vector2 m_NextTangent) {this.m_nextTangent = m_NextTangent;}
    public MotionCurve getMotionCurve() {return m_motionCurve;}
    public void setMotionCurve(MotionCurve m_motionCurve) {this.m_motionCurve = m_motionCurve;}
    public MotionKey getNextKey() {return m_nextKey;}
    public void setNextKey(MotionKey m_nextKey) {this.m_nextKey = m_nextKey;}
    public MotionKey getPrevKey() {return m_prevKey;}
    public void setPrevKey(MotionKey m_prevKey) {this.m_prevKey = m_prevKey;}
    public SlopeMethod getPrevSlopeMethod() {return m_prevSlopeMethod;}
    public void setPrevSlopeMethod(SlopeMethod slopeMethod) {m_prevSlopeMethod = slopeMethod;}
    public SlopeMethod getNextSlopeMethod() {return m_nextSlopeMethod;}
    public void setNextSlopeMethod(SlopeMethod slopeMethod) {m_prevSlopeMethod = slopeMethod;}
    public double getPrevMagnitude() { return m_prevAngleAndMagnitude.y; }
    public double getNextMagnitude() { return m_nextAngleAndMagnitude.y; }

    void InsertBefore(MotionKey newKey)
    {
        m_prevKey = newKey.m_prevKey;
        if (newKey.m_prevKey!=null)
            newKey.m_prevKey.m_nextKey = this;
        newKey.m_prevKey = this;
        m_nextKey = newKey;
    }

    void InsertAfter(MotionKey newKey)
    {
        m_nextKey = newKey.m_nextKey;
        if (newKey.m_nextKey!=null)
            newKey.m_nextKey.m_prevKey = this;
        newKey.m_nextKey = this;
        m_prevKey = newKey;
    }
    
    void CalculateTangents()
    {
        if (!areTangentsDirty())
            return;

        setTangentsDirty( false );

        boolean bCalcSmoothPrev = false;
        boolean bCalcSmoothNext = false;

        switch (getPrevSlopeMethod())
        {
            case SLOPE_MANUAL:
                m_prevTangent.set( Math.cos( getPrevAngleAndMagnitude().x ), Math.sin( getPrevAngleAndMagnitude().x ) );
                if (m_prevKey!=null)
                    Vector2.multiply(m_prevTangent, getTimeAndValue().x - m_prevKey.getTimeAndValue().x );
                break;
            case SLOPE_LINEAR:
                if (m_prevKey!=null)
                    m_prevTangent = Vector2.subtract(getTimeAndValue(), m_prevKey.getTimeAndValue());
                break;
            case SLOPE_FLAT:
                if (m_prevKey!=null)
                    m_prevTangent.set( getTimeAndValue().x - m_prevKey.getTimeAndValue().x, 0.0f );
                break;
            case SLOPE_SMOOTH:
                bCalcSmoothPrev = true;
                break;
            case SLOPE_CLAMPED:
            {
                float fClampTolerence = (GetMotionCurve()->GetMaxValue() - GetMotionCurve()->GetMinValue()) * CLAMPTOLERANCE;
                if (pPrevKey && fabs(pPrevKey->GetValue() - GetValue()) <= fClampTolerence) // make Flat
                    m_inTangent.Set( GetTime().GetSeconds()-pPrevKey->GetTime().GetSeconds(), 0.0f );
                else if (pNextKey && fabs(pNextKey->GetValue() - GetValue()) <= fClampTolerence) // Make Flat
                {
                    if (pPrevKey)
                        m_inTangent.Set( GetTime().GetSeconds()-pPrevKey->GetTime().GetSeconds(), 0.0f );
                    else
                        m_inTangent.Set( 0.0f, 0.0f );
                }
                else
                    bCalcSmoothIn = TRUE;
                break;
            }
            case SLOPE_PLATEAU:
                if (!pPrevKey || !pNextKey)
                {
                    if (pPrevKey)
                        m_inTangent.Set( GetTime().GetSeconds()-pPrevKey->GetTime().GetSeconds(), 0.0f ); // Make Flat
                    else
                        m_inTangent.Set( 0.0f, 0.0f );
                }
                else // we have a prev and a next, lets see if both the prev's out tangent and the next's in tangent are both either greater or less than our value, if so lets make out tangent flat
                {
                    float fPrevTangentValue;
                    if (pPrevKey->GetOutSlopeMethod() == SLOPE_PLATEAU)
                        fPrevTangentValue = pPrevKey->GetValue(); // This way we don't get an infinite recursion
                    else
                    {
                        Vector2 vPrevPos( pPrevKey->GetTime().GetSeconds(), pPrevKey->GetValue() );
                        Vector2 vPrevTangent = (pPrevKey->GetOutTangent() * 0.3333f) + vPrevPos;
                        fPrevTangentValue = vPrevTangent.Y();
                    }

                    float fNextTangentValue;
                    if (pNextKey->GetInSlopeMethod() == SLOPE_PLATEAU)
                        fNextTangentValue = pNextKey->GetValue(); // This way we don't get an infinite recursion
                    else
                    {
                        Vector2 vNextPos( pNextKey->GetTime().GetSeconds(), pNextKey->GetValue() );
                        Vector2 vNextTangent = vNextPos - (pNextKey->GetInTangent() * 0.3333f);
                        fNextTangentValue = vNextTangent.Y();
                    }

                    float fValue = GetValue();
                    if (fPrevTangentValue > fValue && fNextTangentValue > fValue )
                        m_inTangent.Set( GetTime().GetSeconds()-pPrevKey->GetTime().GetSeconds(), 0.0f ); // Make Flat
                    else if (fPrevTangentValue < fValue && fNextTangentValue < fValue)
                        m_inTangent.Set( GetTime().GetSeconds()-pPrevKey->GetTime().GetSeconds(), 0.0f ); // Make Flat
                    else
                        bCalcSmoothIn = TRUE;
                }
                break;
            case SLOPE_SMOOTH:
                bCalcSmoothIn = TRUE;
                break;
            case SLOPE_STEPPED:
            case SLOPE_STEPPEDNEXT:
                ASSERT( FALSE ); // Not a valid method for In Interp Method, it is an out only interp method
                break;
        }

        switch (getNextSlopeMethod())
        {
            case SLOPE_MANUAL:
                m_nextTangent.set( Math.cos( getNextAngleAndMagnitude().x ), Math.sin( getNextAngleAndMagnitude().x ) );
                if (m_nextKey!=null)
                    Vector2.multiply(m_nextTangent, m_nextKey.getTimeAndValue().x - getTimeAndValue().x);
                break;
            case SLOPE_LINEAR:
                if (m_nextKey!=null)
                    m_nextTangent = Vector2.subtract(m_nextKey.getTimeAndValue(), getTimeAndValue());
                break;
            case SLOPE_FLAT:
                if (m_nextKey!=null)
                    m_nextTangent.set(m_nextKey.getTimeAndValue().x - getTimeAndValue().x, 0.0f);
                break;
            case SLOPE_SMOOTH:
                bCalcSmoothNext = true;
                break;
            case SLOPE_CLAMPED:
            {
                float fClampTolerence = (GetMotionCurve()->GetMaxValue() - GetMotionCurve()->GetMinValue()) * CLAMPTOLERANCE;
                if (pPrevKey && fabs(pPrevKey->GetValue() - GetValue()) <= fClampTolerence) // make Flat
                {
                    if (pNextKey)
                        m_outTangent.Set( pNextKey->GetTime().GetSeconds() - GetTime().GetSeconds(), 0.0f );
                    else
                        m_outTangent.Set( 0.0f, 0.0f );
                }
                else if (pNextKey && fabs(pNextKey->GetValue() - GetValue()) <= fClampTolerence) // Make Flat
                    m_outTangent.Set( pNextKey->GetTime().GetSeconds() - GetTime().GetSeconds(), 0.0f );
                else
                    bCalcSmoothOut = TRUE;
                break;
            }
            case SLOPE_PLATEAU:
                if (!pPrevKey || !pNextKey)
                {
                    if (pNextKey)
                        m_outTangent.Set( pNextKey->GetTime().GetSeconds() - GetTime().GetSeconds(), 0.0f ); // Make it flat
                    else
                        m_outTangent.Set( 0.0f, 0.0f );
                }
                else // we have a prev and a next, lets see if both the prev's out tangent and the next's in tangent are both either greater or less than our value, if so lets make out tangent flat
                {
                    float fPrevTangentValue;
                    if (pPrevKey->GetOutSlopeMethod() == SLOPE_PLATEAU)
                        fPrevTangentValue = pPrevKey->GetValue(); // This way we don't get an infinite recursion
                    else
                    {
                        Vector2 vPrevPos( pPrevKey->GetTime().GetSeconds(), pPrevKey->GetValue() );
                        Vector2 vPrevTangent = (pPrevKey->GetOutTangent() * 0.3333f) + vPrevPos;
                        fPrevTangentValue = vPrevTangent.Y();
                    }

                    float fNextTangentValue;
                    if (pNextKey->GetInSlopeMethod() == SLOPE_PLATEAU)
                        fNextTangentValue = pNextKey->GetValue(); // This way we don't get an infinite recursion
                    else
                    {
                        Vector2 vNextPos( pNextKey->GetTime().GetSeconds(), pNextKey->GetValue() );
                        Vector2 vNextTangent = vNextPos - (pNextKey->GetInTangent() * 0.3333f);
                        fNextTangentValue = vNextTangent.Y();
                    }

                    float fValue = GetValue();
                    if (fPrevTangentValue > fValue && fNextTangentValue > fValue )
                        m_outTangent.Set( pNextKey->GetTime().GetSeconds() - GetTime().GetSeconds(), 0.0f ); // Make it flat
                    else if (fPrevTangentValue < fValue && fNextTangentValue < fValue)
                        m_outTangent.Set( pNextKey->GetTime().GetSeconds() - GetTime().GetSeconds(), 0.0f ); // Make it flat
                    else
                        bCalcSmoothOut = TRUE;
                }
                break;
            case SLOPE_SMOOTH:
                bCalcSmoothOut = TRUE;
                break;
            case SLOPE_STEPPED:
            case SLOPE_STEPPEDNEXT:
                break; // nothing to do, no tangents
        }

        if (bCalcSmoothPrev || bCalcSmoothNext)
        {
            if (m_prevKey!=null && m_nextKey!=null)
            {
                Vector2 delta = Vector2.subtract( m_nextKey.getTimeAndValue(), m_prevKey.getTimeAndValue() );
                double weight = Math.abs(delta.x);
                if (weight == 0) // if keys are on top of one another (no tangents)
                {
                    if (bCalcSmoothPrev)
                        m_prevTangent.set(0,0);
                    if (bCalcSmoothNext)
                        m_nextTangent.set(0,0);
                }
                else
                {
                    delta = Vector2.divide( delta, weight );

                    if (bCalcSmoothPrev)
                    {
                        double prevWeight = getTimeAndValue().x - m_prevKey.getTimeAndValue().x;
                        m_prevTangent = Vector2.multiply(delta, prevWeight);
                    }
                    if (bCalcSmoothNext)
                    {
                        double nextWeight = m_nextKey.getTimeAndValue().x - getTimeAndValue().x;
                        m_nextTangent = Vector2.multiply(delta, nextWeight);
                    }
                }
            }
            else
            {
                if (m_nextKey!=null)
                {
                    if (bCalcSmoothPrev)
                        m_prevTangent = Vector2.subtract(m_nextKey.getTimeAndValue(), getTimeAndValue());

                    if (bCalcSmoothNext)
                        m_nextTangent = Vector2.subtract(m_nextKey.getTimeAndValue(), getTimeAndValue());
                }

                if (m_prevKey!=null)
                {
                    if (bCalcSmoothPrev)
                        m_prevTangent = Vector2.subtract(getTimeAndValue(), m_prevKey.getTimeAndValue());

                    if (bCalcSmoothNext)
                        m_nextTangent = Vector2.subtract(getTimeAndValue(), m_prevKey.getTimeAndValue());
                }
            }
        }

        m_prevTangent = Vector2.multiply( m_prevTangent, getPrevAngleAndMagnitude().y );
        m_nextTangent = Vector2.multiply( m_nextTangent, getNextAngleAndMagnitude().y );
    }
}
