package org.team2471.frc.lib.io.dashboard;

import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class DashboardUtils {
  public static final NetworkTable smartDashboardTable = NetworkTable.getTable("SmartDashboard");

  public static void putPersistantBoolean(String key, boolean fallback) {
    smartDashboardTable.setPersistent(key);
    if(!SmartDashboard.containsKey(key)) {
      smartDashboardTable.putBoolean(key, fallback);
    }
  }

  public static void putPersistantNumber(String key, double fallback) {
    smartDashboardTable.setPersistent(key);
    if(!SmartDashboard.containsKey(key)) {
      smartDashboardTable.putNumber(key, fallback);
    }
  }

  public static void putPersistantString(String key, String fallback) {
    smartDashboardTable.setPersistent(key);
    if(!SmartDashboard.containsKey(key)) {
      smartDashboardTable.putString(key, fallback);
    }
  }
}
