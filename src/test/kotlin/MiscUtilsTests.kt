import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.team2471.frc.lib.util.calculateAverage

class MiscUtilsTests {
    @Test
    fun averageOf1To10() {
        var average = 1.0
        var size = 1
        for (i in 2..10) {
            size += 1
            average = calculateAverage(average, i.toDouble(), size)
        }
        assertEquals(5.5, average)
    }

    @Test
    fun randomNumbersAverageTest() {
        val values = listOf(1.56, 2.37, 0.38, 7.98, 2.81, 5.26, 2.33, 7.36, 8.39, 9.04, 0.23)
        var size = 0
        var average = 0.0
        for (i in values) {
            size += 1
            average = calculateAverage(average, i, size)
        }
        assertEquals(4.337272727272727, average)
    }
}