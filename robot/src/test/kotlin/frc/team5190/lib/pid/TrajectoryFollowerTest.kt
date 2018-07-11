/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package frc.team5190.lib.pid

import frc.team5190.lib.trajectory.TrajectoryFollower
import org.junit.Test

class TrajectoryFollowerTest {
    @Test
    fun testPathFollowerOutputs() {
        println(TrajectoryFollower.calculateLinearVelocity(
                xError = 0.0,
                yError = 0.0,
                thetaError = 0.0,
                pathV = 5.0,
                pathW = 1.0,
                theta = Math.toRadians(30.0)
        ))
        println(TrajectoryFollower.calculateAngularVelocity(
                xError = 0.0,
                yError = 0.0,
                thetaError = 1E-9,
                pathV = 5.0,
                pathW = 1.0,
                theta = Math.toRadians(30.0)
        ))
    }
}