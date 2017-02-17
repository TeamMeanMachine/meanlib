package org.team2471.frc.lib.io.log;

import edu.wpi.first.wpilibj.Timer;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

public class Logger {
  private final String context;
  // Change this if you need a different printstream for whatever reason
  public static PrintStream out = System.out;

  private static LogLevel globalLogLevel = LogLevel.INFO;
  private LogLevel localLogLevel = LogLevel.NONE;


  private final Map<String, Double> traceSpamCache = new HashMap<>();
  private final Map<String, Double> debugSpamCache = new HashMap<>();
  private final Map<String, Double> infoSpamCache = new HashMap<>();
  private final Map<String, Double> warnSpamCache = new HashMap<>();
  private final Map<String, Double> errorSpamCache = new HashMap<>();

  public Logger(String context) {
    this.context = context;
  }

  public static synchronized void setGlobalLogLevel(LogLevel level) {
    globalLogLevel = level;
  }

  public synchronized void setLocalLogLevel(LogLevel level) {
    localLogLevel = level;
  }

  public void trace(String message) {
    log(LogLevel.TRACE, message);
  }

  public void trace(String message, double spamDelay) {
    if(!traceSpamCache.containsKey(message)) {
      traceSpamCache.put(message, Timer.getFPGATimestamp() + spamDelay);
      trace(message);
    } else if(Timer.getFPGATimestamp() > traceSpamCache.get(message)) {
      trace(message);
      traceSpamCache.remove(message);
    }
  }

  public void debug(String message) {
    log(LogLevel.DEBUG, message);
  }

  public void debug(String message, double spamDelay) {
    if(!debugSpamCache.containsKey(message)) {
      debugSpamCache.put(message, Timer.getFPGATimestamp() + spamDelay);
      debug(message);
    } else if(Timer.getFPGATimestamp() > debugSpamCache.get(message)) {
      debug(message);
      debugSpamCache.remove(message);
    }
  }

  public void info(String message) {
    log(LogLevel.INFO, message);
  }

  public void info(String message, double spamDelay) {
    if(!infoSpamCache.containsKey(message)) {
      infoSpamCache.put(message, Timer.getFPGATimestamp() + spamDelay);
      info(message);
    } else if(Timer.getFPGATimestamp() > infoSpamCache.get(message)) {
      info(message);
      infoSpamCache.remove(message);
    }
  }

  public void warn(String message) {
    log(LogLevel.WARN, message);
  }

  public void warn(String message, double spamDelay) {
    if(!warnSpamCache.containsKey(message)) {
      warnSpamCache.put(message, Timer.getFPGATimestamp() + spamDelay);
      warn(message);
    } else if(Timer.getFPGATimestamp() > warnSpamCache.get(message)) {
      warn(message);
      warnSpamCache.remove(message);
    }
  }

  public void error(String message) {
    log(LogLevel.ERROR, message);
  }

  public void error(String message, double spamDelay) {
    if(!errorSpamCache.containsKey(message)) {
      errorSpamCache.put(message, Timer.getFPGATimestamp() + spamDelay);
      error(message);
    } else if(Timer.getFPGATimestamp() > errorSpamCache.get(message)) {
      error(message);
      errorSpamCache.remove(message);
    }
  }

  public void fatal(String message) {
    log(LogLevel.FATAL, message);
  }

  public void log(LogLevel level, String message) {
    // Not sure if this is more performant, but I'm gonna use a StringBuilder just in case.
    @SuppressWarnings("StringBufferReplaceableByString")
    StringBuilder messageBuilder = new StringBuilder();
    messageBuilder
        .append('[').append(context).append(']').append(' ')
        .append('[').append(level.name()).append(']')
        .append(' ').append(message);

    LogLevel currentLevel = localLogLevel != LogLevel.NONE ? localLogLevel : globalLogLevel;
    if(level.getStrength() >= currentLevel.getStrength() && currentLevel != LogLevel.NONE) {
      out.println(messageBuilder.toString());
    }
  }
}
