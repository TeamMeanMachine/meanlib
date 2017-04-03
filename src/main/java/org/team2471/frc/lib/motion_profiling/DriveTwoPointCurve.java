package org.team2471.frc.lib.motion_profiling;

import org.team2471.frc.lib.control.CANController;
import org.team2471.frc.lib.motion_profiling.FollowPathTankDriveCommand;
import org.team2471.frc.lib.motion_profiling.Path2D;

public class DriveTwoPointCurve extends FollowPathTankDriveCommand {

  Path2D m_path;

  public DriveTwoPointCurve(double x1, double y1, double tanx1, double tany1,
                            double x2, double y2, double tanx2, double tany2,
                            double time, boolean mirror, double travelDirection,
                            CANController leftController, CANController rightController) {

    setMirrorPath(mirror);
    setLeftController(leftController);
    setRightController(rightController);

    m_path = new Path2D();
    m_path.setTravelDirection(travelDirection);

    m_path.addPointAndTangent(x1, y1, tanx1, tany1);
    m_path.addPointAndTangent(x2, y2, tanx2, tany2);

    m_path.addEasePoint(0.0, 0.0);
    m_path.addEasePoint(time, 1.0);

    setPath(m_path);
  }
}
