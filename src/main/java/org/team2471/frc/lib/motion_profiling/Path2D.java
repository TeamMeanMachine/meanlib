package org.team2471.frc.lib.motion_profiling;

import org.team2471.frc.lib.vector.Vector2;

public class Path2D {

  private MotionCurve m_xCurve;
  private MotionCurve m_yCurve;

  public Path2D() {
    m_xCurve = new MotionCurve();
    m_yCurve = new MotionCurve();
  }

  public void AddVector2( double time, Vector2 point ) {
    AddPoint( time, point.x, point.y );
  }

  public void AddPoint( double time, double x, double y ) {
    m_xCurve.storeValue( time, x );
    m_yCurve.storeValue( time, y );
  }

  public Vector2 getPosition( double time ) {
    return new Vector2( m_xCurve.getValue(time), m_yCurve.getValue(time));
  }

  public Vector2 getTangent( double time ) {
    return new Vector2( m_xCurve.getDerivative(time), m_yCurve.getDerivative(time));
  }

  public Vector2 getSidePosition( double time, double xOffset )  // offset can be positive or negative (half the width of the robot)
  {
    Vector2 startPosition = getPosition( time );
    Vector2 tangent = getTangent( time );
    tangent = Vector2.normalize( tangent );
    tangent = Vector2.perpendicular( tangent );
    tangent = Vector2.multiply( tangent, xOffset );
    startPosition = Vector2.add( startPosition, tangent );
    return startPosition;
  }
}
