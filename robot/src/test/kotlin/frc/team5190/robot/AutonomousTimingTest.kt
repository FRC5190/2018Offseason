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
                Trajectories["Scale to Cube 2"].lastState.t + Trajectories["Cube 2 to Scale"].lastState.t + 1.25} seconds.")

        println("[Test] 3 Cube Far Scale Auto: ${Trajectories["Left Start to Far Scale"].lastState.t +
                Trajectories["Scale to Cube 1"].lastState.t + Trajectories["Cube 1 to Scale"].lastState.t +
                Trajectories["Scale to Cube 2"].lastState.t + Trajectories["Cube 2 to Scale"].lastState.t + 1.25} seconds.")

        println("[Test] 2 Cube Switch Auto:  ${Trajectories["Center Start to Left Switch"].lastState.t +
                Trajectories["Switch to Center"].lastState.t + Trajectories["Center to Pyramid"].lastState.t +
                Trajectories["Pyramid to Center"].lastState.t + Trajectories["Center to Switch"].lastState.t + 1.25} seconds.")

        println("[Test] 2 Cube Switch and Scale Auto:  ${Trajectories["Center Start to Left Switch"].lastState.t +
                Trajectories["Switch to Center"].lastState.t + Trajectories["Center to Pyramid"].lastState.t +
                Trajectories["Pyramid to Scale"].lastState.t + 1.25} seconds.")

        assert(true)
    }

}