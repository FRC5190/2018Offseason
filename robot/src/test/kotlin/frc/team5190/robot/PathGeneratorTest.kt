package frc.team5190.robot

import org.junit.Test


class PathGeneratorTest {
    @Test
    fun initializePaths() {
        PathGenerator

        while (!PathGenerator.pathsGenerated) {}

        PathGenerator.getPath("25 Feet").segments.forEach {
            System.out.printf("X: %.4f, Y: %.4f%n", it.x, it.y)
        }
    }
}