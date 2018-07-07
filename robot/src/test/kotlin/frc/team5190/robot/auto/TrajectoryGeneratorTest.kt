package frc.team5190.robot.auto

import com.xeiam.xchart.QuickChart
import com.xeiam.xchart.SwingWrapper
import frc.team254.lib.trajectory.TrajectoryIterator
import frc.team254.lib.trajectory.timing.TimedState
import frc.team5190.lib.geometry.Pose2d
import frc.team5190.lib.geometry.Pose2dWithCurvature
import frc.team5190.lib.geometry.Rotation2d
import org.junit.Test
import kotlin.math.atan2

class TrajectoryGeneratorTest {
    @Test
    fun testTrajectoryGenerator() {
        val waypoints = mutableListOf(
                Pose2d(1.5, 23.5, Rotation2d()),
                Pose2d(10.0, 23.5, Rotation2d.createFromRadians(0.0)),
                Pose2d(20.0, 16.5, Rotation2d.createFromRadians(-1.57)),
                Pose2d(20.0, 9.0, Rotation2d.createFromRadians(-1.57))
        )

        val startTime = System.currentTimeMillis()
        val trajectory = TrajectoryGenerator.generateTrajectory(false, waypoints, listOf(),
                20.0, 10.0, 6.0)

        val totalTime = System.currentTimeMillis() - startTime

        val xList = arrayListOf<Double>()
        val yList = arrayListOf<Double>()

        val trajectoryIterator = TrajectoryIterator<TimedState<Pose2dWithCurvature>>(trajectory!!.IndexView())


        var prevX = 0.0
        var prevY = 0.0

        while (!trajectoryIterator.isDone) {

            val point = trajectoryIterator.advance(0.02)

            val dx = point.state().state().pose.x - prevX
            val dy = point.state().state().pose.y - prevY


            println("X: ${point.state().state().pose.x}, Y: ${point.state().state().pose.y}, Theta: ${Math.toDegrees(atan2(dy / 0.02, dx / 0.02))}")

            xList.add(point.state().state().pose.x)
            yList.add(point.state().state().pose.y)

            prevX = point.state().state().pose.x
            prevY = point.state().state().pose.y
        }


        val chart = QuickChart.getChart("Path", "X", "Y",
                "Path", xList.toDoubleArray(), yList.toDoubleArray())

        SwingWrapper(chart).displayChart()

        println("\nJob took $totalTime milliseconds.")
        Thread.sleep(100000)
    }
}