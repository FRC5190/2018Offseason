package frc.team5190.lib.motion

import com.xeiam.xchart.QuickChart
import com.xeiam.xchart.SwingWrapper
import org.junit.Test

class SCurveFollowerTest {
    @Test
    fun testSCurve() {
        val follower = SCurveFollower(initialPos = 0.0, targetPos = 15.0, cruiseVelocity = 10.0, maxAcceleration = 15.0, jerk = 60.0)

        val xlist = arrayListOf<Double>()
        val yList = arrayListOf<Double>()

        val aList = arrayListOf<Double>()

        println("T Path: ${follower.tpath}")

        var t = 0.0

        while (t < follower.tpath) {
            xlist.add(t)

            val x = follower.getTestOutput(t)

            yList.add(x.second)
            aList.add(x.third)

            t += 0.02
//            Thread.sleep(20)
        }

        val chart = QuickChart.getChart("Velocity Over Time", "Time", "Velocity",
                "y(x)", xlist.toDoubleArray(), yList.toDoubleArray())

        SwingWrapper(chart).displayChart()
        Thread.sleep(1000000)
    }
}