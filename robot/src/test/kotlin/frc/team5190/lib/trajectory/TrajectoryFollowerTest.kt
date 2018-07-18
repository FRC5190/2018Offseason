/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package frc.team5190.lib.trajectory

import frc.team5190.lib.geometry.Pose2d
import frc.team5190.lib.geometry.Pose2dWithCurvature
import frc.team5190.lib.trajectory.timing.TimedState
import frc.team5190.lib.trajectory.view.TimedView
import frc.team5190.robot.auto.Trajectories
import org.junit.Test
import org.knowm.xchart.SwingWrapper
import org.knowm.xchart.XYChartBuilder
import java.awt.Color
import java.awt.Font
import java.text.DecimalFormat

class TrajectoryFollowerTest {
    @Test
    fun testTrajectoryFollower() {
        val name       = "Pyramid to Scale"
        val trajectory: Trajectory<TimedState<Pose2dWithCurvature>> = Trajectories[name]
        val iterator = TrajectoryIterator(TimedView(trajectory))
        val follower = TrajectoryFollower(trajectory)


        var totalpose = trajectory.firstState.state.pose

        val xList = arrayListOf<Double>()
        val yList = arrayListOf<Double>()

        while (!iterator.isDone) {
            val pose = iterator.advance(0.02).state.state.pose
            val output = follower.getRobotVelocity(pose)

            System.out.printf("Linear Velocity: %3.3f, Angular Velocity: %3.3f%n", output.dx, output.dtheta)

            val positiondelta = output.scaled(0.02)
            val transformed   = totalpose.transformBy(Pose2d.fromTwist(positiondelta))

            xList.add(totalpose.translation.x)
            yList.add(totalpose.translation.y)

            totalpose = transformed

            Thread.sleep(20)
        }

        val fm = DecimalFormat("#.###").format(trajectory.lastState.t)

        val chart = XYChartBuilder().width(1600).height(1520).title("$name: $fm seconds.")
                .xAxisTitle("X").yAxisTitle("Y").build()

        chart.styler.markerSize = 8
        chart.styler.seriesColors = arrayOf(Color(151, 60, 67))

        chart.styler.chartTitleFont = Font("Kanit", 1, 40)
        chart.styler.chartTitlePadding = 15

        chart.styler.xAxisMin = 1.0
        chart.styler.xAxisMax = 26.0
        chart.styler.yAxisMin = 1.0
        chart.styler.yAxisMax = 26.0

        chart.styler.chartFontColor = Color.WHITE
        chart.styler.axisTickLabelsColor = Color.WHITE

        chart.styler.isPlotGridLinesVisible = true
        chart.styler.isLegendVisible = false

        chart.styler.plotGridLinesColor = Color.GRAY
        chart.styler.chartBackgroundColor = Color.DARK_GRAY
        chart.styler.plotBackgroundColor = Color.DARK_GRAY

        chart.addSeries("Trajectory", xList.toDoubleArray(), yList.toDoubleArray())

        SwingWrapper(chart).displayChart()
        Thread.sleep(100000)
    }
}