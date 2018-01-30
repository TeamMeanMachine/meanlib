package org.team2471.frc.lib.motion_profiling;

import edu.wpi.first.wpilibj.PIDInterface;

public class MotionProfileCurve extends MotionCurve {

    private PIDInterface pidInterface;
    private double offset = 0.0;

    public MotionProfileCurve(PIDInterface pidInterface) {
        this.pidInterface = pidInterface;
    }

    public MotionProfileCurve(PIDInterface pidInterface, MotionProfileAnimation animation) {
        this.pidInterface = pidInterface;
        animation.addMotionProfileCurve(this);
    }

    public void play(double time) {
        pidInterface.setSetpoint(getValue(time) + offset);
        System.out.println("Time: " + time + " Value: " + getValue(time));
    }

    public void stop() {
    }

    public double getOffset() {
        return offset;
    }

    public void setOffset(double offset) {
        this.offset = offset;
    }
}
