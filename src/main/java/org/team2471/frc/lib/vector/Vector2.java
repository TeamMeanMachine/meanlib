package org.team2471.frc.lib.vector;

public class Vector2 {
  public double x;
  public double y;

  public Vector2(double x, double y) {
    this.x = x;
    this.y = y;
  }

  public void set(double x, double y) {
    this.x = x;
    this.y = y;
  }

  public static Vector2 add(Vector2 firstVector, Vector2 secondVector) {
    return new Vector2(firstVector.x + secondVector.x, firstVector.y + secondVector.y);
  }

  public static Vector2 subtract(Vector2 firstVector, Vector2 secondVector) {
    return new Vector2(firstVector.x - secondVector.x, firstVector.y - secondVector.y);
  }

  public static Vector2 multiply(Vector2 firstVector, double factor) {
    return new Vector2(firstVector.x * factor, firstVector.y * factor);
  }

  public static Vector2 divide(Vector2 firstVector, double quotient) {
    return new Vector2(firstVector.x / quotient, firstVector.y / quotient);
  }

  public static Vector2 perpendicular(Vector2 vector) {
    return new Vector2(vector.y, -vector.x);
  }

  public static double length(Vector2 vector) {
    return Math.sqrt(dot( vector, vector ));
  }

  public static double angle(Vector2 vector) {
    return Math.atan2(vector.x, vector.y);
  }

  public static Vector2 normalize(Vector2 vector) {
    return divide(vector, length(vector));
  }

  public static double dot(Vector2 vecA, Vector2 vecB) {
    return vecA.x * vecB.x + vecA.y * vecB.y;
  }
}
