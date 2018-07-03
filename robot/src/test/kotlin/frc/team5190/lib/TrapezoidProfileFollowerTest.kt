package frc.team5190.lib

import com.xeiam.xchart.QuickChart
import com.xeiam.xchart.SwingWrapper
import frc.team5190.lib.motion.TrapezoidProfileFollower
import org.junit.Test

class TrapezoidProfileFollowerTest {
    @Test
    fun testTrapezoid() {
        val follower = TrapezoidProfileFollower(initialPos = 0.0, targetPos = 10.0, cruiseVelocity = 10.0, acceleration = 7.0)

        val xlist = arrayListOf<Double>()
        val yList = arrayListOf<Double>()

        println("T Path: ${follower.tpath}")

        while (follower.t < follower.tpath) {
            xlist.add(follower.t)
            yList.add(follower.getOutput(0.0).second)

            Thread.sleep(20)
        }

        val chart = QuickChart.getChart("Velocity Over Time", "Time", "Velocity",
                "y(x)", xlist.toDoubleArray(), yList.toDoubleArray())

        SwingWrapper(chart).displayChart()
        Thread.sleep(1000000)
    }
}