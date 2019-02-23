package org.team2471.frc.lib.motion_profiling;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Autonomous {

    public String name;
    public Map<String, Path2D> paths = new HashMap<>();

    private boolean m_mirrored = false;

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

    public Set<String> getPathNames() {
        return  paths.keySet();
    }

    public boolean isMirrored() {
        return m_mirrored;
    }

    public void setMirrored(boolean m_mirrored) {
        this.m_mirrored = m_mirrored;
    }
}
