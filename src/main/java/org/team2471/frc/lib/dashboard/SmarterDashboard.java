package org.team2471.frc.lib.dashboard;

import java.util.HashMap;
import java.util.Map;

import edu.wpi.first.wpilibj.Preferences;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.tables.TableKeyNotDefinedException;

// It's smarter because it remembers ☜(ﾟヮﾟ☜) (☞ﾟヮﾟ)☞
public class SmarterDashboard {
  private static final Preferences prefs = Preferences.getInstance();
  private static final Map<String, Boolean> booleanCache = new HashMap<>();
  private static final Map<String, Double> numberCache = new HashMap<>();
  private static final Map<String, String> stringCache = new HashMap<>();

  public static boolean getBoolean(String key, boolean fallback) {
    boolean value;

    try {
      value = SmartDashboard.getBoolean(key);
    } catch (TableKeyNotDefinedException ignored) {
      value = prefs.getBoolean(key, fallback);
      SmartDashboard.putBoolean(key, value);
    }

    booleanCache.put(key, value);
    return value;
  }

  public static double getNumber(String key, double fallback) {
    double value;

    try {
      value = SmartDashboard.getNumber(key);
    } catch (TableKeyNotDefinedException ignored) {
      value = prefs.getDouble(key, fallback);
      SmartDashboard.putNumber(key, value);
    }

    numberCache.put(key, value);
    return value;
  }

  public static String getString(String key, String fallback) {
    String value;

    try {
      value = SmartDashboard.getString(key);
    } catch (TableKeyNotDefinedException ignored) {
      value = prefs.getString(key, fallback);
      SmartDashboard.putString(key, value);
    }

    stringCache.put(key, value);
    return value;
  }

  public static void putBoolean(String key, boolean value) {
    SmartDashboard.putBoolean(key, value);
  }

  public static void putNumber(String key, double value) {
    SmartDashboard.putNumber(key, value);
  }

  public static void putString(String key, String value) {
    SmartDashboard.putString(key, value);
  }

  public static void updatePrefs() {
    booleanCache.forEach(prefs::putBoolean);
    numberCache.forEach(prefs::putDouble);
    stringCache.forEach(prefs::putString);
  }
}
