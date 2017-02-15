package org.team2471.frc.lib.io.log;

import java.io.PrintStream;

public class Logger {
  // Change this if you need a different printstream for whatever reason
  public static PrintStream out = System.out;

  private static LogLevel globalLogLevel = LogLevel.INFO;
  private LogLevel localLogLevel = LogLevel.NONE;

  public static synchronized void setGlobalLogLevel(LogLevel level) {
    globalLogLevel = level;
  }

  public synchronized void setLocalLogLevel(LogLevel level) {
    localLogLevel = level;
  }

  public void trace(String message) {
    log(LogLevel.TRACE, message);
  }

  public void debug(String message) {
    log(LogLevel.DEBUG, message);
  }

  public void info(String message) {
    log(LogLevel.INFO, message);
  }

  public void warn(String message) {
    log(LogLevel.WARN, message);
  }

  public void error(String message) {
    log(LogLevel.ERROR, message);
  }

  public void fatal(String message) {
    log(LogLevel.FATAL, message);
  }

  private void log(LogLevel level, String message) {
    // Not sure if this is more performant, but I'm gonna use a StringBuilder just in case.
    @SuppressWarnings("StringBufferReplaceableByString")
    StringBuilder messageBuilder = new StringBuilder();
    messageBuilder.append('[').append(level.name()).append(']')
        .append(' ').append(message);

    LogLevel currentLevel = localLogLevel != LogLevel.NONE ? localLogLevel : globalLogLevel;
    if(level.getStrength() >= currentLevel.getStrength() && currentLevel != LogLevel.NONE) {
      out.println(messageBuilder.toString());
    }
  }
}
