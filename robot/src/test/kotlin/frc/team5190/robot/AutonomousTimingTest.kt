package frc.team5190.robot

import frc.team5190.robot.auto.Trajectories
import org.junit.Test

class AutonomousTimingTest {
    @Test
    fun testTiming() {
        Trajectories
        Thread.sleep(1000)

        println("\n\n[Test] 3 Cube Near Scale Auto: ${Trajectories.leftStartToNearScale.lastState.t +
                Trajectories.scaleToCube1.lastState.t + Trajectories.cube1ToScale.lastState.t +
                Trajectories.scaleToCube2.lastState.t + Trajectories.cube2ToScale.lastState.t + 1.25} seconds.")

        println("[Test] 3 Cube Far Scale Auto: ${Trajectories.leftStartToFarScale.lastState.t +
                Trajectories.scaleToCube1.lastState.t + Trajectories.cube1ToScale.lastState.t +
                Trajectories.scaleToCube2.lastState.t + Trajectories.cube2ToScale.lastState.t + 1.25} seconds.")

        println("[Test] 2 Cube Switch Auto:  ${Trajectories.centerStartToLeftSwitch.lastState.t +
                Trajectories.switchToCenter.lastState.t + Trajectories.centerToPyramid.lastState.t +
                Trajectories.pyramidToCenter.lastState.t + Trajectories.centerToSwitch.lastState.t + 1.25} seconds.")

        println("[Test] 2 Cube Switch and Scale Auto:  ${Trajectories.centerStartToLeftSwitch.lastState.t +
                Trajectories.switchToCenter.lastState.t + Trajectories.centerToPyramid.lastState.t +
                Trajectories.pyramidToScale.lastState.t + 1.25} seconds.")

        assert(true)
    }

}