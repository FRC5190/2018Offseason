package frc.team5190.robot.auto

import com.xeiam.xchart.QuickChart
import com.xeiam.xchart.SwingWrapper
import frc.team5190.lib.geometry.Pose2d
import frc.team5190.lib.geometry.Rotation2d
import frc.team5190.lib.geometry.Translation2d
import frc.team5190.lib.trajectory.TrajectoryGenerator
import frc.team5190.lib.trajectory.TrajectoryIterator
import org.junit.Test

class TrajectoryGeneratorTest {
    @Test
    fun testTrajectoryGenerator() {
        val waypoints = mutableListOf(
                Pose2d()
        )

        val startTime = System.currentTimeMillis()



        @Suppress("UNUSED_VARIABLE")
        val trajectory = TrajectoryGenerator.generateTrajectory(false, waypoints, listOf(),
                10.0, 6.0)!!

        val totalTime = System.currentTimeMillis() - startTime
        println("Trajectory Generation took $totalTime milliseconds.")

        println("Trajectory Duration: ${trajectory.lastState.t} seconds")

        val iterator = TrajectoryIterator(trajectory.indexView)

        val xList = arrayListOf<Double>()
        val yList = arrayListOf<Double>()

        while (!iterator.isDone) {
            val point = iterator.advance(0.02)
            xList.add(point.state.state.translation.x)
            yList.add(point.state.state.translation.y)

            println("X: ${point.state.state.translation.x}, Y: ${point.state.state.translation.y}, V: ${point.state.velocity}")
        }

        SwingWrapper(QuickChart.getChart(" ", " ", " ", " ", xList.toDoubleArray(), yList.toDoubleArray())).displayChart()
        Thread.sleep(10000000)

    }
}