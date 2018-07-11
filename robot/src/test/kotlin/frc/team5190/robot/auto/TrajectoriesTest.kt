/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package frc.team5190.robot.auto


import frc.team5190.lib.trajectory.TrajectoryIterator
import frc.team5190.lib.trajectory.TrajectoryUtil
import org.junit.Test
import org.knowm.xchart.SwingWrapper
import org.knowm.xchart.XYChartBuilder
import java.awt.Color
import java.awt.Font
import java.text.DecimalFormat

class TrajectoriesTest {
    @Test
    fun testTrajectories() {

        val name = "Left Start to Far Scale"

        val trajectory = TrajectoryUtil.mirrorTimed(Trajectories[name])
        val iterator = TrajectoryIterator(trajectory.indexView)

        val xList = arrayListOf<Double>()
        val yList = arrayListOf<Double>()

        while (!iterator.isDone) {
            val point = iterator.advance(0.02)
            xList.add(point.state.state.translation.x)
            yList.add(point.state.state.translation.y)

            System.out.printf("X: %2.3f, Y: %2.3f, C: %2.3f, T: %2.3f, V: %2.3f\n", point.state.state.translation.x,
                    point.state.state.translation.y, point.state.state.curvature, point.state.state.rotation.degrees, point.state.velocity)
        }


        val fm = DecimalFormat("#.###").format(trajectory.lastState.t)

        val chart = XYChartBuilder().width(1600).height(1520).title("$name: $fm seconds.")
                .xAxisTitle("X").yAxisTitle("Y").build()

        chart.styler.markerSize = 8
        chart.styler.seriesColors = arrayOf(Color(151, 60, 67))

        chart.styler.chartTitleFont = Font("Kanit", 1, 40)
        chart.styler.chartTitlePadding = 15

        chart.styler.chartFontColor = Color.WHITE
        chart.styler.axisTickLabelsColor = Color.WHITE

        chart.styler.xAxisMin = 1.0
        chart.styler.xAxisMax = 26.0
        chart.styler.yAxisMin = 1.0
        chart.styler.yAxisMax = 26.0

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