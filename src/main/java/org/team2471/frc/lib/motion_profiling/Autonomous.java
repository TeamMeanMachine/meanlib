package org.team2471.frc.lib.motion_profiling;

import java.util.HashMap;
import java.util.Map;

public class Autonomous {

    public String name;
    public Map<String, Path2D> paths = new HashMap<>();

    public Autonomous(String name) {
        this.name = name;
    }

    public void putPath(Path2D path2D) {
        paths.put(path2D.name, path2D);
    }

    public Path2D getPath(String name) {
        return paths.get(name);
    }
}
