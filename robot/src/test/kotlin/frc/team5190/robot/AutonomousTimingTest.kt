package frc.team5190.robot

import frc.team5190.robot.auto.Trajectories
import org.junit.Test

class AutonomousTimingTest {
    @Test
    fun testTiming() {
        Trajectories
        Thread.sleep(1000)
        println("\n\n[Test] 3 Cube Near Scale Auto: ${Trajectories["Left Start to Near Scale"].lastState.t +
                Trajectories["Scale to Cube 1"].lastState.t + Trajectories["Cube 1 to Scale"].lastState.t +
                Trajectories["Scale to Cube 2"].lastState.t + Trajectories["Cube 2 to Scale"].lastState.t + 1} seconds.")

        println("[Test] 3 Cube Far Scale Auto:  ${Trajectories["Left Start to Far Scale"].lastState.t +
                Trajectories["Scale to Cube 1"].lastState.t + Trajectories["Cube 1 to Scale"].lastState.t +
                Trajectories["Scale to Cube 2"].lastState.t + Trajectories["Cube 2 to Scale"].lastState.t + 1} seconds.")
    }

}