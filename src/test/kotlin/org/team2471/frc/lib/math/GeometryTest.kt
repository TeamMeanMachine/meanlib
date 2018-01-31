package org.team2471.frc.lib.math

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.within
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import java.lang.Math.sqrt

object GeometryTest : Spek({
    given("the unit circle") {
        val unitCircle = Circle.UNIT

        on("finding intersecting points with line y=x") {
            val line = Line(Point(-1.5, -1.5), Point(1.5, 1.5))
            val intersectingPoints = unitCircle.intersectingPoints(line)

            it("should find two points") {
                assertThat(intersectingPoints.size).isEqualTo(2)
            }
            it("should return points (-sqrt(0.5), -sqrt(0.5)), (sqrt(0.5), sqrt(0.5))") {
                assertThat(intersectingPoints[0].x).isCloseTo(-sqrt(0.5), within(0.0001))
                assertThat(intersectingPoints[0].y).isCloseTo(-sqrt(0.5), within(0.0001))
                assertThat(intersectingPoints[1].x).isCloseTo(sqrt(0.5), within(0.0001))
                assertThat(intersectingPoints[1].y).isCloseTo(sqrt(0.5), within(0.0001))
            }
        }
        on("finding intersecting points with line y=1") {
            val line = Line(Point(-1.0, 1.0), Point(1.0, 1.0))
            val intersectingPoints = unitCircle.intersectingPoints(line)

            it("should find one point") {
                assertThat(intersectingPoints.size).isEqualTo(1)
            }
            it("should return point (0.0, 1.0") {
                assertThat(intersectingPoints[0].x).isCloseTo(0.0, within(0.0001))
                assertThat(intersectingPoints[0].y).isCloseTo(1.0, within(0.0001))
            }
        }

        on("finding intersecting points with line y=3x+6") {
            val line = Line(Point(-2.0, 0.0), Point(0.0, 6.0))
            val intersectingPoints = unitCircle.intersectingPoints(line)

            it("should find no points") {
                assertThat(intersectingPoints.size).isEqualTo(0)
            }
        }
    }
})
