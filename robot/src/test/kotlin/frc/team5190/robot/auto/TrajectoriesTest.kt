package frc.team5190.robot.auto


import frc.team5190.lib.trajectory.TrajectoryIterator
import org.junit.Test
import org.knowm.xchart.SwingWrapper
import org.knowm.xchart.XYChartBuilder
import java.awt.Color
import java.awt.Font
import java.text.DecimalFormat
import kotlin.math.absoluteValue

class TrajectoriesTest {
    @Test
    fun testTrajectories() {

        val name = "Near Scale to Cube 3"

        val trajectory = Trajectories[name]
        val iterator = TrajectoryIterator(trajectory.indexView)

        val xList = arrayListOf<Double>()
        val yList = arrayListOf<Double>()

        val tList = arrayListOf<Double>()
        val vList = arrayListOf<Double>()

        while (!iterator.isDone) {
            val point = iterator.advance(0.02)
            xList.add(point.state.state.translation.x)
            yList.add(point.state.state.translation.y)
            tList.add(point.state.t)
            vList.add(point.state.velocity.absoluteValue)
            System.out.printf("X: %2.3f, Y: %2.3f, C: %2.3f, V: %2.3f\n", point.state.state.translation.x,
                    point.state.state.translation.y, point.state.state.curvature, point.state.velocity)
        }


        val fm = DecimalFormat("#.###").format(trajectory.lastState.t)

        val chart = XYChartBuilder().width(1600).height(1520).title("$name: $fm seconds.")
                .xAxisTitle("X").yAxisTitle("Y").build()

        chart.styler.markerSize = 4
        chart.styler.seriesColors = arrayOf(Color.MAGENTA, Color.WHITE)

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
        chart.addSeries("Velocity", tList.toDoubleArray(), vList.toDoubleArray())


        SwingWrapper(chart).displayChart()
        Thread.sleep(100000)

    }
}