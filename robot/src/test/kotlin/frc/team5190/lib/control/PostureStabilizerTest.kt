package frc.team5190.lib.control

import frc.team5190.lib.math.Pose2D
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D
import org.junit.Test

class PostureStabilizerTest {
    @Test
    fun testPostureStabilizerOutputs() {
        val goal = Pose2D(Vector2D(1.0, 1.0), 0.0)
        val pose = Pose2D(Vector2D(0.7, 0.7), Math.toRadians(-10.0))

        val ps = PostureStabilizer(goal)
        ps.getRobotVelocity(pose)
    }
}