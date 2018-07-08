package frc.team254.lib.spline

import com.xeiam.xchart.QuickChart
import com.xeiam.xchart.SwingWrapper
import frc.team5190.lib.spline.QuinticHermiteSpline
import frc.team5190.lib.spline.SplineGenerator
import frc.team5190.lib.geometry.Pose2d
import frc.team5190.lib.geometry.Rotation2d
import org.junit.Test

class SplineGeneratorTest {
    @Test
    fun testSplineGeneration() {

        val waypoint1 = Pose2d(0.0, 0.0, Rotation2d.createFromDegrees(0.0))
        val waypoint2 = Pose2d(10.0, -10.0, Rotation2d.createFromDegrees(-40.0))

        val spline = QuinticHermiteSpline(waypoint1, waypoint2)

        val startTime = System.currentTimeMillis()
        val list = SplineGenerator.parameterizeSpline(spline)
        println("Parameterization took ${System.currentTimeMillis() - startTime} milliseconds.")

        val xList = arrayListOf<Double>()
        val yList = arrayListOf<Double>()

        list.forEach {
            println("")
            System.out.printf("X: %3f, Y: %3f, Curvature: %3f", it.pose.x, it.pose.y)
            xList.add(it.pose.x)
            yList.add(it.pose.y)
        }
        val chart = QuickChart.getChart("Path", "X", "Y",
                "Path", xList.toDoubleArray(), yList.toDoubleArray())

        SwingWrapper(chart).displayChart()

        Thread.sleep(100000)

    }
}