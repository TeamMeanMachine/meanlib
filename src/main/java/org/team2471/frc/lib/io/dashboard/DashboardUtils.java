package org.team2471.frc.lib.io.dashboard;

import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class DashboardUtils {
  public static final NetworkTable smartDashboardTable = NetworkTable.getTable("SmartDashboard");

  public static void putPersistentBoolean(NetworkTable table, String key, boolean fallback) {
    table.setPersistent(key);
    if(!SmartDashboard.containsKey(key)) {
      table.putBoolean(key, fallback);
    }
  }

  public static void putPersistentBoolean(String key, boolean fallback) {
    putPersistentBoolean(smartDashboardTable, key, fallback);
  }

  public static void putPersistentNumber(NetworkTable table, String key, double fallback) {
    table.setPersistent(key);
    if(!SmartDashboard.containsKey(key)) {
      table.putNumber(key, fallback);
    }
  }

  public static void putPersistentNumber(String key, double fallback) {
    putPersistentNumber(smartDashboardTable, key, fallback);
  }

  public static void putPersistentString(NetworkTable table, String key, String fallback) {
    table.setPersistent(key);
    if(!SmartDashboard.containsKey(key)) {
      table.putString(key, fallback);
    }
  }

  public static void putPersistentString(String key, String fallback) {
    putPersistentString(smartDashboardTable, key, fallback);
  }
}
