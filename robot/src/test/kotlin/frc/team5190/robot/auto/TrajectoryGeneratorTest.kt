package frc.team5190.robot.auto

import frc.team5190.lib.geometry.Pose2d
import frc.team5190.lib.geometry.Rotation2d
import org.junit.Test

class TrajectoryGeneratorTest {
    @Test
    fun testTrajectoryGenerator() {
        val waypoints = mutableListOf(
                Pose2d(0.0, 0.0, Rotation2d()),
                Pose2d(10.0, 10.0, Rotation2d.createFromDegrees(-45.0))
        )

        val startTime = System.currentTimeMillis()
        val trajectory = TrajectoryGenerator.generateTrajectory(false, waypoints, listOf(),
                10.0, 5.0, 6.0)

        val totalTime = System.currentTimeMillis() - startTime

        println(trajectory)
        println("Job took $totalTime milliseconds.")
    }
}