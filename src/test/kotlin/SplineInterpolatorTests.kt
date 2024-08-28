import org.junit.jupiter.api.Test
import org.team2471.frc.lib.interpolation.SplineInterpolator

class SplineInterpolatorTests {
    @Test
    fun test() {
        val x = SplineInterpolator(mutableMapOf(5.0 to 4.2, 2.6 to 3.2, 8.25 to 2.4, 0.0 to 5.5,))

        x.addPoint(10.0, 10.0)
    }
}