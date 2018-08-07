/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package frc.team5190.lib.math.trajectory.followers

import frc.team5190.lib.math.geometry.*
import frc.team5190.lib.math.trajectory.Trajectory
import frc.team5190.lib.math.trajectory.TrajectoryIterator
import frc.team5190.lib.math.trajectory.timing.TimedState
import frc.team5190.lib.math.trajectory.view.TimedView
import frc.team5190.robot.auto.Trajectories
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Test
import org.knowm.xchart.SwingWrapper
import org.knowm.xchart.XYChartBuilder
import java.awt.Color
import java.awt.Font
import java.text.DecimalFormat
import java.util.concurrent.TimeUnit

class NonLinearControllerTest {

    private lateinit var trajectoryFollower: TrajectoryFollower

    @Test
    fun testTrajectoryFollower() {
        val name = "Left Start to Far Scale"
        val trajectory: Trajectory<TimedState<Pose2dWithCurvature>> = Trajectories[name]
        val iterator = TrajectoryIterator(TimedView(trajectory))
        trajectoryFollower = NonLinearController(trajectory)

        var totalpose = trajectory.firstState.state.pose.transformBy(Pose2d(Translation2d(0.0, 3.0), Rotation2d.fromDegrees(20.0)))

        var time = 0.0
        val dt = 0.02

        val xList = arrayListOf<Double>()
        val yList = arrayListOf<Double>()

        val refXList = arrayListOf<Double>()
        val refYList = arrayListOf<Double>()

        while (!iterator.isDone) {
            val pt = iterator.advance(dt)
            val output = trajectoryFollower.getSteering(totalpose, time.toLong()).scaled(0.02)
            time += dt * 1.0e+9

            assert(if (trajectory.firstState.acceleration > 0) output.dx >= 0 else output.dx <= 0)

            totalpose = totalpose.transformBy(Pose2d.fromTwist(Twist2d(output.dx, output.dy, output.dtheta * 5.0)))

            xList.add(totalpose.translation.x)
            yList.add(totalpose.translation.y)

            refXList.add(pt.state.state.translation.x)
            refYList.add(pt.state.state.translation.y)
        }

        val fm = DecimalFormat("#.###").format(trajectory.lastState.t)

        val chart = XYChartBuilder().width(1800).height(1520).title("$name: $fm seconds.")
                .xAxisTitle("X").yAxisTitle("Y").build()

        chart.styler.markerSize = 8
        chart.styler.seriesColors = arrayOf(Color.ORANGE, Color(151, 60, 67))

        chart.styler.chartTitleFont = Font("Kanit", 1, 40)
        chart.styler.chartTitlePadding = 15

        chart.styler.xAxisMin = 1.0
        chart.styler.xAxisMax = 26.0
        chart.styler.yAxisMin = 1.0
        chart.styler.yAxisMax = 26.0

        chart.styler.chartFontColor = Color.WHITE
        chart.styler.axisTickLabelsColor = Color.WHITE

        chart.styler.legendBackgroundColor = Color.GRAY

        chart.styler.isPlotGridLinesVisible = true
        chart.styler.isLegendVisible = true

        chart.styler.plotGridLinesColor = Color.GRAY
        chart.styler.chartBackgroundColor = Color.DARK_GRAY
        chart.styler.plotBackgroundColor = Color.DARK_GRAY

        chart.addSeries("Trajectory", refXList.toDoubleArray(), refYList.toDoubleArray())
        chart.addSeries("Robot", xList.toDoubleArray(), yList.toDoubleArray())

        assert((trajectory.lastState.state.translation - totalpose.translation).norm.also {
            println("Norm of Translational Error: $it")
        } < 0.50)
        assert((trajectory.lastState.state.rotation - totalpose.rotation).degrees.also {
            println("Rotational Error: $it degrees")
        } < 5.0)

        SwingWrapper(chart).displayChart()

        runBlocking { delay(100, TimeUnit.SECONDS) }
    }
}