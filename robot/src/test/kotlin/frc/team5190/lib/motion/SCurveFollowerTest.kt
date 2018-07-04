package frc.team5190.lib.motion

import com.xeiam.xchart.QuickChart
import com.xeiam.xchart.SwingWrapper
import org.junit.Test

class SCurveFollowerTest {
    @Test
    fun testSCurve() {
        val follower = SCurveFollower(initialPos = 0.0, targetPos = 40.0, cruiseVelocity = 20.0, maxAcceleration = 10.0, jerk = 10.0)

        val tList = arrayListOf<Double>()
        val vList = arrayListOf<Double>()
        val pList = arrayListOf<Double>()

        println("T Path: ${follower.tpath}")

        var t = 0.0

        while (t < follower.tpath) {
            tList.add(t)

            val x = follower.getTestOutput(t)

            vList.add(x.second)
            pList.add(x.first)

            t += 0.02
        }

        val chart = QuickChart.getChart("Velocity Over Time", "Time", "Velocity",
                pList.last().toString(), tList.toDoubleArray(), vList.toDoubleArray())

        val chart2 = QuickChart.getChart("Position Over Time", "Time", "Velocity",
                pList.last().toString(), tList.toDoubleArray(), pList.toDoubleArray())

        SwingWrapper(chart).displayChart()
        SwingWrapper(chart2).displayChart()
        Thread.sleep(1000000)
    }
}