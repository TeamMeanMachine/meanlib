package org.team2471.frc.lib.motion_profiling;

import java.util.ArrayList;

/**
 * Created by Bob on 12/9/2016.
 */
public class Animation {
    public Animation() {
    }

    public void addMotionProfilingCurve( MotionProfilingCurve motionProfilingCurve ) {
        m_listMotionProfilingCurves.add( motionProfilingCurve );
    }

    public int getNumMotionProfilingCurves() {
        return m_listMotionProfilingCurves.size();
    }

    public MotionProfilingCurve getMotionProfilingCurveAt( int index ) {
        return m_listMotionProfilingCurves.get(index);
    }

    public double getLength() {  // as in how many seconds long is the longest motion.

        // walk list, return longest
        double longest = 0;
        for ( MotionProfilingCurve curve : m_listMotionProfilingCurves ) {
            longest = Math.max( longest, curve.getLength() );
        }
        return longest;
    }

    public void play( double time ) {
        for ( MotionProfilingCurve curve : m_listMotionProfilingCurves ) {
            curve.play( time );
        }
    }

    private ArrayList<MotionProfilingCurve> m_listMotionProfilingCurves;
}
