


import org.junit.jupiter.api.Test
import org.team2471.frc.lib.math.Vector2
import java.nio.ByteBuffer

class Vector2Tests {
    @Test
    fun closestPointTest() {
        val points = arrayOf(Vector2(3.2, 6.4), Vector2(7.0, 2.2), Vector2(0.0, 0.0))
        val point = Vector2(-2.0, -2.8)
        println(point.getClosestPoint(*points))
        assert(point.getClosestPoint(*points) == Vector2(0.0, 0.0))
    }
}