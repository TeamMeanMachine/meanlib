package org.team2471.frc.lib.motion_profiling;

import java.util.ArrayList;

public class MotionProfileAnimation {

    ArrayList<MotionProfileCurve> m_listMotionProfileCurves;

    public MotionProfileAnimation() {
        m_listMotionProfileCurves = new ArrayList<MotionProfileCurve>(0);
    }

    public void addMotionProfileCurve(MotionProfileCurve motionProfileCurve) {
        m_listMotionProfileCurves.add(motionProfileCurve);
    }

    public int getNumMotionProfileCurves() {
        return m_listMotionProfileCurves.size();
    }

    public MotionProfileCurve getMotionProfileCurveAt(int index) {
        return m_listMotionProfileCurves.get(index);
    }

    public double getLength() {  // as in how many seconds long is the longest motion.

        // walk list, return longest
        double longest = 0;
        for (MotionProfileCurve curve : m_listMotionProfileCurves) {
            longest = Math.max(longest, curve.getLength());
        }
        return longest;
    }

    public void play(double time) {
        for (MotionProfileCurve curve : m_listMotionProfileCurves) {
            curve.play(time);
        }
    }

    public void stop() {
        for (MotionProfileCurve curve : m_listMotionProfileCurves) {
            curve.stop();
        }
    }
}
