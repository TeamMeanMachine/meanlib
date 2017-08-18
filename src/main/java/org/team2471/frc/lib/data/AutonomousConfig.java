package org.team2471.frc.lib.data;

import org.team2471.frc.lib.motion_profiling.Path2D;

import java.util.HashMap;
import java.util.Map;

public class AutonomousConfig {
  private final String name;
  private final Map<String, Path2D> paths = new HashMap<>();

  public AutonomousConfig(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void putPath(String name, Path2D path) {
    paths.put(name, path);
  }

  public Path2D getPath(String name) {
    return paths.get(name);
  }

  public void deletePath(String name) {
    paths.remove(name);
  }

  public void renamePath(String prevName, String newName) {
    paths.put(newName, paths.remove(prevName));
  }
}
