package frc.team5190.robot.auto

import com.xeiam.xchart.QuickChart
import com.xeiam.xchart.SwingWrapper
import frc.team5190.lib.geometry.Pose2d
import frc.team5190.lib.geometry.Rotation2d
import frc.team5190.lib.geometry.Twist2d
import org.junit.Test
import java.lang.Math.atan2

class TrajectoryGeneratorTest {
    @Test
    fun testTrajectoryGenerator() {
        val waypoints = mutableListOf(
                Pose2d(1.5, 23.5, Rotation2d()),
                Pose2d(10.0, 23.5, Rotation2d.createFromRadians(0.0)),
                Pose2d(20.0, 16.5, Rotation2d.createFromRadians(-1.57)),
                Pose2d(20.0, 9.0, Rotation2d.createFromRadians(-1.57)),
                Pose2d(23.0, 7.0, Rotation2d.createFromRadians(0.174))
        )

        val startTime = System.currentTimeMillis()
        val trajectory = TrajectoryGenerator.generateTrajectory(false, waypoints, listOf(),
                10.0, 5.0, 6.0)

        val xList = arrayListOf<Double>()
        val yList = arrayListOf<Double>()

        val totalTime = System.currentTimeMillis() - startTime

        if (trajectory != null) {
            var i = 0.0
            var pose = Pose2d()

            var x = 0.0
            var y = 0.0

            while (i < trajectory.length()) {

                val state_ = trajectory.getInterpolated(i).state()

                val twist = Twist2d.fromPose(state_.state().pose)

                val dy = state_.state().pose.y - trajectory.getInterpolated(i - 0.02).state().state().pose.y
                val dx = state_.state().pose.x - trajectory.getInterpolated(i - 0.02).state().state().pose.x
                val dt = state_.t() - state_.t()


                pose = pose.transformBy(Pose2d.fromTwist(Twist2d(state_.velocity(), 0.0, state_.state().curvature)))


                xList.add(state_.state().pose.x)
                yList.add(state_.state().pose.y)

                val ydot = dy / dt
                val xdot = dx / dt

                val state = state_

                System.out.printf("X: %3f, Y: %3f, Theta: %3f\n", state.state().pose.x, state.state().pose.y, pose.rotation.degrees)

                i += 0.02
            }
        }


        val chart = QuickChart.getChart("Path", "X", "Y",
                "Path", xList.toDoubleArray(), yList.toDoubleArray())

        SwingWrapper(chart).displayChart()

        println("\nJob took $totalTime milliseconds.")
        Thread.sleep(100000)
    }
}