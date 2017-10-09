package org.team2471.frc.lib.control

import org.team2471.frc.lib.math.Point2D
import org.team2471.frc.lib.math.Vector2D
import java.lang.Math.toDegrees
import java.util.*

data class Waypoint(val point: Point2D, val radialTolerance: Double)

// see: http://www.ri.cmu.edu/pub_files/pub1/kelly_alonzo_1994_4/kelly_alonzo_1994_4.pdf
class PurePursuitController(private val lookahead: Double,
                            private var position: Point2D,
                            private var heading: Double,
                            vararg waypoints: Waypoint) {
    var finished = false
        private set

    private val remainingWaypoints: LinkedList<Waypoint> = LinkedList(waypoints.asList())
    private var lastPoint = position
    private val currentWaypoint = remainingWaypoints[0]

    private var lookaheadPoint: Point2D = position

    private val error: Double
        get() = position.vectorTo(lookaheadPoint).angle

    fun getCurvature(deltaVector: Vector2D): Double {
        // update robot pose
        position += deltaVector
        heading += toDegrees(deltaVector.angle)


        if (position.distance(currentWaypoint.point) <= currentWaypoint.radialTolerance) {
            lastPoint = currentWaypoint.point
        }


        return (2 * error) / lookahead
    }
}
