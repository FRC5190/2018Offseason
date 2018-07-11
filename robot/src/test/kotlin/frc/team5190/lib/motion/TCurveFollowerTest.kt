/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package frc.team5190.lib.motion

import org.junit.Test
import org.knowm.xchart.QuickChart
import org.knowm.xchart.SwingWrapper

class TCurveFollowerTest {
    @Test
    fun testTrapezoid() {
        val follower = TCurveFollower(initialPos = 0.0, targetPos = 10.0, cruiseVelocity = 10.0, acceleration = 7.0)

        val xlist = arrayListOf<Double>()
        val yList = arrayListOf<Double>()
        val yList2 = arrayListOf<Double>()

        println("T Path: ${follower.tpath}")

        while (follower.t < follower.tpath) {
            xlist.add(follower.t)

            val x = follower.getOutput()
            yList.add(x.second)
            yList2.add(x.third)

            Thread.sleep(20)
        }

        val chart = QuickChart.getChart("Velocity Over Time", "Time", "Velocity",
                "Value", xlist.toDoubleArray(), yList.toDoubleArray())

        val chart2 = QuickChart.getChart("Position Over Time", "Time", "Positon",
                yList2[yList2.size - 2].toString(), xlist.toDoubleArray(), yList2.toDoubleArray())

        SwingWrapper(chart).displayChart()
        SwingWrapper(chart2).displayChart()
        Thread.sleep(1000000)
    }
}