/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package frc.team5190.lib.math.trajectory.followers

import frc.team5190.lib.math.geometry.Pose2d
import frc.team5190.lib.math.geometry.Pose2dWithCurvature
import frc.team5190.lib.math.geometry.Twist2d
import frc.team5190.lib.math.trajectory.Trajectory
import frc.team5190.lib.math.trajectory.TrajectoryIterator
import frc.team5190.lib.math.trajectory.timing.TimedState
import frc.team5190.lib.math.trajectory.view.TimedView

class FeedForwardController(trajectory: Trajectory<TimedState<Pose2dWithCurvature>>,
                            override val dt: Double = 0.02) : TrajectoryFollower {

    private val trajectoryIterator = TrajectoryIterator(TimedView(trajectory))

    override var trajectoryPoint = trajectoryIterator.preview(0.0)

    override val trajectoryPose
        get() = trajectoryPoint.state.state.pose

    override val isFinished
        get() = trajectoryIterator.isDone

    // Returns desired linear and angular cruiseVelocity of the robot
    override fun getSteering(pose: Pose2d) = Twist2d(
            dx = trajectoryPoint.state.velocity,
            dy = 0.0,
            dtheta = (trajectoryIterator.preview(dt).state.state.rotation - trajectoryPose.rotation).radians / dt
    ).also { trajectoryPoint = trajectoryIterator.advance(dt) }
}