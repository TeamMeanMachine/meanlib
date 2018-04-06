package org.team2471.frc.lib.motion_profiling;

import org.team2471.frc.lib.vector.Vector2;

public interface Point2DInterface {
    public void onPositionChanged();
    public Vector2 getPosition();
    public void setPosition(Vector2 position);
    public Vector2 getPrevTangent();
    public void setPrevTangent(Vector2 prevTangent);
    public Vector2 getNextTangent();
    public void setNextTangent(Vector2 nextTangent);
    public Point2DInterface getNextPoint();
    public void setNextPoint(Point2DInterface m_nextPoint);
    public Path2DPoint getPrevPoint();
    public void setPrevPoint(Point2DInterface m_prevPoint);


    }
