/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package frc.team5190.lib.math.trajectory

import frc.team5190.lib.math.geometry.Pose2d
import frc.team5190.lib.math.geometry.Pose2dWithCurvature
import frc.team5190.lib.math.geometry.Translation2d
import frc.team5190.lib.math.geometry.Twist2d
import frc.team5190.lib.math.trajectory.followers.NonLinearReferenceController
import frc.team5190.lib.math.trajectory.timing.TimedState
import frc.team5190.lib.math.trajectory.view.TimedView
import frc.team5190.robot.auto.Trajectories
import org.junit.Test
import org.knowm.xchart.SwingWrapper
import org.knowm.xchart.XYChartBuilder
import java.awt.Color
import java.awt.Font
import java.text.DecimalFormat

class TimeVaryingNonLinearFollowerTest {

    private lateinit var trajectoryFollower: NonLinearReferenceController

    @Test
    fun testTrajectoryFollower() {
        val name      = "Pyramid to Scale"
        val trajectory: Trajectory<TimedState<Pose2dWithCurvature>> = Trajectories[name]
        val iterator = TrajectoryIterator(TimedView(trajectory))
        trajectoryFollower = NonLinearReferenceController(trajectory)

        var crossed = false

        val marker = addMarkerAt(Translation2d(22.3, 20.6), trajectory)

        var totalpose = trajectory.firstState.state.pose

        val xList = arrayListOf<Double>()
        val yList = arrayListOf<Double>()

        val x2List = arrayListOf<Double>()
        val y2List = arrayListOf<Double>()

        while (!iterator.isDone) {
            val pose = iterator.advance(0.02).state.state.pose
            val output = trajectoryFollower.getSteering(totalpose)

//            System.out.printf("Linear Velocity: %3.3f, Angular Velocity: %3.3f%n", output.dx, output.dtheta)

            if (hasCrossedMarker(marker) && !crossed) {
                println("Crossed Marker at ${pose.translation}")
                crossed = true
            }

            val positiondelta = Twist2d(output.scaled(0.02).dx, output.scaled(0.02).dy, output.scaled(0.1).dtheta)
            val transformed   = totalpose.transformBy(Pose2d.fromTwist(positiondelta))

            xList.add(totalpose.translation.x)
            yList.add(totalpose.translation.y)

            x2List.add(pose.translation.x)
            y2List.add(pose.translation.y)

            totalpose = transformed
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

        chart.addSeries("Trajectory", x2List.toDoubleArray(), y2List.toDoubleArray())
        chart.addSeries("Robot", xList.toDoubleArray(), yList.toDoubleArray())


        SwingWrapper(chart).displayChart()
        Thread.sleep(100000)
    }

    private fun addMarkerAt(waypoint: Translation2d, trajectory: Trajectory<TimedState<Pose2dWithCurvature>>): Marker {
        // Iterate through the trajectory and add a data point every 50 ms.
        val iterator = TrajectoryIterator(TimedView(trajectory))
        val dataArray = arrayListOf<TrajectorySamplePoint<TimedState<Pose2dWithCurvature>>>()

        while (!iterator.isDone) {
            dataArray.add(iterator.advance(0.05))
        }

        return Marker((dataArray.minBy { waypoint.distance(it.state.state.translation) }!!.state.t)
                .also { t -> println("[Trajectory Follower] Added Marker at T = $t seconds.") })
    }

    private fun hasCrossedMarker(marker: Marker): Boolean {
        return trajectoryFollower.point.state.t > marker.t
    }

    private class Marker(val t: Double)
}