package frc.team5190.robot.auto

import com.xeiam.xchart.QuickChart
import com.xeiam.xchart.SwingWrapper
import frc.team5190.lib.trajectory.TrajectoryIterator
import org.junit.Test

class TrajectoryGeneratorTest {
    @Test
    fun testTrajectoryGenerator() {


        val trajectory = Trajectories["Cube 2 to Far Scale"]


        val iterator = TrajectoryIterator(trajectory.indexView)

        val xList = arrayListOf<Double>()
        val yList = arrayListOf<Double>()

        while (!iterator.isDone) {
            val point = iterator.advance(0.02)
            xList.add(point.state.state.translation.x)
            yList.add(point.state.state.translation.y)

            println(point.state.state.rotation.degrees)
        }

        xList.add(0.0); yList.add(0.0)

        SwingWrapper(QuickChart.getChart(" ", " ", " ", " ", xList.toDoubleArray(), yList.toDoubleArray())).displayChart()
        Thread.sleep(10000000)

    }
}