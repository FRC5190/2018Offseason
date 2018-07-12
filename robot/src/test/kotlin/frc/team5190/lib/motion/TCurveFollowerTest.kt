/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package frc.team5190.lib.motion

import org.junit.Test
import org.knowm.xchart.QuickChart
import org.knowm.xchart.SwingWrapper
import org.knowm.xchart.XYChartBuilder
import java.awt.Color
import java.awt.Font

class TCurveFollowerTest {
    @Test
    fun testTrapezoid() {
        val follower = TCurveFollower(initialPos = 0.0, targetPos = 10.0, cruiseVelocity = 10.0, acceleration = 7.0)

        val tList = arrayListOf<Double>()
        val pList = arrayListOf<Double>()
        val vList = arrayListOf<Double>()

        println("T Path: ${follower.tpath}")

        while (follower.t < follower.tpath) {
            tList.add(follower.t)

            val x = follower.getOutput()
            pList.add(x.second)
            vList.add(x.third)

            Thread.sleep(20)
        }

        val chart = XYChartBuilder().width(1600).height(1520).title("Trapezoidal Velocity Profile").xAxisTitle("X").yAxisTitle("Y").build()
        chart.styler.markerSize = 8
        chart.styler.seriesColors = arrayOf(Color.BLUE, Color.ORANGE)

        chart.styler.chartTitleFont = Font("Kanit", 1, 40)
        chart.styler.chartTitlePadding = 15

        chart.styler.chartFontColor = Color.WHITE
        chart.styler.axisTickLabelsColor = Color.WHITE

        chart.styler.isPlotGridLinesVisible = true
        chart.styler.isLegendVisible = true

        chart.styler.plotGridLinesColor = Color.GRAY
        chart.styler.chartBackgroundColor = Color.DARK_GRAY
        chart.styler.plotBackgroundColor = Color.DARK_GRAY

        chart.addSeries("Position", tList.toDoubleArray(), pList.toDoubleArray())
        chart.addSeries("Velocity", tList.toDoubleArray(), vList.toDoubleArray())

        SwingWrapper(chart).displayChart()
        Thread.sleep(1000000)
    }
}