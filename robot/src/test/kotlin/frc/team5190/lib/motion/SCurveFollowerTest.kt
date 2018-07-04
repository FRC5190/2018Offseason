package frc.team5190.lib.motion

import com.xeiam.xchart.QuickChart
import com.xeiam.xchart.SwingWrapper
import org.junit.Test

class SCurveFollowerTest {
    @Test
    fun testSCurve() {
        val follower = SCurveFollower(initialPos = 0.0, targetPos = 30.0, cruiseVelocity = 10.0, averageAcceleration = 5.0, jerk = 10.0)

        val xlist = arrayListOf<Double>()
        val yList = arrayListOf<Double>()

        println("T Path: ${follower.tpath}")


        var t = 0.0

        while (follower.t < follower.tpath) {
            xlist.add(follower.t)
            yList.add(follower.getOutput(0.0).second)

            Thread.sleep(20)
            t += 0.02
        }

        val chart = QuickChart.getChart("Velocity Over Time", "Time", "Velocity",
                "y(x)", xlist.toDoubleArray(), yList.toDoubleArray())

        SwingWrapper(chart).displayChart()
        Thread.sleep(1000000)
    }
}