package frc.team5190.lib.util

import org.junit.Test

class PathreaderTest {
    @Test
    fun testSerialization() {
        val trajectory = Pathreader.getPathCollection("src/main/resources/Raw/LS-LL 1st Cube.json")
        trajectory.segments.forEach { segment ->
            System.out.printf("%.3f, %.3f, %.3f %n", segment.x, segment.y, segment.velocity)
        }
    }
}