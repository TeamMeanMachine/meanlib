package org.team2471.frc.lib.motion_profiling;

import java.util.HashMap;
import java.util.Map;

public class Autonomous {

    public String name;
    public Map<String, Path2D> paths = new HashMap<>();

    private double trackWidth = 25.0 / 12.0;
    private double scrubFactor = 1.12;
    private double robotWidth = 30.0 / 12.0;
    private double robotLength = 38.0 / 12.0;

    public Autonomous(String name) {
        this.name = name;
    }

    public void putPath(Path2D path2D) {
        paths.put(path2D.name, path2D);
        path2D.setAutonomous(this);
    }

    public Path2D get(String name) {
        return paths.get(name);
    }

    void fixUpTailAndPrevPointers() {
        for (Map.Entry<String, Path2D> entry : paths.entrySet()) {
            entry.getValue().fixUpTailAndPrevPointers();
            entry.getValue().setAutonomous(this);
        }
    }

    public double getRobotLength() {
        return robotLength;
    }

    public void setRobotLength(double robotLength) {
        this.robotLength = robotLength;
    }

    public double getRobotWidth() {
        return robotWidth;
    }

    public void setRobotWidth(double _robotWidth) {
        this.robotWidth = _robotWidth;
    }

    public double getScrubFactor() {
        return scrubFactor;
    }

    public void setScrubFactor(double scrubFactor) {
        this.scrubFactor = scrubFactor;
    }

    public double getTrackWidth() {
        return trackWidth;
    }

    public void setTrackWidth(double trackWidth) {
        this.trackWidth = trackWidth;
    }
}
