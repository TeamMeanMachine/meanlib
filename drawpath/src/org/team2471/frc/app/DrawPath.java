package org.team2471.frc.app;

import org.team2471.frc.lib.motion_profiling.Path2D;
import org.team2471.frc.lib.vector.Vector2;

import javax.swing.*;
import java.awt.*;

public class DrawPath extends JFrame {

  Path2D m_path;

  public DrawPath() {
    setSize(1024, 768);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setLocationRelativeTo(null);
    setVisible(true);

  }

  public static void main(String a[]) {
    new DrawPath();
  }

  public void paint(Graphics g) {

    Graphics2D g2 = (Graphics2D) g;

    g2.setStroke(new BasicStroke(6));
    g2.setColor(Color.black);

    if (m_path == null) {
      m_path = new Path2D();

      double size = 2.0;
      double tangent = 4.0;
      m_path.addPointAndTangent(0.0, 0.0, 0.0, tangent);
      m_path.addPointAndTangent(size, size, tangent, 0.0);

      m_path.addEasePoint(0.0, 0.0);
      m_path.addEasePoint(2.0, 1.0);

/*
      m_path.addPointAndTangent( 0.0,  0.0, 0.0, 1.0 );
      m_path.addPoint(  -4.0,  4.0 );
      m_path.addPoint( -34.0, -4.0 );
      m_path.addPoint( -38.0,  0.0 );
      m_path.addPoint( -34.0,  4.0 );
      m_path.addPoint(  -4.0, -4.0 );
      m_path.addPointAndTangent( 0.0,  0.0, 0.0, 1.0 );

      m_path.addEasePoint(    0,   0 );
//      m_path.addEasePoint(    10,   0.5 );  // middle point
      m_path.addEasePoint(  7.0,0.40 );  // two middle points to create a slowdown
      m_path.addEasePoint( 13.0,0.60 );  // two middle points to create a slowdown
*/

/*
      m_path.addEasePoint( 20.0, 1.0 );
      m_path.addPointAndTangent(0.0, 0.0, 0.0, 2.0);
      m_path.addPoint(5.0, 5.0);
      m_path.addPointAndTangent(10.0, 0.0, 0.0, -2.0);
      m_path.addPoint(10.0, -7);

      m_path.addEasePoint(0.0, 0.0);
      m_path.addEasePoint(8.0, 1.0);
*/
/*      m_path.addPointAndTangent(0.0, 0.0, 0.0, 4.5);
      m_path.addPointAndTangent(4.0, 4.0, 4.5, 0.0);
      m_path.addPointAndTangent(8.0, 0.0, 0.0, -4.5);
      m_path.addPointAndTangent(4.0, -4.0, -4.5, 0.0);
      m_path.addPointAndTangent(0.0, 0.0, 0.0, 4.5);

      m_path.addEasePoint(0.0, 0.0);
      m_path.addEasePoint(8.0, 1.0);*/
    }

    // get the stuff ready for the path drawing loop
    double numPoints = 100.0;
    double deltaT = m_path.getDuration() / numPoints;
    Vector2 prevPos = m_path.getPosition(0.0);
    Vector2 prevLeftPos = m_path.getLeftPosition(0.0);
    Vector2 prevRightPos = m_path.getRightPosition(0.0);
    Vector2 pos, leftPos, rightPos;
    double prevEase = 0.0;
    final double MAX_SPEED = 8.0;

    for (double t = deltaT; t <= m_path.getDuration(); t += deltaT) {
      pos = m_path.getPosition(t);
      leftPos = m_path.getLeftPosition(t);
      rightPos = m_path.getRightPosition(t);

      // center line
      g2.setColor(Color.black);
      drawPathLine(g2, prevPos, pos);

      // left wheel
      double leftSpeed = Vector2.length(Vector2.subtract(leftPos, prevLeftPos)) / deltaT;
      //System.out.println("left Speed="+leftSpeed);
      leftSpeed /= MAX_SPEED;  // MAX_SPEED is full green, 0 is full red.
      leftSpeed = Math.min(1.0, leftSpeed);
      double leftDelta = m_path.getLeftPositionDelta(t);
      if (leftDelta>0)
        g2.setColor(new Color((int) ((1.0 - leftSpeed) * 255), (int) (leftSpeed * 255), 0));
      else {
        double blue = Math.max(Math.min(128-leftDelta*500, 255),0);
        g2.setColor(new Color(0, 0, (int)blue));
      }
      drawPathLine(g2, prevLeftPos, leftPos);

      // right wheel
      double rightSpeed = Vector2.length(Vector2.subtract(rightPos, prevRightPos)) / deltaT / MAX_SPEED;
      rightSpeed = Math.min(1.0, rightSpeed);
      double rightDelta = m_path.getRightPositionDelta(t);
      System.out.println("Right: " + rightDelta);
      if (rightDelta>0)
        g2.setColor(new Color((int) ((1.0 - rightSpeed) * 255), (int) (rightSpeed * 255), 0));
      else {
        double blue = Math.max(Math.min(128-leftDelta*500, 255),0);
        g2.setColor(new Color(0, 0, (int)blue));
      }
      drawPathLine(g2, prevRightPos, rightPos);

      // set the prevs for the next loop
      prevPos.set(pos.x, pos.y);
      prevLeftPos.set(leftPos.x, leftPos.y);
      prevRightPos.set(rightPos.x, rightPos.y);
    }

    g2.setStroke(new BasicStroke(3));

    for (double t = deltaT; t <= m_path.getDuration(); t += deltaT) {
      // draw the ease curve too
      g2.setColor(Color.black);
      double ease = m_path.getEaseCurve().getValue(t);
      double prevT = t - deltaT;
//      g2.drawLine((int) (prevT * 40 + 600), (int) (prevEase * -200 + 700), (int) (t * 40 + 600), (int) (ease * -200 + 700));
      g2.drawLine((int) (prevT * 40 + 100), (int) (prevEase * -200 + 700), (int) (t * 40 + 100), (int) (ease * -200 + 700));
      prevEase = ease;
    }
  }

  void drawPathLine( Graphics2D g2, Vector2 p1, Vector2 p2 ) {
    final double scale = 100;
    final double xOffset = 300;
    final double yOffset = 500;
/*
    final double scale = 40.0;
    final double xOffset = 500.0;
    final double yOffset = 400.0;
*/

    //g2.drawLine( (int)(p1.x*-scale+xOffset), (int)(p1.y*scale+yOffset), (int)(p2.x*-scale+xOffset), (int)(p2.y*scale+yOffset) );
    g2.drawLine( (int)(p2.x*scale+xOffset), (int)(p2.y*-scale+yOffset), (int)(p2.x*scale+xOffset), (int)(p2.y*-scale+yOffset) );
  }
}