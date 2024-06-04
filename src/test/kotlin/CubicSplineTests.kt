import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.team2471.frc.lib.math.CubicSpline
import org.team2471.frc.lib.math.Vector2

class CubicSplineTests {
    @Test
    fun `(2,-2) 3 to (5,5) -1 equation`() {
        val test = CubicSpline(Vector2(2.0, -2.0), Vector2(5.0, 5.0), 3.0, -1.0)
        println(test.toString())
        assertEquals(-0.29629629629629634, test.a)
        assertEquals(2.444444444444445, test.b)
        assertEquals(-3.222222222222224, test.c)
        assertEquals(-2.962962962962962, test.d)
    }
}