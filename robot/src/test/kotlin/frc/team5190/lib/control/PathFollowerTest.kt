package frc.team5190.lib.control

import org.junit.Test

class PathFollowerTest {
    @Test
    fun testPathFollowerOutputs() {
        println(PathFollower.calculateLinearVelocity(
                xError = 0.0,
                yError = 0.0,
                thetaError = 0.0,
                pathV = 5.0,
                pathW = 1.0,
                theta = Math.toRadians(30.0)
        ))
        println(PathFollower.calculateAngularVelocity(
                xError = 0.0,
                yError = 0.0,
                thetaError = 1E-9,
                pathV = 5.0,
                pathW = 1.0,
                theta = Math.toRadians(30.0)
        ))
    }
}